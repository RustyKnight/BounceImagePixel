/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.dot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 *
 * @author shane.whitehead
 */
public interface Dot {

    public Point2D getImageLocation();

    public void paint(Graphics2D g);
    
}
