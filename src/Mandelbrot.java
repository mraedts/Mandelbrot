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
    static int resolution = 1;
    static int gridBounds = 1;
    static double[][] points;
    static double[] z = {0,0};

    public static void main(String[] args) {

        double[] c = {-1,5};
        double[] result = iterate(z,c);
        System.out.println("r: " + result[0] + " | " + " i:" + result[1] );

        //createPoints();

    }



    public static void createPoints() {
        double x = -gridBounds;
        double y = gridBounds;
        final double negativeXBound = x;

        final int rowWidth = (gridBounds* resolution * 2 + 1) ;
        //System.out.println("row width: " + rowWidth);

        points = new double[rowWidth * rowWidth][2];
        //System.out.println(points.length);

        for (int i = 1; i <= points.length; i++) {
            double[] coords = {x, y};
            System.out.println(isMandelbrot(x,y));
            points[i-1] = coords;

            //System.out.println(round(x, 1) + " | " + round(y, 1) + " | I: " + i);
            if (i % rowWidth == 0) {
                y = y - 1.0 / resolution;
                x = negativeXBound;
                //System.out.println("reset at index " + i + " | " + rowWidth);
            } else {
                x = x + 1.0 / resolution;
            }
        }

        for (int i = 0; i < points.length; i++) {
            System.out.println(round(points[i][0], 1 ) + " | " + round(points[i][1], 1) + " | I: ");
        }
    }

    static double distanceFromOrigin(double r, double i) {
        double rSquared = r * r;
        double iSquared = i * i;
        return Math.sqrt(rSquared + iSquared);
    }



    static boolean isMandelbrot(double x, double y) {
        double[] c = {-0.75,0.1};

        double[] result = iterate(z,c);
        if (Double.isInfinite(result[0]) || Double.isInfinite(result[1])) return false;
        double distance = distanceFromOrigin(result[0], result[1]);
        System.out.println(distance);
        return distanceFromOrigin(result[0], result[1]) < 2.0;
   }



    static double[] iterate(double[] z, double[] c) {
        double zRealResult = z[0] * z[0];
        double zImaginaryResult = z[0] * z[1] * 2 + z[1] * z[1];
        double[] result = {zRealResult + c[0], zImaginaryResult + c[1]};

        if (Double.isInfinite(result[0]) || Double.isInfinite(result[1])) return result;

        System.out.println("r: " + result[0] + " | " + " i:" + result[1] );

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
