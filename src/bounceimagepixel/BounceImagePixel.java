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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
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
import org.kaizen.animation.AnimatableListener;
import org.kaizen.animation.DefaultAnimatable;
import org.kaizen.animation.DefaultAnimatableDuration;
import org.kaizen.animation.curves.Curves;
import org.kaizen.animation.ranges.DoubleRange;
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

        private DefaultAnimatable animatable = new DefaultAnimatable(new AnimatableListener() {
            @Override
            public void animationChanged(Animatable animator) {
                repaint();
            }

            @Override
            public void animationStarted(Animatable animator) {
            }

            @Override
            public void animationStopped(Animatable animator) {
            }
        });

        public TestPane(BufferedImage img) {
            setBackground(Color.BLACK);

            Random rnd = new Random();

            int xOffset = (int) (diameter - img.getWidth()) / 2;
            int yOffset = (int) (diameter - img.getWidth()) / 2;

            AnimatableDot.Observer dotObserver = new AnimatableDot.Observer() {
                @Override
                public void dotAnimationDidComplete(AnimatableDot source) {
                    dotsThatHaveCompleted += 1;
                    stopIfAllDotsCompleted();
                }
            };

            int pixel = 0;
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    pixel += 1;
                    Color color = new Color(img.getRGB(x, y), true);
                    Point2D pixelLocation = new Point2D.Double(x + xOffset, y + yOffset);
                    Dot dot = new Dot(pixelLocation, color);
//                    if (pixel % 100 == 0) {
                    double fromAngle = 360.0 * rnd.nextDouble();

                    double angleRange = (180 * rnd.nextDouble()) * (rnd.nextBoolean() ? 1 : -1);

                    double toAngle = fromAngle + angleRange;

                    Point2D fromPoint = Utilities.pointOnCircle(fromAngle, radius);
                    Point2D toPoint = Utilities.pointOnCircle(toAngle, radius);

                    Point2DRange animationRange = new Point2DRange(fromPoint, toPoint);

                    MovableAnimatableDot animatable = new MovableAnimatableDot(dot, animationRange, dotObserver);
                    dots.add(animatable);
//                    } else {
//                        FadableAnimatableDot animatable = new FadableAnimatableDot(dot, delay, dotObserver);
//                        dots.add(animatable);
//                    }
                }
            }
            
            Collections.shuffle(dots);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    animatable.stop();
                    dotsThatHaveCompleted = 0;
                    for (AnimatableDot dot : dots) {
                        switch (dot.getState()) {
                            case IMAGE:
                                dot.setState(AnimatableDot.State.RUN);
                                break;
                            case RUN:
                                dot.setState(AnimatableDot.State.IMAGE);
                                break;
                        }
                    }
                    animatable.start();
                }
            });
        }

        private int dotsThatHaveCompleted = 0;

        protected void stopIfAllDotsCompleted() {
            if (animatable.isAnimating()) {
                if (dotsThatHaveCompleted >= dots.size()) {
                    animatable.stop();
                }
            }
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 800);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            double diameter = radius * 2;

            double xOffset = (getWidth() - diameter) / 2d;
            double yOffset = (getHeight() - diameter) / 2d;
            g2d.translate(xOffset, yOffset);
            g2d.setColor(Color.DARK_GRAY);
            g2d.draw(new Ellipse2D.Double(0, 0, diameter, diameter));
            for (AnimatableDot dot : dots) {
                dot.paint(g2d);
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

        // This is left here because undoing is going to break my brain
        protected void initialiseAndStartAnimation() {
            startAnimation();
        }

        protected abstract void stateDidChange();

        protected abstract void startAnimation();

    }
    
    public static abstract class RandimisedDurationAnimatableDot extends AbstractAnimatableDot {

        // Randomise the bounce duration on each cycle
        private Random rnd = new Random();
        private DurationRange delayRange = new DurationRange(Duration.ofSeconds(1), Duration.ofSeconds(5));

        public RandimisedDurationAnimatableDot(Dot dot, Observer observer) {
            super(dot, observer);
        }

        @Override
        public Duration getAnimationDuration() {
            return delayRange.valueAt(rnd.nextDouble());
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
                    stopImageAnimation();
                    isFirstBouncePass = true;
                    animationRange = new Point2DRange(currentLocation, bounceRange.getTo());
                    initialiseAndStartAnimation();
                    break;
            }
        }

        protected void preRollImageAnimation() {
            stopBounceAnimation();
            if (currentLocation == getDot().getImageLocation()) {
                stopImageAnimation();
                return;
            }
            animationRange = new Point2DRange(currentLocation, getDot().getImageLocation());
            startAnimation();
        }

        protected void stopBounceAnimation() {
            if (bounceAnimatable != null) {
                stopAnimation(bounceAnimatable);
                bounceAnimatable = null;
            }
        }

        protected void stopImageAnimation() {
            stopAnimation(imageAnimatable);
            imageAnimatable = null;
        }

        protected void stopAnimation(Animatable animatable) {
            if (bounceAnimatable != null) {
                animatable.stop();
                animatable = null;
            }
        }

        @Override
        protected void startAnimation() {
            if (getState() == RUN) {
                startBounceAnimation();
            } else {
                startImageAnimation();
            }
        }

        protected void startBounceAnimation() {
            if (getState() != RUN) {
                return;
            }
            stopBounceAnimation();
            bounceAnimatable = new DefaultAnimatableDuration(getAnimationDuration(), Curves.QUART_IN_OUT.getCurve(), new AnimatableDurationListenerAdapter() {

                @Override
                public void animationCompleted(Animatable animator) {
                    if (getState() == IMAGE && imageAnimatable == null) {
                        preRollImageAnimation();
                        return;
                    }
                    currentLocation = animationRange.valueAt(1);
                    stopBounceAnimation();
                    if (isFirstBouncePass) {
                        isFirstBouncePass = false;
                        animationRange = bounceRange;
                    }
                    animationRange.reverse();
                    initialiseAndStartAnimation();
                }

                @Override
                public void animationTimeChanged(AnimatableDuration animatable) {
                    double progress = animatable.getProgress();
                    currentLocation = animationRange.valueAt(progress);
                }
            });
            bounceAnimatable.start();
        }

        protected void startImageAnimation() {
            if (getState() != IMAGE) {
                return;
            }
            if (imageAnimatable != null) {
                return;
            }
            imageAnimatable = new DefaultAnimatableDuration(getAnimationDuration(), Curves.QUART_OUT.getCurve(), new AnimatableDurationListenerAdapter() {
                @Override
                public void animationCompleted(Animatable animator) {
                    currentLocation = animationRange.valueAt(1);
                    stopImageAnimation();
                    getObserver().dotAnimationDidComplete(MovableAnimatableDot.this);
                }

                @Override
                public void animationTimeChanged(AnimatableDuration animatable) {
                    double progress = animatable.getProgress();
                    currentLocation = animationRange.valueAt(progress);
                }
            });
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
            Point2D location = dot.imageLocation;
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
                    initialiseAndStartAnimation();
                    break;
                case RUN:
                    animationRange = fadeOutRange;
                    initialiseAndStartAnimation();
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

    public static class Dot {

        private Color color;
        private Point2D imageLocation;
        private Rectangle dot = new Rectangle(0, 0, 1, 1);

        public Dot(Point2D pixelPoint, Color color) {
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
            return Duration.ofNanos(getTo().toNanos() - getFrom().toNanos());
        }

        public Duration valueAt(double progress) {
            Duration distance = getDistance();
            long value = (long) Math.round((double) distance.toNanos() * progress);
            value += getFrom().getNano();
            return Duration.ofNanos(value);
        }
    }
}
