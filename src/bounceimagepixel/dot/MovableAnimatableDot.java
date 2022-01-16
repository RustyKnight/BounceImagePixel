/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.dot;

import bounceimagepixel.animation.Point2DRange;
import bounceimagepixel.dot.Dot;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.time.Duration;
import org.kaizen.animation.Animatable;
import org.kaizen.animation.AnimatableDuration;
import org.kaizen.animation.AnimatableDurationListenerAdapter;
import org.kaizen.animation.DefaultAnimatableDuration;
import org.kaizen.animation.curves.Curves;

/**
 *
 * @author shane.whitehead
 */
public class MovableAnimatableDot extends RandimisedDurationAnimatableDot {
    
    Point2DRange bounceRange;
    Point2D currentLocation;
    private Animatable bounceAnimatable;
    private Animatable imageAnimatable;
    Point2DRange animationRange;
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
        if (getState() == State.RUN) {
            startBounceAnimation();
        } else {
            startImageAnimation();
        }
    }

    protected void makeBounceAnimatable() {
        bounceAnimatable = new DefaultAnimatableDuration(getAnimationDuration(), Curves.QUART_IN_OUT.getCurve(), new AnimatableDurationListenerAdapter() {
            @Override
            public void animationCompleted(Animatable animator) {
                if (getState() == State.IMAGE) {
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
        if (getState() != State.RUN) {
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
        if (getState() != State.IMAGE) {
            return;
        }
        if (imageAnimatable.isRunning()) {
            return;
        }
        imageAnimatable.start();
    }
    
}
