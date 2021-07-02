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

    public RenderThread(int maxIterations, Mandelbrot.InstructionSet instructionSet, String colorType) {
        this.maxIterations = maxIterations;
        this.instructionSet = instructionSet;
        this.colorType = colorType;
    }

    @Override
    public Result[] call() throws Exception {
        Result[] results = new Result[instructionSet.instructions.size()];

        for (int i = 0; i < instructionSet.instructions.size(); i++) {
            Mandelbrot.Instruction instruction = instructionSet.instructions.get(i);
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
        double[] z = {0,0};

        while (Double.isFinite(z[0]) && Double.isFinite(z[1]) && iterations < maxIterations) {
            z = solve(z[0], z[1], c);
            iterations++;
        }
        return new CalculationResult(z, iterations);
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

        return slope * percent + yIntercept;
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
        double iterationPercentage = (double)iterationsToInfinity / (double)maxIterations;
        int color =  Color.HSBtoRGB(0.7f + 10 * (float)iterationPercentage ,1f,1.0f);
        if (iterationsToInfinity == maxIterations) return new Color(0,0,0);
        return new Color(color);
    }
}
