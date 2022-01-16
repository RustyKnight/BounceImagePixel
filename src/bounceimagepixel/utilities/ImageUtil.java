/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bounceimagepixel.utilities;

import bounceimagepixel.Main;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author shane.whitehead
 */
public final class ImageUtil {
    
    public static BufferedImage pixelate(BufferedImage imageToPixelate, int pixelSize) {
        BufferedImage pixelateImage = new BufferedImage(imageToPixelate.getWidth(), imageToPixelate.getHeight(), imageToPixelate.getType());
        for (int y = 0; y < imageToPixelate.getHeight(); y += pixelSize) {
            for (int x = 0; x < imageToPixelate.getWidth(); x += pixelSize) {
                BufferedImage croppedImage = getCroppedImage(imageToPixelate, x, y, pixelSize, pixelSize);
                Color dominantColor = getDominantColor(croppedImage);
                for (int yd = y; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()); yd++) {
                    for (int xd = x; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()); xd++) {
                        pixelateImage.setRGB(xd, yd, dominantColor.getRGB());
                    }
                }
            }
        }
        return pixelateImage;
    }

    public static BufferedImage getCroppedImage(BufferedImage image, int startx, int starty, int width, int height) {
        if (startx < 0) {
            startx = 0;
        }
        if (starty < 0) {
            starty = 0;
        }
        if (startx > image.getWidth()) {
            startx = image.getWidth();
        }
        if (starty > image.getHeight()) {
            starty = image.getHeight();
        }
        if (startx + width > image.getWidth()) {
            width = image.getWidth() - startx;
        }
        if (starty + height > image.getHeight()) {
            height = image.getHeight() - starty;
        }
        return image.getSubimage(startx, starty, width, height);
    }

    public static Color getDominantColor(BufferedImage image) {
        int sumR = 0;
        int sumB = 0;
        int sumG = 0;
        int sum2 = 0;
        int color = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                color = image.getRGB(x, y);
                Color c = new Color(color);
                sumR += c.getRed();
                sumB += c.getBlue();
                sumG += c.getGreen();
                sum2++;
            }
        }
        return new Color(sumR / sum2, sumG / sum2, sumB / sum2);
    }
    private final Main outer;

    public ImageUtil(final Main outer) {
        this.outer = outer;
    }
    
}
