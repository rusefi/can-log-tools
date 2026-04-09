package com.rusefi.can.render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChartRenderer {
    public static BufferedImage prepareImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public static void drawBackground(Graphics2D g, int width, int height) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
    }

    public static void drawNoData(Graphics2D g, int width, int height) {
        g.setColor(Color.BLACK);
        g.drawString("No data", width / 2 - 20, height / 2);
    }

    public static void drawLabel(Graphics2D g, double minValue, double maxValue, Color color, int y) {
        g.setColor(color);
        Font oldFont = g.getFont();
        g.setFont(oldFont.deriveFont(oldFont.getSize2D() * 3f));
        g.drawString(String.format("Min: %.2f Max: %.2f", minValue, maxValue), 10, y);
        g.setFont(oldFont);
    }

    public static void drawPoints(Graphics2D g, List<Point> points, double minValue, double maxValue, Color color, int width, int height) {
        double valueRange = maxValue - minValue;
        if (valueRange == 0) valueRange = 1;

        g.setColor(color);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            int y1 = height - 1 - (int) ((p1.value - minValue) / valueRange * (height - 1));
            int y2 = height - 1 - (int) ((p2.value - minValue) / valueRange * (height - 1));
            g.drawLine(p1.x, y1, p2.x, y2);
        }
    }

    public static void saveImage(BufferedImage image, String outputDir, String fileName) throws IOException {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.dispose();
        File outFile = new File(outputDir, fileName);
        ImageIO.write(image, "png", outFile);
    }
}
