/*
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Mandelbrot {
    static int iterations = 0;
    static int resolution = 1000;
    static int gridBounds = 2;
    static double[][] points;
    static double[] z = {0,0};

    static final int rowWidth = (gridBounds* resolution * 2 + 1) ;
    static BufferedImage image = new BufferedImage(rowWidth,rowWidth,BufferedImage.TYPE_INT_RGB);

    public static void main(String[] args) throws IOException {

        double[] c = {-1,5};
        double[] result = iterate(z,c);
        //System.out.println("r: " + result[0] + " | " + " i:" + result[1] );


        createPoints();

    }

    public static void createPoints() throws IOException {
        double x = -gridBounds;
        double y = gridBounds;
        final double negativeXBound = x;


        System.out.println("row width: " + rowWidth);

        points = new double[rowWidth * rowWidth][2];
        //System.out.println(points.length);

        int column = 0;
        int row = 0;

        for (int i = 1; i <= points.length; i++) {
            double[] coords = {x, y};
            if (isMandelbrot(x,y)) {
                Color black = new Color(0,0,0);
                int rgb = black.getRGB();
                System.out.println("column: " + column +  " | row: " + row);
                image.setRGB(column, row, rgb);
            } else {
                Color white = new Color(255,255,255);
                int rgb = white.getRGB();
                System.out.println("column: " + column +  " | row: " + row);
                image.setRGB(column,row,rgb);
            }
            points[i-1] = coords;

            //System.out.println(round(x, 1) + " | " + round(y, 1) + " | I: " + (i-1));
            if (i % rowWidth == 0) {
                y = y - 1.0 / resolution;
                x = negativeXBound;
                column = 0;
                row++;
                //System.out.println("reset at index " + i + " | " + rowWidth);
            } else {
                column++;
                x = x + 1.0 / resolution;
            }



        }

        System.out.println(image.getRGB(5,5));
        File file = new File("saved.png");
        ImageIO.write(image, "png", file);

        for (double[] point : points) {
            //System.out.println(round(point[0], 1) + " | " + round(point[1], 1) + " | I: ");
        }
    }

    static double distanceFromOrigin(double r, double i) {
        double rSquared = r * r;
        double iSquared = i * i;
        return Math.sqrt(rSquared + iSquared);
    }



    static boolean isMandelbrot(double x, double y) {
        double[] c = {x,y};

        double[] result = iterate(z,c);
        if (Double.isInfinite(result[0]) || Double.isInfinite(result[1])) return false;
        double distance = distanceFromOrigin(result[0], result[1]);
        //System.out.println(distance);
        return distanceFromOrigin(result[0], result[1]) < 2.0;
   }



    static double[] iterate(double[] z, double[] c) {
        double zRealResult = z[0] * z[0];
        double zImaginaryResult = z[0] * z[1] * 2 + z[1] * z[1];
        double[] result = {zRealResult + c[0], zImaginaryResult + c[1]};

        if (Double.isInfinite(result[0]) || Double.isInfinite(result[1])) return result;

        //System.out.println("r: " + result[0] + " | " + " i:" + result[1] );

        if (iterations < 30) {
            iterations++;
            return iterate(result, c);
        }

        return result;
    }

    //DEBUG
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
