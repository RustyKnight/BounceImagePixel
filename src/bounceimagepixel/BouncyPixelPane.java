/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel;

import bounceimagepixel.animation.Point2DRange;
import bounceimagepixel.dot.PixelDot;
import bounceimagepixel.dot.MovableAnimatableDot;
import bounceimagepixel.utilities.TrigUtilities;
import bounceimagepixel.utilities.ImageUtil;
import bounceimagepixel.dot.ImageDot;
import bounceimagepixel.dot.AnimatableDot;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import org.kaizen.animation.Animatable;
import org.kaizen.animation.AnimatableDuration;
import org.kaizen.animation.AnimatableDurationListenerAdapter;
import org.kaizen.animation.AnimatableListenerAdapter;
import org.kaizen.animation.DefaultAnimatable;
import org.kaizen.animation.DefaultAnimatableDuration;
import org.kaizen.animation.curves.Curves;
import org.kaizen.animation.ranges.FloatRange;

/**
 *
 * @author shane.whitehead
 */
public class BouncyPixelPane extends JPanel {
    
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
    private AnimatableDuration transitionAnimation = new DefaultAnimatableDuration(Duration.ofSeconds(1), Curves.SINE_IN.getCurve(), new AnimatableDurationListenerAdapter() {
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

    public BouncyPixelPane(BufferedImage img) {
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
    } //            preparePixelDots(img, dotObserver);

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
                Point2D fromPoint = TrigUtilities.pointOnCircle(fromAngle, radius);
                Point2D toPoint = TrigUtilities.pointOnCircle(toAngle, radius);
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
                Point2D fromPoint = TrigUtilities.pointOnCircle(fromAngle, radius);
                Point2D toPoint = TrigUtilities.pointOnCircle(toAngle, radius);
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
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
