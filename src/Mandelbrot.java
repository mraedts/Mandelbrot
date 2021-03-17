/*
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
 */

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Mandelbrot {
    static int iterations = 0;
    static int resolution = 10;
    static int gridBounds = 2;
    static double[][] points;

    public static void main(String[] args) {
        // double[] z = {0,0};
        // double[] c = {-0.75,0.1};
        // iterate(z,c);
        createPoints();
    }



    public static void createPoints() {
        double x = -gridBounds;
        double y = gridBounds;
        final double negativeXBound = x;

        points = new double[resolution * resolution * gridBounds * gridBounds][2];
        System.out.println(points.length);

        for (int i = 0; i < points.length; i++) {
            double[] coords = {x, y};
            points[i] = coords;

            System.out.println(round(x, 1 ) + " | " + round(y, 1) + " | I: ");

            if ( i != 0) {
                if (i % (resolution * gridBounds * 2) == 0) {
                    y = y - 1.0 / resolution;
                    x = negativeXBound;
                    System.out.println("reset at index " + i + " | " + resolution * gridBounds * 2);
                } else {
                    x = x + 1.0 / resolution;
                }
            } else x = x + 1.0 / resolution;
        }

        for (int i = 0; i < points.length; i++) {
            //System.out.println(round(points[i][0], 1 ) + " | " + round(points[i][1], 1) + " | I: ");
        }
    }

    static double distanceFromOrigin(double r, double i) {
        double rSquared = r * r;
        double iSquared = i * i;
        return Math.sqrt(rSquared + iSquared);
    }

    //DEBUG
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    static double[] iterate(double[] z, double[] c) {
        double zRealResult = z[0] * z[0];
        double zImaginaryResult = z[0] * z[1] * 2 + z[1] * z[1];
        double[] result = {zRealResult + c[0], zImaginaryResult + c[1]};

        if (iterations < 30) {
            iterations++;
            return iterate(result, c);
        }

        return result;
    }
}
