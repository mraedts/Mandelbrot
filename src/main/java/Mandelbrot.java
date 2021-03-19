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
import java.util.ArrayList;

public class Mandelbrot {

    public static class InstructionSet {
        double xTarget;
        double yTarget;
        ArrayList<FrameInstruction> frameInstructions = new ArrayList<>();

        public InstructionSet(double xTarget, double yTarget) {
            this.xTarget = xTarget;
            this.yTarget = yTarget;
        }
    }

    public static class FrameInstruction {
        double zoom;
        String imageNumber;

        public FrameInstruction(double zoom, String imageNumber) {
            this.zoom = zoom;
            this.imageNumber = imageNumber;
        }
    }

    public static void main(String[] args) throws IOException {
        double zoom = 1000000;
        int maxImages = 100000;

        String imageNumber;

        InstructionSet[] instructionSets = new InstructionSet[8];
        for (int i = 0; i < instructionSets.length; i++) {
            instructionSets[i] = new InstructionSet(-0.74925,0.1005);
        }

        int setNumber = 0;
        int i = 0;
        while (i < maxImages) {
            imageNumber = String.format("%09d", i + 1);
            zoom = zoom + 10000000;

            instructionSets[setNumber].frameInstructions.add(new FrameInstruction(zoom, imageNumber));

            i++;
            setNumber++;
            if (setNumber == 8) setNumber = 0;
        }

        RenderThread[] threads = new RenderThread[8];

        for (int n = 0; n < threads.length; n++) {
           threads[n] = new RenderThread(instructionSets[n]);
           threads[n].start();
        }
    }
}
