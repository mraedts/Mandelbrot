package main.java;

import java.awt.*;
import java.util.concurrent.Callable;

class Result {
    Color color;
    int column, row;

    public Result(Color color, int column, int row) {
        this.color = color;
        this.column = column;
        this.row = row;
    }
}

public class RenderThread implements Callable<Result[]> {
    int maxIterations;
    Mandelbrot.InstructionSet instructionSet;
    String colorType;
    double renderProgress;
    int colorArraySize = 100;
    int[][] colors = new int[colorArraySize][3];

    public RenderThread(int maxIterations, Mandelbrot.InstructionSet instructionSet, String colorType, double renderProgress) {
        this.maxIterations = maxIterations;
        this.instructionSet = instructionSet;
        this.colorType = colorType;
        this.renderProgress = renderProgress;
    }

    @Override
    public Result[] call() throws Exception {

        Result[] results = new Result[instructionSet.instructions.size()];

        for (int i = 0; i < instructionSet.instructions.size(); i++) {
            Mandelbrot.Instruction instruction = instructionSet.instructions.get(i);
            //System.out.println(instruction.x + " | " + instruction.y);
            CalculationResult result = iterate(new double[] {instruction.x,instruction.y});
            Color color;

            if (colorType.equals("HSB")) color = getColor(result.iterations);
            else if (colorType.equals("Ultra Fractal")) color = getUltraFractalColor(result.iterations);
            else throw new IllegalArgumentException("Wrong color type entered! Options: HSB || Ultra Fractal");
            results[i] = new Result(color,instruction.column, instruction.row);
        }
        return results;
    }

    public class CalculationResult {
        double[] solution;
        int iterations;

        public CalculationResult(double[] solution, int iterations) {
            this.solution = solution;
            this.iterations = iterations;
        }
    }

    public  double[] solve(double r, double i, double[] c) {
        double zRealComponent = r * r - i * i;
        double zImaginaryComponent = 2 * r * i;
        return new double[]{zRealComponent + c[0], zImaginaryComponent + c[1]};
    }

    public CalculationResult iterate(double[] c) {
        int iterations = 0;
        double r = 0, i = 0, w = 0;

        while (r + i < 4 && iterations < maxIterations) {
            double x = r - i + c[0];
            double y = w - r - i + c[1];
            r = x * x;
            i = y * y;
            w = (x+y) * (x+y);

            iterations++;
        }
        //System.out.println("r:  " + c[0] + "|  i: " + c[1] +  "| iterations: " + iterations);
        return new CalculationResult(new double[]{r,i}, iterations);
    }

    public class ColorPoint {
        int r,g,b;
        double percent;

