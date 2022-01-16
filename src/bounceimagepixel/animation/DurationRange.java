/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.animation;

import java.time.Duration;
import org.kaizen.animation.ranges.Range;

/**
 *
 * @author shane.whitehead
 */
public class DurationRange extends Range<Duration> {
    
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
