/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.dot;

import bounceimagepixel.dot.Dot;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 *
 * @author shane.whitehead
 */
public class PixelDot implements Dot {
    
    Color color;
    Point2D imageLocation;
    private Rectangle dot = new Rectangle(0, 0, 1, 1);

    public PixelDot(Point2D pixelPoint, Color color) {
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
