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

    public RenderThread(int maxIterations, Mandelbrot.InstructionSet instructionSet) {
        this.maxIterations = maxIterations;
        this.instructionSet = instructionSet;
    }

    @Override
    public Result[] call() throws Exception {
        Result[] results = new Result[instructionSet.instructions.size()];

        for (int i = 0; i < instructionSet.instructions.size(); i++) {
            Mandelbrot.Instruction instruction = instructionSet.instructions.get(i);
            CalculationResult result = iterate(new double[] {instruction.x,instruction.y});
            Color color = determineColor(result.iterations);
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

    public Color determineColor(int iterationsToInfinity) {
        double iterationPercentage = (double)iterationsToInfinity / (double)maxIterations;

        int color =  Color.HSBtoRGB(0.7f + 10 * (float)iterationPercentage ,0.6f,1.0f);

        if (iterationsToInfinity == maxIterations) return new Color(0,0,0);
        return new Color(color);
    }


}
