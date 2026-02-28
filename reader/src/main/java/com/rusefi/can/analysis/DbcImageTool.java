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
    public static final int WIDTH = 1500;
    public static final int HEIGHT = 700;

    public static String escapeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

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

        Map<DbcField, double[]> minMaxMap = new HashMap<>();
        for (DbcField field : allFields) {
            double[] minMax = new double[2];
            renderField(field, packetsById.get(field.getSid()), minTime, duration, outputDir, escapeFileName(field.getName()) + ".png", minMax);
            minMaxMap.put(field, minMax);
        }

        createIndexHtml(allFields, minMaxMap);
    }

    public static void renderField(DbcField field, List<CANPacket> packets, double minTime, double duration, String outputDir) throws IOException {
        renderField(field, packets, minTime, duration, outputDir, null);
    }

    public static void renderField(DbcField field, List<CANPacket> packets, double minTime, double duration, String outputDir, double[] minMax) throws IOException {
        renderField(field, packets, minTime, duration, outputDir, field.getName() + ".png", minMax);
    }

    public static void renderField(DbcField field, List<CANPacket> packets, double minTime, double duration, String outputDir, String fileName, double[] minMax) throws IOException {
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

            if (minMax != null) {
                minMax[0] = minValue;
                minMax[1] = maxValue;
            }

            drawPoints(g, points, minValue, maxValue, Color.BLACK);

            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(g.getFont().getSize2D() * 3f));
            g.drawString(String.format("Min: %.2f Max: %.2f", minValue, maxValue), 10, 20);
        } else {
            g.drawString("No data", WIDTH / 2 - 20, HEIGHT / 2);
        }

        g.dispose();
        File outFile = new File(outputDir, fileName);
        ImageIO.write(image, "png", outFile);
    }

    private static void drawPoints(Graphics2D g, List<Point> points, double minValue, double maxValue, Color color) {
        double valueRange = maxValue - minValue;
        if (valueRange == 0) valueRange = 1;

        g.setColor(color);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            int y1 = HEIGHT - 1 - (int) ((p1.value - minValue) / valueRange * (HEIGHT - 1));
            int y2 = HEIGHT - 1 - (int) ((p2.value - minValue) / valueRange * (HEIGHT - 1));
            g.drawLine(p1.x, y1, p2.x, y2);
        }
    }

    public static double[] renderComparison(DbcField field,
                                        List<CANPacket> packets1, double minTime1, double duration1,
                                        List<CANPacket> packets2, double minTime2, double duration2,
                                        String outputDir, String fileName) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        double minValue = Double.MAX_VALUE;
        double maxValue = -Double.MAX_VALUE;

        List<Point> points1 = new ArrayList<>();
        if (packets1 != null && !packets1.isEmpty()) {
            for (CANPacket packet : packets1) {
                double value = field.getValue(packet);
                minValue = Math.min(minValue, value);
                maxValue = Math.max(maxValue, value);
                double x = (packet.getTimeStampMs() - minTime1) / duration1 * (WIDTH - 1);
                points1.add(new Point((int) x, value));
            }
        }

        List<Point> points2 = new ArrayList<>();
        if (packets2 != null && !packets2.isEmpty()) {
            for (CANPacket packet : packets2) {
                double value = field.getValue(packet);
                minValue = Math.min(minValue, value);
                maxValue = Math.max(maxValue, value);
                double x = (packet.getTimeStampMs() - minTime2) / duration2 * (WIDTH - 1);
                points2.add(new Point((int) x, value));
            }
        }

        if (points1.isEmpty() && points2.isEmpty()) {
            g.setColor(Color.BLACK);
            g.drawString("No data", WIDTH / 2 - 20, HEIGHT / 2);
        } else {
            drawPoints(g, points1, minValue, maxValue, Color.GREEN);
            drawPoints(g, points2, minValue, maxValue, Color.RED);

            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(g.getFont().getSize2D() * 3f));
            g.drawString(String.format("Min: %.2f Max: %.2f", minValue, maxValue), 10, 20);
        }

        g.dispose();
        File outFile = new File(outputDir, fileName);
        ImageIO.write(image, "png", outFile);
        return new double[]{minValue, maxValue};
    }

    public static void createComparisonHtml(List<ComparisonEntry> entries, String outputDir, String title) throws IOException {
        File htmlFile = new File(outputDir, "comparison.html");
        try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile))) {
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<style>");
            writer.println(".y-label { position: fixed; bottom: 10px; right: 10px; font-weight: bold; color: blue; background: rgba(255,255,255,0.7); padding: 5px; border-radius: 5px; pointer-events: none; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<div id='yValue' class='y-label'></div>");
            writer.println("<h1>" + title + "</h1>");
            writer.println("<table border='1'>");
            writer.println("<tr><th>Packet ID</th><th>Start Bit</th><th>Field Name</th><th>Visualization</th></tr>");
            for (ComparisonEntry entry : entries) {
                writer.printf("<tr><td>0x%X</td><td>%d</td><td>%s</td><td><img src='images/%s' width='750' data-min='%.2f' data-max='%.2f' onmousemove='updateY(event, this)' onmouseout='hideY()'></td></tr>%n",
                        entry.field.getSid(), entry.field.getStartOffset(), entry.field.getName(), entry.imageName, entry.minValue, entry.maxValue);
            }
            writer.println("</table>");
            writer.println("<script>");
            writer.println("function updateY(event, img) {");
            writer.println("  var rect = img.getBoundingClientRect();");
            writer.println("  var y = event.clientY - rect.top;");
            writer.println("  var min = parseFloat(img.getAttribute('data-min'));");
            writer.println("  var max = parseFloat(img.getAttribute('data-max'));");
            writer.println("  var val = max - (y / rect.height) * (max - min);");
            writer.println("  var label = document.getElementById('yValue');");
            writer.println("  label.innerText = 'Value: ' + val.toFixed(2);");
            writer.println("  label.style.display = 'block';");
            writer.println("}");
            writer.println("function hideY() {");
            writer.println("  document.getElementById('yValue').style.display = 'none';");
            writer.println("}");
            writer.println("</script>");
            writer.println("</body></html>");
        }
    }

    public static class ComparisonEntry {
        private final DbcField field;
        private final String imageName;
        private final double minValue;
        private final double maxValue;

        public ComparisonEntry(DbcField field, String imageName, double minValue, double maxValue) {
            this.field = field;
            this.imageName = imageName;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public double getMinValue() {
            return minValue;
        }

        public double getMaxValue() {
            return maxValue;
        }

        public DbcField getField() {
            return field;
        }

        public String getImageName() {
            return imageName;
        }
    }

    private static void createIndexHtml(List<DbcField> fields, Map<DbcField, double[]> minMaxMap) throws IOException {
        File htmlFile = new File("index.html");
        try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile))) {
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<style>");
            writer.println(".y-label { position: fixed; bottom: 10px; right: 10px; font-weight: bold; color: blue; background: rgba(255,255,255,0.7); padding: 5px; border-radius: 5px; pointer-events: none; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<div id='yValue' class='y-label'></div>");
            writer.println("<h1>CAN Field Visualizations</h1>");
            writer.println("<table border='1'>");
            writer.println("<tr><th>Packet ID</th><th>Start Bit</th><th>Field Name</th><th>Visualization</th></tr>");
            for (DbcField field : fields) {
                double[] minMax = minMaxMap.get(field);
                double min = minMax != null ? minMax[0] : 0;
                double max = minMax != null ? minMax[1] : 0;
                writer.printf("<tr><td>0x%X</td><td>%d</td><td>%s</td><td><img src='processed/images/%s.png' width='750' data-min='%.2f' data-max='%.2f' onmousemove='updateY(event, this)' onmouseout='hideY()'></td></tr>%n",
                        field.getSid(), field.getStartOffset(), field.getName(), field.getName(), min, max);
            }
            writer.println("</table>");
            writer.println("<script>");
            writer.println("function updateY(event, img) {");
            writer.println("  var rect = img.getBoundingClientRect();");
            writer.println("  var y = event.clientY - rect.top;");
            writer.println("  var min = parseFloat(img.getAttribute('data-min'));");
            writer.println("  var max = parseFloat(img.getAttribute('data-max'));");
            writer.println("  var val = max - (y / rect.height) * (max - min);");
            writer.println("  var label = document.getElementById('yValue');");
            writer.println("  label.innerText = 'Value: ' + val.toFixed(2);");
            writer.println("  label.style.display = 'block';");
            writer.println("}");
            writer.println("function hideY() {");
            writer.println("  document.getElementById('yValue').style.display = 'none';");
            writer.println("}");
            writer.println("</script>");
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
