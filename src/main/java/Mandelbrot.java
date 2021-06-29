package main.java;/*
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Mandelbrot {
    int threadCount;
    int width = 4000;
    int height = 4000;
    int maxIterations = 100000;
    int zoom = 2000;


    public Mandelbrot() throws InterruptedException, IOException, ExecutionException {
        this.threadCount = Runtime.getRuntime().availableProcessors();
        //this.threadCount = 1;
        InstructionSet[] instructionSets =  createInStructions(maxIterations,width,height,0,0, zoom);
        startThreads(instructionSets);

    }

    public  class Instruction {
        double x;
        double y;
        int row, column;

        public Instruction(double x, double y, int column, int row) {
            this.x = x;
            this.y = y;
            this.column = column;
            this.row = row;
        }
    }

    public class InstructionSet {

        ArrayList<Instruction> instructions = new ArrayList<>();



        public void add(Instruction instruction) {
            this.instructions.add(instruction);
        }


    }

    public class GridLimits {
        double xMin;
        double yMax;
        double xMax;
        double yMin;

        public GridLimits(double xTarget, double yTarget, double zoom, int width, int height) {
            this.xMin = xTarget - (double)width / (2 * zoom) ;
            this.yMax = yTarget + (double)height / (2 * zoom)  ;
            this.yMin = yTarget - (double)height / (2 * zoom) ;
            this.xMax = xTarget + (double)width / (2 * zoom)  ;
        }
    }


    public void startThreads(InstructionSet[] instructionSets) throws InterruptedException, ExecutionException, IOException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Result[]>> callables = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            CalculationThread callable = new CalculationThread(100,instructionSets[i]);
            callables.add(callable);
        }

        List<Future<Result[]>> futures = executor.invokeAll(callables);
        executor.shutdown();

        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);


        for (int i = 0; i < futures.size(); i++) {
            Result[] results = futures.get(i).get();

            for (int j = 0; j < results.length; j++) {
                Color color = results[j].color;
                image.setRGB(results[j].row,results[j].column, results[j].color.getRGB());
            }
        }

        File file = new File("C:\\Users\\Mart\\IdeaProjects\\Mandelbrot\\imgs\\" + "render" +".png");
        ImageIO.write(image, "png", file);


    }

    public InstructionSet[] createInStructions(int maxIterations, int width, int height, int xTarget, int yTarget, int zoom) {
        InstructionSet[] instructionSets = new InstructionSet[threadCount];

        GridLimits limits = new GridLimits(xTarget, yTarget, zoom, width, height);

        double x = limits.xMin;
        double y = limits.yMax;

        int column = 0;
        int row = 0;

        double xStep = Math.abs(limits.xMax - limits.xMin) / (double) width;
        double yStep = Math.abs(limits.yMax - limits.yMin) / (double) height;

        int thread = 0;

        for (int i = 0; i < threadCount; i++) {
            instructionSets[i] = new InstructionSet();
        }
        for (int i = 1; i <= width * height; i++) {
            double[] coords = {x, y};
            //System.out.println("coords: " + x + " | " + y);

            instructionSets[thread].instructions.add(new Instruction(x, y, column, row));

            if (i % width == 0) {
                y = y - yStep;
                x = limits.xMin;
                column = 0;
                row++;
            } else {
                column++;
                x = x + xStep;
            }
            thread++;
            if (thread == threadCount) thread = 0;
        }
        return instructionSets;
    }



    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Mandelbrot mandelbrot = new Mandelbrot();






    }
}
