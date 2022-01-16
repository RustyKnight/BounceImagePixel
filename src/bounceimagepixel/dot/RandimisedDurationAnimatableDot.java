/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.dot;

import bounceimagepixel.animation.DurationRange;
import java.time.Duration;
import java.util.Random;

/**
 *
 * @author shane.whitehead
 */
public abstract class RandimisedDurationAnimatableDot extends AbstractAnimatableDot {
    
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
