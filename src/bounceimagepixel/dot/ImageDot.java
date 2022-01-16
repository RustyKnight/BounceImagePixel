/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.dot;

import bounceimagepixel.dot.Dot;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author shane.whitehead
 */
public class ImageDot implements Dot {
    
    Color color;
    Point2D imageLocation;
    private Shape clipShape;
    BufferedImage img;

    public ImageDot(Point2D pixelPoint, BufferedImage img) {
        this.color = color;
        this.imageLocation = pixelPoint;
        this.img = img;
        int width = img.getWidth();
        int height = img.getHeight();
        clipShape = new RoundRectangle2D.Double(0, 0, width, height, width / 4, height / 4);
    }

    public Point2D getImageLocation() {
        return imageLocation;
    }

    public void paint(Graphics2D g) {
        Graphics2D g2d = (Graphics2D) g.create();
        //            g2d.setColor(Color.WHITE);
        //            g2d.draw(clipShape);
        g2d.setClip(clipShape);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
    }
    
}
