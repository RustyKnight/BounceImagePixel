/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.dot;

import java.time.Duration;
import javax.swing.Timer;

/**
 *
 * @author shane.whitehead
 */
public abstract class AbstractAnimatableDot implements AnimatableDot {
    
    State state = State.IMAGE;
    Dot dot;
    Observer observer;

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
