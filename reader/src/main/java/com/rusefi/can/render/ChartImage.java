package com.rusefi.can.render;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ChartImage {
    public final BufferedImage image;
    public final int width;
    public final int height;

    public ChartImage(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void drawNoData(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.drawString("No data", width / 2 - 20, height / 2);
    }

    public void drawBackground(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
    }

    public Graphics2D createGraphics() {
        return image.createGraphics();
    }
}
