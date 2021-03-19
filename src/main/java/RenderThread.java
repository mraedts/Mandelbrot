import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class RenderThread extends Thread {
    //settings
    int width = 4000;
    int height = 4000;
    int iterations = 0;
    int maxIterations = 1500;

    double xTarget;
    double yTarget;
    double zoom;
    String imageNumber;
    Mandelbrot.InstructionSet instructionSet;
    double displayedPercentage = 0.0;

    @Override
    public void run() {
        super.run();
        for (int i = 0; i < instructionSet.frameInstructions.size(); i++) {
            Mandelbrot.FrameInstruction instructions = instructionSet.frameInstructions.get(i);
            try {
                renderOnTarget(instructionSet.xTarget, instructionSet.yTarget,  instructions.zoom,  instructions.imageNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public RenderThread(Mandelbrot.InstructionSet instructionSet) throws IOException {
        this.instructionSet =  instructionSet;
    }

    public class CalculationResult {
        double[] solution;
        int iterations;

        public CalculationResult(double[] solution, int iterations) {
            this.solution = solution;
            this.iterations = iterations;
        }
    }

    public class GridLimits {
        double xMin;
        double yMax;
        double xMax;
        double yMin;

        public GridLimits(double xTarget, double yTarget, double zoom) {
            this.xMin = xTarget - (double)width / (2 * zoom) ;
            this.yMax = yTarget + (double)height / (2 * zoom)  ;
            this.yMin = yTarget - (double)height / (2 * zoom) ;
            this.xMax = xTarget + (double)width / (2 * zoom)  ;
        }
    }

    public void renderOnTarget(double xTarget, double yTarget, double zoom, String imageNumber) throws IOException {
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        GridLimits limits = new GridLimits(xTarget, yTarget, zoom);

        double x = limits.xMin;
        double y = limits.yMax;

        int column = 0;
        int row = 0;

        double xStep = Math.abs(limits.xMax - limits.xMin) / (double)width;
        double yStep = Math.abs(limits.yMax - limits.yMin) / (double) height;

        for (int i = 1; i <= width * height;i++) {
            double[] coords = {x, y};
            CalculationResult result = iterate(coords);

            if (result.iterations == maxIterations) {
                Color black = new Color(0,0,0);
                int rgb = black.getRGB();

                image.setRGB(column, row, rgb);
            } else {
                Color white = determineColor(result.iterations);
                int rgb = white.getRGB();

                image.setRGB(column,row,rgb);
            }

            if (i % width == 0) {
                y = y - yStep;
                x = limits.xMin;
                column = 0;
                row++;
            } else {
                column++;
                x = x + xStep;
            }

            /*
            double percentage = (((double)i + 1.0) / (double)(width * height)) * 100;
            percentage = round(percentage, 1);
            if (percentage != displayedPercentage) {
                System.out.println( percentage + "%");
                displayedPercentage = percentage;
            }
             */
        }

        File file = new File("./img/" + imageNumber +".png");
        ImageIO.write(image, "png", file);
    }

    public Color determineColor(int iterationsToInfinity) {
        double iterationPercentage = (double)iterationsToInfinity / (double)maxIterations;
        int red = (int)round(iterationPercentage * 255 + 0, 0);
        if (red > 255) red = 255;
        return new Color(red,0,0);
    }


    public  double[] solve(double r, double i, double[] c) {
        double zRealComponent = r * r - i * i;
        double zImaginaryComponent = 2 * r * i;
        return new double[]{zRealComponent + c[0], zImaginaryComponent + c[1]};
    }

    public  CalculationResult iterate(double[] c) {
        int iterations = 0;
        double[] z = {0,0};

        while (Double.isFinite(z[0]) && Double.isFinite(z[1]) && iterations < maxIterations) {
            z = solve(z[0], z[1], c);
            iterations++;
        }
        return new CalculationResult(z, iterations);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