        public ColorPoint(int r, int g, int b, double percent) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.percent = percent;
        }
    }

    public double interpolate(double[] pointA, double[] pointB, double percent) {
        double xDiff = pointB[0] - pointA[0];
        double yDiff = pointB[1] - pointA[1];
        double slope = yDiff / xDiff;
        double yIntercept = pointA[1] - pointA[0] * slope;

        return (slope * percent + yIntercept) ;
    }

    public Color getUltraFractalColor(int iterationsToInfinity) {
        if (iterationsToInfinity == maxIterations) return new Color(0,0,0);
        double iterationPercentage = (double)iterationsToInfinity / (double)maxIterations;

        ColorPoint[] colorPoints = new ColorPoint[6];

        colorPoints[0] = new ColorPoint(0,   7, 100, 0);
        colorPoints[1] = new ColorPoint( 32, 107, 203, 0.16);
        colorPoints[2] = new ColorPoint(237, 255, 255, 0.42);
        colorPoints[3] = new ColorPoint(255, 170,   0, 0.6425);
        colorPoints[4] = new ColorPoint(0,   2,   0, 0.8575);
        colorPoints[5] = new ColorPoint(0,   7, 100, 1);


        ColorPoint min = null;
        ColorPoint max = null;

        for (int i = 0; i < colorPoints.length;i++) {
            if (i == colorPoints.length - 1) {
                min = colorPoints[i-1];
                max = colorPoints[i];
            }
            else if (iterationPercentage >= colorPoints[i].percent && iterationPercentage < colorPoints[i+1].percent) {
                min = colorPoints[i];
                max = colorPoints[i + 1];
                break;
            }
        }
        assert min != null;

        double r = interpolate(new double[]{min.percent, min.r}, new double[]{max.percent, max.r}, iterationPercentage);
        double g = interpolate(new double[]{min.percent, min.g}, new double[]{max.percent, max.g}, iterationPercentage);
        double b = interpolate(new double[]{min.percent, min.b}, new double[]{max.percent, max.b}, iterationPercentage);

        int red = (int)Math.round(r);
        int green = (int)Math.round(g);
        int blue = (int)Math.round(b);

        return new Color(red,green,blue);
    }

    public Color getColor(int iterationsToInfinity) {
        if (iterationsToInfinity == maxIterations) return new Color(0,0,0);
        double iterationPercentage = (double)iterationsToInfinity / (double)maxIterations;
        int color =  Color.HSBtoRGB(0.91f + 10 * (float)iterationPercentage ,0.8f,1.0f);
        return new Color(color);
    }

    public Color getColorFromArray(int iterations, double r, double i) {
        double smoothed = log2(log2(r * r + i * i) / 2);  // log_2(log_2(|p|))
        int colorI = (int)(Math.sqrt(i + 10 - smoothed) * 256) % colorArraySize;

        int red = colors[colorI][0];
        int green = colors[colorI][1];
        int blue = colors[colorI][2];

        return new Color(red,green,blue);
    }

    public static double log2(double N)
    {

        // calculate log2 N indirectly
        // using log() method
        int result = (int)(Math.log(N) / Math.log(2));

        return result;
    }

    public void preComputeColorArray(int loops) {
        System.out.println("computing array...");
        ColorPoint[] colorPoints = new ColorPoint[6];

        colorPoints[0] = new ColorPoint(0,   7, 100, 0);
        colorPoints[1] = new ColorPoint( 32, 107, 203, 0.16);
        colorPoints[2] = new ColorPoint(237, 255, 255, 0.42);
        colorPoints[3] = new ColorPoint(255, 170,   0, 0.6425);
        colorPoints[4] = new ColorPoint(0,   2,   0, 0.8575);
        colorPoints[5] = new ColorPoint(0,   7, 100, 1);

        double percent = 0;
        double percentStep = 1 / (double)colorArraySize;
        System.out.println(percentStep);



        int index = 0;
        for (int i = 0; i < colorArraySize; i++) {
            System.out.println(percent);
            ColorPoint min = null;
            ColorPoint max = null;

            if (percent == 0) min = colorPoints[0];
            if (percent == 1) max = colorPoints[colorPoints.length-1];

            Color color = new Color(1);
            for (int j = colorPoints.length-1; j >= 0; j--) {
                if (percent <= colorPoints[i].percent ) max = colorPoints[i];
            }
            for (int j = 0; j < colorPoints.length; j++) {
                if (percent >= colorPoints[i].percent) min = colorPoints[i];
            }

            assert min != null;
            assert max != null;

            double r = interpolate(new double[]{min.percent, min.r}, new double[]{max.percent, max.r}, percent);
            double g = interpolate(new double[]{min.percent, min.g}, new double[]{max.percent, max.g}, percent);
            double b = interpolate(new double[]{min.percent, min.b}, new double[]{max.percent, max.b}, percent);

            int red = (int)Math.round(r);
            int green = (int)Math.round(g);
            int blue = (int)Math.round(b);

            colors[index][0] = red;
            colors[index][1] = green;
            colors[index][2] = blue;

            percent+= percentStep;


        }
/*
        for (int i = 0; i < colorArraySize;i++) {
            System.out.println("r: " + colors[i][0] + " | g:  " + colors[i][1] + " | b: " + colors[i][2]);
        }

 */

        System.out.println("done computing array...");

    }
}
