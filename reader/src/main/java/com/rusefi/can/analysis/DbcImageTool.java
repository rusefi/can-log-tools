package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DbcImageTool {
    private static final int WIDTH = 1500;
    private static final int HEIGHT = 200;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: DbcImageTool <dbcFile> <traceFile>");
            return;
        }

        String dbcPath = args[0];
        String tracePath = args[1];

        DbcFile dbc = DbcFile.readFromFile(dbcPath);
        List<CANPacket> packets = CANLineReader.getReader().readFile(tracePath);

        if (packets.isEmpty()) {
            System.err.println("No packets found in " + tracePath);
            return;
        }

        String outputDir = "processed" + File.separator + "images";
        new File(outputDir).mkdirs();

        Map<Integer, List<CANPacket>> packetsById = packets.stream()
                .collect(Collectors.groupingBy(CANPacket::getId));

        double minTime = packets.get(0).getTimeStampMs();
        double maxTime = packets.get(packets.size() - 1).getTimeStampMs();
        double duration = maxTime - minTime;

        List<DbcField> allFields = new ArrayList<>();
        for (DbcPacket packet : dbc.values()) {
            allFields.addAll(packet.getFields());
        }

        // Sort by packet ID and starting bit for index.html
        allFields.sort((f1, f2) -> {
            if (f1.getSid() != f2.getSid()) {
                return Integer.compare(f1.getSid(), f2.getSid());
            }
            return Integer.compare(f1.getStartOffset(), f2.getStartOffset());
        });

        for (DbcField field : allFields) {
            renderField(field, packetsById.get(field.getSid()), minTime, duration, outputDir);
        }

        createIndexHtml(allFields, outputDir);
    }

    private static void renderField(DbcField field, List<CANPacket> packets, double minTime, double duration, String outputDir) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);

        if (packets != null && !packets.isEmpty()) {
            List<Point> points = new ArrayList<>();
            double minValue = Double.MAX_VALUE;
            double maxValue = -Double.MAX_VALUE;

            for (CANPacket packet : packets) {
                double value = field.getValue(packet);
                minValue = Math.min(minValue, value);
                maxValue = Math.max(maxValue, value);
                double x = (packet.getTimeStampMs() - minTime) / duration * (WIDTH - 1);
                points.add(new Point((int) x, value));
            }

            double valueRange = maxValue - minValue;
            if (valueRange == 0) valueRange = 1;

            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                int y1 = HEIGHT - 1 - (int) ((p1.value - minValue) / valueRange * (HEIGHT - 1));
                int y2 = HEIGHT - 1 - (int) ((p2.value - minValue) / valueRange * (HEIGHT - 1));
                g.drawLine(p1.x, y1, p2.x, y2);
            }
            
            g.drawString(String.format("Min: %.2f Max: %.2f", minValue, maxValue), 10, 20);
        } else {
            g.drawString("No data", WIDTH / 2 - 20, HEIGHT / 2);
        }

        g.dispose();
        File outFile = new File(outputDir, field.getName() + ".png");
        ImageIO.write(image, "png", outFile);
    }

    private static void createIndexHtml(List<DbcField> fields, String outputDir) throws IOException {
        File htmlFile = new File("index.html");
        try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile))) {
            writer.println("<html><body>");
            writer.println("<h1>CAN Field Visualizations</h1>");
            writer.println("<table border='1'>");
            writer.println("<tr><th>Packet ID</th><th>Start Bit</th><th>Field Name</th><th>Visualization</th></tr>");
            for (DbcField field : fields) {
                writer.printf("<tr><td>0x%X</td><td>%d</td><td>%s</td><td><img src='processed/images/%s.png' width='750'></td></tr>%n",
                        field.getSid(), field.getStartOffset(), field.getName(), field.getName());
            }
            writer.println("</table>");
            writer.println("</body></html>");
        }
    }

    private static class Point {
        int x;
        double value;

        Point(int x, double value) {
            this.x = x;
            this.value = value;
        }
    }
}
