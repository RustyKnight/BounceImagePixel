/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel;

import static bounceimagepixel.BounceImagePixel.AnimatableDot.State.IMAGE;
import static bounceimagepixel.BounceImagePixel.AnimatableDot.State.RUN;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.kaizen.animation.Animatable;
import org.kaizen.animation.AnimatableDuration;
import org.kaizen.animation.AnimatableDurationListenerAdapter;
import org.kaizen.animation.AnimatableListenerAdapter;
import org.kaizen.animation.DefaultAnimatable;
import org.kaizen.animation.DefaultAnimatableDuration;
import org.kaizen.animation.curves.Curves;
import org.kaizen.animation.ranges.DoubleRange;
import org.kaizen.animation.ranges.FloatRange;
import org.kaizen.animation.ranges.Range;

public class BounceImagePixel {

    public static void main(String[] args) {
        new BounceImagePixel();
    }

    public BounceImagePixel() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedImage img = ImageIO.read(getClass().getResource("/images/MegaTokyo.png"));
                    JFrame frame = new JFrame();
                    frame.add(new TestPane(img));
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(BounceImagePixel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public class TestPane extends JPanel {

        private double radius = 300;
        private final double diameter = radius * 2;
        private List<AnimatableDot> dots = new ArrayList<>(1024);

        // This is a good demonstration of why state variables need to be handled better
        // Maybe encapsulated into custom animatables?
        private boolean requiresTransition = false;
        private float alpha = 1.0f;
        private FloatRange alphaRange = new FloatRange(0f, 1.0f);

        private boolean isBouncing = false;

        private BufferedImage sourceImage;

        private DefaultAnimatable animatable = new DefaultAnimatable(new AnimatableListenerAdapter() {
            @Override
            public void animationChanged(Animatable animator) {
                repaint();
            }
        });

        private AnimatableDuration transitionAnimation = new DefaultAnimatableDuration(Duration.ofSeconds(1), Curves.QUART_IN.getCurve(), new AnimatableDurationListenerAdapter() {
            @Override
            public void animationChanged(Animatable animator) {
                alpha = alphaRange.valueAt(transitionAnimation.getProgress());
                repaint();
            }

            @Override
            public void animationCompleted(Animatable animator) {
                didCompleteTransition();
            }
        });

        public TestPane(BufferedImage img) {
            setBackground(Color.BLACK);

            sourceImage = img;

            AnimatableDot.Observer dotObserver = new AnimatableDot.Observer() {
                @Override
                public void dotAnimationDidComplete(AnimatableDot source) {
                    dotsThatHaveCompleted += 1;
                    stopIfAllDotsCompleted();
                }
            };

            prepareImageDots(img, dotObserver);
//            preparePixelDots(img, dotObserver);

            Collections.shuffle(dots);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isBouncing = !isBouncing;
                    animatable.stop();
                    dotsThatHaveCompleted = 0;
                    
                    if (isBouncing && requiresTransition) {
                        alphaRange.reverse();
                        transitionAnimation.start();
                    } else {
                        didCompleteTransition();
                    }
                }
            });
        }

        protected void didCompleteTransition() {            
            Instant startedAt = Instant.now();
            for (AnimatableDot dot : dots) {
                if (isBouncing) {
                    dot.setState(AnimatableDot.State.RUN);
                } else {
                    dot.setState(AnimatableDot.State.IMAGE);
                }
            }
            Duration duration = Duration.between(startedAt, Instant.now());
            System.out.println("Took: " + duration.toSecondsPart() + "." + duration.toMillisPart() + "s");
            animatable.start();
        }

        private int dotsThatHaveCompleted = 0;

        protected void stopIfAllDotsCompleted() {
            if (animatable.isRunning()) {
                if (dotsThatHaveCompleted >= dots.size()) {
                    animatable.stop();
                    if (requiresTransition) {
                        alphaRange.reverse();
                        transitionAnimation.start();
                    }
                }
            }
            repaint();
        }

        protected void prepareImageDots(BufferedImage img, AnimatableDot.Observer dotObserver) {
            requiresTransition = true;

            int pixelSize = 10;
            BufferedImage pixelated = ImageUtil.pixelate(img, pixelSize);

            int rows = (int) Math.ceil(img.getWidth() / (double) pixelSize);
            int cols = (int) Math.ceil(img.getHeight() / (double) pixelSize);

            Random rnd = new Random();

            int xOffset = (int) (diameter - img.getWidth()) / 2;
            int yOffset = (int) (diameter - img.getWidth()) / 2;

            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
//                    Color color = new Color(img.getRGB(x, y), true);

                    int blockWidth = pixelSize;
                    int blockHeight = pixelSize;

                    int xPos = x * pixelSize;
                    int yPos = y * pixelSize;

                    if (xPos + blockWidth > pixelated.getWidth()) {
                        blockWidth = pixelated.getWidth() - xPos;
                    }
                    if (yPos + blockHeight > pixelated.getHeight()) {
                        blockHeight = pixelated.getHeight() - yPos;
                    }

                    BufferedImage block = pixelated.getSubimage(xPos, yPos, blockWidth, blockHeight);
                    Point2D pixelLocation = new Point2D.Double(xPos + xOffset, yPos + yOffset);

                    ImageDot dot = new ImageDot(pixelLocation, block);
                    double fromAngle = 360.0 * rnd.nextDouble();

                    double angleRange = (180 * rnd.nextDouble()) * (rnd.nextBoolean() ? 1 : -1);

                    double toAngle = fromAngle + angleRange;

                    Point2D fromPoint = Utilities.pointOnCircle(fromAngle, radius);
                    Point2D toPoint = Utilities.pointOnCircle(toAngle, radius);

                    Point2DRange animationRange = new Point2DRange(fromPoint, toPoint);

                    MovableAnimatableDot animatable = new MovableAnimatableDot(dot, animationRange, dotObserver);
                    dots.add(animatable);
                }
            }
        }

        protected void preparePixelDots(BufferedImage img, AnimatableDot.Observer dotObserver) {
            Random rnd = new Random();

            int xOffset = (int) (diameter - img.getWidth()) / 2;
            int yOffset = (int) (diameter - img.getWidth()) / 2;

            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    Color color = new Color(img.getRGB(x, y), true);
                    Point2D pixelLocation = new Point2D.Double(x + xOffset, y + yOffset);
                    PixelDot dot = new PixelDot(pixelLocation, color);
//                    if (pixel % 100 == 0) {
                    double fromAngle = 360.0 * rnd.nextDouble();

                    double angleRange = (180 * rnd.nextDouble()) * (rnd.nextBoolean() ? 1 : -1);

                    double toAngle = fromAngle + angleRange;

                    Point2D fromPoint = Utilities.pointOnCircle(fromAngle, radius);
                    Point2D toPoint = Utilities.pointOnCircle(toAngle, radius);

                    Point2DRange animationRange = new Point2DRange(fromPoint, toPoint);

                    MovableAnimatableDot animatable = new MovableAnimatableDot(dot, animationRange, dotObserver);
                    dots.add(animatable);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 800);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
            );
            g2d.setRenderingHints(hints);

            double diameter = radius * 2;

            double xOffset = (getWidth() - diameter) / 2d;
            double yOffset = (getHeight() - diameter) / 2d;
            g2d.translate(xOffset, yOffset);
            g2d.setColor(Color.DARK_GRAY);
            g2d.draw(new Ellipse2D.Double(0, 0, diameter, diameter));

            if (requiresTransition) {
                g2d.setComposite(AlphaComposite.SrcOver.derive(1.0f - alpha));
            }

            for (AnimatableDot dot : dots) {
                dot.paint(g2d);
            }

            if (requiresTransition) {
                int xPos = (int) (diameter - sourceImage.getWidth()) / 2;
                int yPos = (int) (diameter - sourceImage.getHeight()) / 2;
                g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
                g2d.drawImage(sourceImage, xPos, yPos, this);
            }

            g2d.dispose();
        }

    }

    public class Utilities {

        public static Point2D pointOnCircle(double degress, double radius) {
            double rads = Math.toRadians(degress - 90); // 0 becomes the top
            double xPosy = Math.round((Math.cos(rads) * radius));
            double yPosy = Math.round((Math.sin(rads) * radius));
            return new Point2D.Double(radius + xPosy, radius + yPosy);
        }
    }

    public static interface AnimatableDot {

        enum State {
            RUN, IMAGE;
        }

        public interface Observer {

            public void dotAnimationDidComplete(AnimatableDot source);
        }

        public void setState(State state);

        public State getState();

        public void paint(Graphics2D g2d);

        public Observer getObserver();

    }

    public static abstract class AbstractAnimatableDot implements AnimatableDot {

        private State state = State.IMAGE;

        private Dot dot;
        private Observer observer;

        public AbstractAnimatableDot(Dot dot, Observer observer) {
            this.dot = dot;
            this.observer = observer;
        }

        public abstract Duration getAnimationDuration();

        @Override
        public Observer getObserver() {
            return observer;
        }

        public Dot getDot() {
            return dot;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            if (this.state == state) {
                return;
            }
            this.state = state;
            stateDidChange();
        }

        private Timer delayedStartTimer;

        protected abstract void stateDidChange();

        protected abstract void startAnimation();

    }

    public static abstract class RandimisedDurationAnimatableDot extends AbstractAnimatableDot {

        // Randomise the bounce duration on each cycle
        private Random rnd = new Random();
        private DurationRange animationRange = new DurationRange(Duration.ofSeconds(3), Duration.ofSeconds(5));

        public RandimisedDurationAnimatableDot(Dot dot, Observer observer) {
            super(dot, observer);
        }

        @Override
        public Duration getAnimationDuration() {
            Duration valueAt = animationRange.valueAt(rnd.nextDouble());
            return valueAt;
        }

    }

    public static class MovableAnimatableDot extends RandimisedDurationAnimatableDot {

        private Point2DRange bounceRange;
        private Point2D currentLocation;

        private Animatable bounceAnimatable;
        private Animatable imageAnimatable;
        private Point2DRange animationRange;

        private boolean isFirstBouncePass = true;

        public MovableAnimatableDot(Dot dot, Point2DRange animationRange, Observer observer) {
            super(dot, observer);
            this.bounceRange = animationRange;
            this.animationRange = bounceRange;
            this.currentLocation = dot.getImageLocation();

            makeBounceAnimatable();
            makeImageAnimatable();
        }

        @Override
        public void paint(Graphics2D g) {
            Dot dot = getDot();
            Point2D location = currentLocation;
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(location.getX(), location.getY());
            dot.paint(g2d);
            g2d.dispose();
        }

        @Override
        protected void stateDidChange() {
            switch (getState()) {
                case IMAGE:
                    preRollImageAnimation();
                    break;
                case RUN:
                    imageAnimatable.stop();
                    isFirstBouncePass = true;
                    animationRange = new Point2DRange(currentLocation, bounceRange.getTo());
                    startAnimation();
                    break;
            }
        }

        protected void preRollImageAnimation() {
            bounceAnimatable.stop();
            if (currentLocation == getDot().getImageLocation()) {
                imageAnimatable.stop();
                return;
            }
            animationRange = new Point2DRange(currentLocation, getDot().getImageLocation());
            startAnimation();
        }

        @Override
        protected void startAnimation() {
            if (getState() == RUN) {
                startBounceAnimation();
            } else {
                startImageAnimation();
            }
        }

        protected void makeBounceAnimatable() {
            bounceAnimatable = new DefaultAnimatableDuration(getAnimationDuration(), Curves.QUART_IN_OUT.getCurve(), new AnimatableDurationListenerAdapter() {
                @Override
                public void animationCompleted(Animatable animator) {
                    if (getState() == IMAGE) {
                        animator.stop();
                        preRollImageAnimation();
                        return;
                    }
                    currentLocation = animationRange.valueAt(1);
                    if (isFirstBouncePass) {
                        isFirstBouncePass = false;
                        animationRange = bounceRange;
                    }
                    animationRange.reverse();
                    bounceAnimatable.start();
                }

                @Override
                public void animationTimeChanged(AnimatableDuration animatable) {
                    double progress = animatable.getProgress();
                    currentLocation = animationRange.valueAt(progress);
                }
            });
        }

        protected void startBounceAnimation() {
            if (getState() != RUN) {
                return;
            }
            if (bounceAnimatable.isRunning()) {
                return;
            }
            bounceAnimatable.start();
        }

        protected void makeImageAnimatable() {
//            Duration duration = getAnimationDuration();
            Duration duration = Duration.ofSeconds(1);
            imageAnimatable = new DefaultAnimatableDuration(duration, Curves.QUART_OUT.getCurve(), new AnimatableDurationListenerAdapter() {
                @Override
                public void animationCompleted(Animatable animator) {
                    currentLocation = animationRange.valueAt(1);
                    imageAnimatable.stop();
                    getObserver().dotAnimationDidComplete(MovableAnimatableDot.this);
                }

                @Override
                public void animationTimeChanged(AnimatableDuration animatable) {
                    double progress = animatable.getProgress();
                    currentLocation = animationRange.valueAt(progress);
                }
            });
        }

        protected void startImageAnimation() {
            if (getState() != IMAGE) {
                return;
            }
            if (imageAnimatable.isRunning()) {
                return;
            }
            imageAnimatable.start();
        }

    }

    public static class FadableAnimatableDot extends RandimisedDurationAnimatableDot {

        private DoubleRange fadeInRange = new DoubleRange(0d, 1d);
        private DoubleRange fadeOutRange = new DoubleRange(1d, 0d);
        private DoubleRange animationRange = fadeInRange;
        private Animatable animatable;

        private double opacity = 1;

        public FadableAnimatableDot(Dot dot, Observer observer) {
            super(dot, observer);
        }

        @Override
        public void paint(Graphics2D g) {
            Dot dot = getDot();
            Point2D location = dot.getImageLocation();
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
            g2d.translate(location.getX(), location.getY());
            dot.paint(g2d);
            g2d.dispose();
        }

        @Override
        protected void stateDidChange() {
            if (animatable != null) {
                return;
            }
            switch (getState()) {
                case IMAGE:
                    animationRange = fadeInRange;
                    startAnimation();
                    break;
                case RUN:
                    animationRange = fadeOutRange;
                    startAnimation();
                    break;
            }
        }

        protected void stopAnimation() {
            if (animatable != null) {
                animatable.stop();
                animatable = null;
            }
        }

        @Override
        protected void startAnimation() {
            if (animatable != null) {
                return;
            }
            animatable = new DefaultAnimatableDuration(getAnimationDuration(), Curves.QUART_IN_OUT.getCurve(), new AnimatableDurationListenerAdapter() {
                @Override
                public void animationCompleted(Animatable animator) {
                    opacity = animationRange.valueAt(1);
                    stopAnimation();
                    getObserver().dotAnimationDidComplete(FadableAnimatableDot.this);
                }

                @Override
                public void animationTimeChanged(AnimatableDuration animatable) {
                    double progress = animatable.getProgress();
                    opacity = animationRange.valueAt(progress);
                }
            });
            animatable.start();
        }

    }

    public static interface Dot {

        public Point2D getImageLocation();

        public void paint(Graphics2D g);
    }

    public static class ImageDot implements Dot {

        private Color color;
        private Point2D imageLocation;
        private Shape clipShape;
        private BufferedImage img;

        public ImageDot(Point2D pixelPoint, BufferedImage img) {
            this.color = color;
            this.imageLocation = pixelPoint;
            this.img = img;

            int width = img.getWidth();
            int height = img.getHeight();

            clipShape = new RoundRectangle2D.Double(0, 0, width, height, width / 4, height / 4);
        }

        public Point2D getImageLocation() {
            return imageLocation;
        }

        public void paint(Graphics2D g) {
            Graphics2D g2d = (Graphics2D) g.create();
//            g2d.setColor(Color.WHITE);
//            g2d.draw(clipShape);
            g2d.setClip(clipShape);
            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();
        }
    }

    public static class PixelDot implements Dot {

        private Color color;
        private Point2D imageLocation;
        private Rectangle dot = new Rectangle(0, 0, 1, 1);

        public PixelDot(Point2D pixelPoint, Color color) {
            this.color = color;
            this.imageLocation = pixelPoint;
        }

        public Point2D getImageLocation() {
            return imageLocation;
        }

        public void paint(Graphics2D g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.fill(dot);
            g2d.dispose();
        }
    }

    public static class Point2DRange extends Range<Point2D> {

        public Point2DRange(Point2D from, Point2D to) {
            super(from, to);
        }

        public double getXDistance() {
            return getTo().getX() - getFrom().getX();
        }

        public double getYDistance() {
            return getTo().getY() - getFrom().getY();
        }

        protected double getFromX() {
            return getFrom().getX();
        }

        protected double getFromY() {
            return getFrom().getY();
        }

        @Override
        public Point2D valueAt(double progress) {
            double xDistance = getXDistance();
            double yDistance = getYDistance();

            double xValue = Math.round((double) xDistance * progress);
            double yValue = Math.round((double) yDistance * progress);

            xValue += getFromX();
            yValue += getFromY();

            return new Point2D.Double(xValue, yValue);
        }

    }

    public static class DurationRange extends Range<Duration> {

        public DurationRange(Duration from, Duration to) {
            super(from, to);
        }

        public Duration getDistance() {
            return Duration.ofMillis(getTo().toMillis() - getFrom().toMillis());
        }

        public Duration valueAt(double progress) {
            Duration distance = getDistance();
            long value = (long) Math.round((double) distance.toMillis() * progress);
            value += getFrom().toMillis();
            return Duration.ofMillis(value);
        }
    }

    public final class ImageUtil {

        public static BufferedImage pixelate(BufferedImage imageToPixelate, int pixelSize) {
            BufferedImage pixelateImage = new BufferedImage(
                    imageToPixelate.getWidth(),
                    imageToPixelate.getHeight(),
                    imageToPixelate.getType());

            for (int y = 0; y < imageToPixelate.getHeight(); y += pixelSize) {
                for (int x = 0; x < imageToPixelate.getWidth(); x += pixelSize) {
                    BufferedImage croppedImage = getCroppedImage(imageToPixelate, x, y, pixelSize, pixelSize);
                    Color dominantColor = getDominantColor(croppedImage);
                    for (int yd = y; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()); yd++) {
                        for (int xd = x; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()); xd++) {
                            pixelateImage.setRGB(xd, yd, dominantColor.getRGB());
                        }
                    }
                }
            }

            return pixelateImage;
        }

        public static BufferedImage getCroppedImage(BufferedImage image, int startx, int starty, int width, int height) {
            if (startx < 0) {
                startx = 0;
            }
            if (starty < 0) {
                starty = 0;
            }
            if (startx > image.getWidth()) {
                startx = image.getWidth();
            }
            if (starty > image.getHeight()) {
                starty = image.getHeight();
            }
            if (startx + width > image.getWidth()) {
                width = image.getWidth() - startx;
            }
            if (starty + height > image.getHeight()) {
                height = image.getHeight() - starty;
            }
            return image.getSubimage(startx, starty, width, height);
        }

        public static Color getDominantColor(BufferedImage image) {
            int sumR = 0, sumB = 0, sumG = 0, sum2 = 0;
            int color = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    color = image.getRGB(x, y);
                    Color c = new Color(color);
                    sumR += c.getRed();
                    sumB += c.getBlue();
                    sumG += c.getGreen();
                    sum2++;
                }
            }
            return new Color(sumR / sum2, sumG / sum2, sumB / sum2);
        }
    }
}
