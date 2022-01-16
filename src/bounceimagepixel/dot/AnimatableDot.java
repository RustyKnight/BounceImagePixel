/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.dot;

import java.awt.Graphics2D;

/**
 *
 * @author shane.whitehead
 */
public interface AnimatableDot {

    public static enum State {
        RUN, IMAGE
    }

    public static interface Observer {
        public void dotAnimationDidComplete(AnimatableDot source);
    }

    public void setState(State state);
    public State getState();
    public void paint(Graphics2D g2d);
    public Observer getObserver();
    
}
