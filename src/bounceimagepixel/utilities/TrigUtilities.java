/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.utilities;

import java.awt.geom.Point2D;

/**
 *
 * @author shane.whitehead
 */
public class TrigUtilities {
    
    public static Point2D pointOnCircle(double degress, double radius) {
        double rads = Math.toRadians(degress - 90); // 0 becomes the top
        double xPosy = Math.round(Math.cos(rads) * radius);
        double yPosy = Math.round(Math.sin(rads) * radius);
        return new Point2D.Double(radius + xPosy, radius + yPosy);
    }
    
}
