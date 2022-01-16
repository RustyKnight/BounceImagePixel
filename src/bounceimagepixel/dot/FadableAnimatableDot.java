/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.dot;

import bounceimagepixel.dot.Dot;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import org.kaizen.animation.Animatable;
import org.kaizen.animation.AnimatableDuration;
import org.kaizen.animation.AnimatableDurationListenerAdapter;
import org.kaizen.animation.DefaultAnimatableDuration;
import org.kaizen.animation.curves.Curves;
import org.kaizen.animation.ranges.DoubleRange;

/**
 *
 * @author shane.whitehead
 */
public class FadableAnimatableDot extends RandimisedDurationAnimatableDot {
    
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
