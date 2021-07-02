package main.java;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Mandelbrot {
    int threadCount;
    int width = 2560;
    int height = 1440;
    int maxIterations = 2000;
    double zoom = 1;
    double zoomSpeed = 1.03; // * 100 = % zoom per frame
    double xTarget =  -0.0452407411;
    double yTarget = 0.9868162204352258;
    /* targets:
                (-0.74915,0.1005)
     */
    int maxFrames = 1000;
    String colorType = "Ultra Fractal";

    int frameCount = 1;
    int framesLeftTorender = maxFrames;
    int etaMillis = 0;

    long totalFrameTime = 0;

    public Mandelbrot() throws InterruptedException, IOException, ExecutionException {
        long start = System.currentTimeMillis();

        while (frameCount < maxFrames + 1 && Double.isFinite(zoom)) {
            long frameStart = System.currentTimeMillis();
            System.out.println("Rendering frame " + frameCount + "/" + maxFrames + " | " + round(((double)frameCount/maxFrames) * 100, 1) + "% | Zoom level: " + zoom + " | ETA: " + etaMillis / 1000 + "s");
            this.threadCount = Runtime.getRuntime().availableProcessors();

            InstructionSet[] instructionSets =  createInStructions(width,height,xTarget,yTarget, zoom);

            startThreads(instructionSets);

            zoom = zoom * zoomSpeed;
            frameCount++;

            long frameFinish = System.currentTimeMillis();
            long frameTimeElapsed = frameFinish - frameStart;

            totalFrameTime += frameTimeElapsed;

            double avgMillisPerFrame = totalFrameTime / ((double)frameCount-1);
            etaMillis = (int)Math.round(framesLeftTorender * avgMillisPerFrame);
            framesLeftTorender--;
        }

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        System.out.println("Finished rendering "+ maxFrames + " frames after " + round((double)timeElapsed / 1000,1) +
                " seconds.\nResolution:   " +width + "x" + height + "\nMaximum iterations:   "
                + maxIterations + "\nZoom per frame: " + round((zoomSpeed - 1) * 100, 0) + "%\nTarget: (" + xTarget + "," + yTarget + ")");

    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
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
    }

    public class GridLimits {
        double xMin;
        double yMax;
        double xMax;
        double yMin;

        public GridLimits(double xTarget, double yTarget, double zoom, int width, int height) {
            String aspectRatio = getAspectRatio(width, height);
            if (aspectRatio.equals("1:1")) {
                this.xMin = xTarget - 12.5 / (2 * zoom);
                this.yMax = yTarget + 12.5 / (2 * zoom) ;
                this.yMin = yTarget - 12.5 / (2 * zoom);
                this.xMax = xTarget + 12.5 / (2 * zoom);
            } else if (aspectRatio.equals("16:9")) {
                this.xMin = xTarget - 16 / (2 * zoom);
                this.yMax = yTarget + 9 / (2 * zoom);
                this.yMin = yTarget - 9 / (2 * zoom);
                this.xMax = xTarget + 16 / (2 * zoom);
            }
        }

        public String getAspectRatio(int width, int height) {
            if (width == height) return "1:1";
            else if (((double)width / 16) == ((double)height / 9)) return "16:9";
            else throw new IllegalArgumentException("Invalid resolution (" + width + "x" + height + ") entered! supported aspect ratios are 1:1 and 16:9.");
        }
    }

    public void startThreads(InstructionSet[] instructionSets) throws InterruptedException, ExecutionException, IOException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Callable<Result[]>> callables = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            RenderThread callable = new RenderThread(maxIterations,instructionSets[i], colorType);
            callables.add(callable);
        }

        List<Future<Result[]>> futures = executor.invokeAll(callables);
        executor.shutdown();

        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < futures.size(); i++) {
            Result[] results = futures.get(i).get();

            for (int j = 0; j < results.length; j++) {
                Color color = results[j].color;
                image.setRGB(results[j].column,results[j].row, results[j].color.getRGB());
            }
        }

        File file = new File("C:\\Users\\Mart\\IdeaProjects\\Mandelbrot\\imgs\\" + "img"+ String.format("%03d", frameCount) +".png");
        ImageIO.write(image, "png", file);
    }

    public InstructionSet[] createInStructions(int width, int height, double xTarget, double yTarget, double zoom) {
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
