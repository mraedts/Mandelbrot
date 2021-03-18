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
    //static double[][] points;
    static double[] z = {0,0};
    static double displayedPercentage = 0.0;

    static final int rowWidth = (gridBounds* resolution * 2 + 1) ;
    static BufferedImage image = new BufferedImage(rowWidth,rowWidth,BufferedImage.TYPE_INT_RGB);

    public static void main(String[] args) throws IOException {
        createPoints();
    }

    public static void createPoints() throws IOException {
        double x = -gridBounds;
        double y = gridBounds;
        final double negativeXBound = x;
        System.out.println("row width: " + rowWidth);
        //points = new double[rowWidth * rowWidth][2];
        //System.out.println(points.length);
        int column = 0;
        int row = 0;

        for (int i = 1; i <= rowWidth * rowWidth; i++) {
            double[] coords = {x, y};
            if (isMandelbrot(coords)) {
                Color black = new Color(0,0,0);
                int rgb = black.getRGB();
                //System.out.println("column: " + column +  " | row: " + row + " true");
                image.setRGB(column, row, rgb);
            } else {
                Color white = new Color(255,255,255);
                int rgb = white.getRGB();
                //System.out.println("column: " + column +  " | row: " + row+ " false");
                image.setRGB(column,row,rgb);
            }
            //points[i-1] = coords;

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
            double percentage = (((double)i + 1.0) / (double)(rowWidth * rowWidth)) * 100;
            percentage = round(percentage, 1);
            if (percentage != displayedPercentage) {
                System.out.println( percentage + "%");
                displayedPercentage = percentage;
            }

        }
        System.out.println(image.getRGB(5,5));
        File file = new File("saved.png");
        ImageIO.write(image, "png", file);


    }
    static double distanceFromOrigin(double r, double i) {
        double rSquared = r * r;
        double iSquared = i * i;
        return Math.sqrt(rSquared + iSquared);
    }

    public static boolean isMandelbrot(double[] c) {
        double[] result = iterate(c);
        if (Double.isFinite(result[0]) && Double.isFinite(result[1])) return true;
        else return false;
    }

    public static Color determineColor(int iterations) {
        return null;
    }


    public static double[] solve(double r, double i, double[] c) {
        double zRealComponent = r * r - i * i;
        double zImaginaryComponent = 2 * r * i;
        double[] solution = {zRealComponent + c[0], zImaginaryComponent + c[1]};
        //System.out.println("r: " + solution[0] + " | i: " + solution[1]);
        return solution;
    }

    public static double[] iterate(double[] c) {
        int iterations = 0;
        double[] currentIterationResult = {0,0};
        double[] z = {0,0};

        while (Double.isFinite(z[0]) && Double.isFinite(z[1]) && iterations < 1000) {
            z = solve(z[0], z[1], c);
            iterations++;
        }

        return z;
    }




    //DEBUG
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
