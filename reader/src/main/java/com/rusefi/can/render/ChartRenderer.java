package com.rusefi.can.render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChartRenderer {
    public static final int WIDTH = 1500;
    public static final int HEIGHT = 700;

    public static ChartImage prepareImage(int width, int height) {
        return new ChartImage(width, height);
    }

    public static void drawLabel(Graphics2D g, double minValue, double maxValue, Color color, int y) {
        g.setColor(color);
        Font oldFont = g.getFont();
        g.setFont(oldFont.deriveFont(oldFont.getSize2D() * 3f));
        g.drawString(String.format("Min: %.2f Max: %.2f", minValue, maxValue), 10, y);
        g.setFont(oldFont);
    }

    public static void drawPoints(Graphics2D g, List<Point> points, double minValue, double maxValue, Color color, ChartImage chart) {
        double valueRange = maxValue - minValue;
        if (valueRange == 0) valueRange = 1;

        g.setColor(color);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            int y1 = chart.height - 1 - (int) ((p1.value - minValue) / valueRange * (chart.height - 1));
            int y2 = chart.height - 1 - (int) ((p2.value - minValue) / valueRange * (chart.height - 1));
            g.drawLine(p1.x, y1, p2.x, y2);
        }
    }

    public static void saveImage(ChartImage chart, String outputDir, String fileName) throws IOException {
        Graphics2D g = (Graphics2D) chart.image.getGraphics();
        g.dispose();
        File outFile = new File(outputDir, fileName);
        ImageIO.write(chart.image, "png", outFile);
    }
}
