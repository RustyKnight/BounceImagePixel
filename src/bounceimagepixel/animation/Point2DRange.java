/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.animation;

import java.awt.geom.Point2D;
import org.kaizen.animation.ranges.Range;

/**
 *
 * @author shane.whitehead
 */
public class Point2DRange extends Range<Point2D> {
    
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
