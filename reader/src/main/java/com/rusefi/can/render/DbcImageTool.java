package com.rusefi.can.render;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.reader.DbcFileReader;
import com.rusefi.can.dbc.DbcPacket;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DbcImageTool {

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

        DbcFile dbc = DbcFileReader.readFromFile(dbcPath);
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
            return Integer.compare(f1.getLsbBitIndex(), f2.getLsbBitIndex());
        });

        Map<DbcField, double[]> minMaxMap = new HashMap<>();
        for (DbcField field : allFields) {
            double[] minMax = new double[2];
            renderField(field, packetsById.get(field.getSid()), minTime, duration, outputDir, escapeFileName(field.getName()) + ".png", minMax);
            minMaxMap.put(field, minMax);
        }

        createIndexHtml(allFields, minMaxMap, dbc, minTime, duration);
    }

    public static void renderField(DbcField field, List<CANPacket> packets, double minTime, double duration, String outputDir) throws IOException {
        renderField(field, packets, minTime, duration, outputDir, null);
    }

    public static void renderField(DbcField field, List<CANPacket> packets, double minTime, double duration, String outputDir, double[] minMax) throws IOException {
        renderField(field, packets, minTime, duration, outputDir, field.getName() + ".png", minMax);
    }

    public static void renderField(DbcField field, List<CANPacket> packets, double minTime, double duration, String outputDir, String fileName, double[] minMax) throws IOException {
        TraceResult tr = processTrace(field, packets, minTime, duration);

        if (minMax != null && !tr.points.isEmpty()) {
            minMax[0] = tr.min;
            minMax[1] = tr.max;
        }

        ChartImage chart = ChartRenderer.prepareImage(ChartRenderer.WIDTH, ChartRenderer.HEIGHT);
        Graphics2D g = chart.createGraphics();
        chart.drawBackground(g);

        if (tr.points.isEmpty()) {
            chart.drawNoData(g);
        } else {
            ChartRenderer.drawPoints(g, tr.points, tr.min, tr.max, Color.BLACK, chart);
            ChartRenderer.drawLabel(g, tr.min, tr.max, Color.BLACK, 50);
        }

        ChartRenderer.saveImage(chart, outputDir, fileName);
    }

    private static class TraceResult {
        List<Point> points = new ArrayList<>();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double mean;
        double stdDev;
    }

    private static TraceResult processTrace(DbcField field, List<CANPacket> packets, double minTime, double duration) {
        TraceResult result = new TraceResult();
        if (packets == null || packets.isEmpty()) return result;

        double sum = 0;
        double sumSq = 0;

        for (CANPacket packet : packets) {
            double value = field.getValue(packet);
            result.min = Math.min(result.min, value);
            result.max = Math.max(result.max, value);
            sum += value;
            sumSq += value * value;
            double x = (packet.getTimeStampMs() - minTime) / duration * (ChartRenderer.WIDTH - 1); // WIDTH used for x-axis scaling
            result.points.add(new Point((int) x, value));
        }

        result.mean = result.points.isEmpty() ? 0 : sum / result.points.size();
        result.stdDev = result.points.isEmpty() ? 0 : Math.sqrt(Math.max(0, sumSq / result.points.size() - result.mean * result.mean));

        return result;
    }

    public static class ComparisonResult {
        public final double minValue;
        public final double maxValue;
        public final double mean1;
        public final double stdDev1;
        public final double mean2;
        public final double stdDev2;
        public final double difference;
        public final double minTime;
        public final double duration;

        public ComparisonResult(double minValue, double maxValue, double mean1, double stdDev1, double mean2, double stdDev2, double difference, double minTime, double duration) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.mean1 = mean1;
            this.stdDev1 = stdDev1;
            this.mean2 = mean2;
            this.stdDev2 = stdDev2;
            this.difference = difference;
            this.minTime = minTime;
            this.duration = duration;
        }
    }

    public static ComparisonResult renderComparison(DbcField field,
                                        List<CANPacket> packets1, double minTime1, double duration1,
                                        List<CANPacket> packets2, double minTime2, double duration2,
                                        String outputDir, String fileName) throws IOException {
        TraceResult tr1 = processTrace(field, packets1, minTime1, duration1);
        TraceResult tr2 = processTrace(field, packets2, minTime2, duration2);

        double minValue = Math.min(tr1.min, tr2.min);
        double maxValue = Math.max(tr1.max, tr2.max);
        double difference = Math.abs(tr1.mean - tr2.mean) + Math.abs(tr1.stdDev - tr2.stdDev);

        ChartImage chart = ChartRenderer.prepareImage(ChartRenderer.WIDTH, ChartRenderer.HEIGHT);
        Graphics2D g = chart.createGraphics();
        chart.drawBackground(g);

        if (tr1.points.isEmpty() && tr2.points.isEmpty()) {
            chart.drawNoData(g);
        } else {
            ChartRenderer.drawPoints(g, tr1.points, minValue, maxValue, Color.GREEN, chart);
            ChartRenderer.drawPoints(g, tr2.points, minValue, maxValue, Color.RED, chart);

            if (!tr1.points.isEmpty()) {
                ChartRenderer.drawLabel(g, tr1.min, tr1.max, Color.GREEN, 50);
            }

            if (!tr2.points.isEmpty()) {
                ChartRenderer.drawLabel(g, tr2.min, tr2.max, Color.RED, 100);
            }
        }

        ChartRenderer.saveImage(chart, outputDir, fileName);
        return new ComparisonResult(minValue, maxValue, tr1.mean, tr1.stdDev, tr2.mean, tr2.stdDev, difference, minTime1, duration1);
    }

    public static void createComparisonHtml(List<ComparisonEntry> entries, String outputDir, String traceName1, String traceName2, String outputFileName, DbcFile dbc) throws IOException {
        File htmlFile = new File(outputDir, outputFileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile))) {
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Comparison: " + traceName1 + " vs " + traceName2 + "</title>");
            writer.println("<style>");
            writer.println(".y-label { position: fixed; bottom: 10px; right: 10px; font-weight: bold; color: blue; background: rgba(255,255,255,0.7); padding: 5px; border-radius: 5px; pointer-events: none; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<div id='yValue' class='y-label'></div>");
            writer.println("<h1>Comparison: <span style='color: green'>" + traceName1 + "</span> vs <span style='color: red'>" + traceName2 + "</span></h1>");
            writer.println("<h3>" + dbc.getFileName() + "</h3>");
            writer.print("<h2>Count=" + entries.size() + "<br>");
            Map<Integer, Long> countPerSid = entries.stream()
                    .collect(Collectors.groupingBy(entry -> entry.getField().getSid(), TreeMap::new, Collectors.counting()));
            writer.print(countPerSid.entrySet().stream()
                    .map(entry -> {
                        String name = DbcFile.getPacketName(dbc, entry.getKey());
                        return name + ": " + entry.getValue();
                    })
                    .collect(Collectors.joining("<br>")));
            writer.println("</h2>");
            writer.println("<table border='1'>");
            writer.println("<tr><th>Field Info</th><th>Statistics</th><th>Visualization</th></tr>");
            for (ComparisonEntry entry : entries) {
                DbcField field = entry.getField();
                String packetName = DbcFile.getPacketName(dbc, field.getSid());

                writer.printf("<tr><td>%s<br>%s</td><td>" +
                                "<span style='color: green'>Mean 1: %.2f<br>StdDev 1: %.2f</span><br><span style='color: red'>Mean 2: %.2f<br>StdDev 2: %.2f<br>Difference %.2f</span>" +
                                "</td><td><img src='images/%s' width='750' data-min='%.2f' data-max='%.2f' data-mintime='%.2f' data-duration='%.2f' onmousemove='updateY(event, this)' onmouseout='hideY()'></td></tr>%n",
                        packetName, field.getName(),
                        entry.getMean1(), entry.getStdDev1(), entry.getMean2(), entry.getStdDev2(),
                        entry.getDifference(),

                        entry.getImageName(), entry.getMinValue(), entry.getMaxValue(), entry.getMinTime(), entry.getDuration());
            }
            writer.println("</table>");
            writer.println("<script>");
            writer.println("function updateY(event, img) {");
            writer.println("  var rect = img.getBoundingClientRect();");
            writer.println("  var x = event.clientX - rect.left;");
            writer.println("  var y = event.clientY - rect.top;");
            writer.println("  var min = parseFloat(img.getAttribute('data-min'));");
            writer.println("  var max = parseFloat(img.getAttribute('data-max'));");
            writer.println("  var minTime = parseFloat(img.getAttribute('data-mintime')) || 0;");
            writer.println("  var duration = parseFloat(img.getAttribute('data-duration')) || 0;");
            writer.println("  var val = max - (y / rect.height) * (max - min);");
            writer.println("  var time = minTime + (x / rect.width) * duration;");
            writer.println("  var label = document.getElementById('yValue');");
            writer.println("  label.innerText = 'Time: ' + (time / 1000).toFixed(3) + 's Value: ' + val.toFixed(2);");
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
        private final ComparisonResult result;

        public ComparisonEntry(DbcField field, String imageName, ComparisonResult result) {
            this.field = field;
            this.imageName = imageName;
            this.result = result;
        }

        public double getDifference() {
            return result.difference;
        }

        public double getMinValue() {
            return result.minValue;
        }

        public double getMaxValue() {
            return result.maxValue;
        }

        public DbcField getField() {
            return field;
        }

        public String getImageName() {
            return imageName;
        }

        public double getMean1() {
            return result.mean1;
        }

        public double getStdDev1() {
            return result.stdDev1;
        }

        public double getMean2() {
            return result.mean2;
        }

        public double getStdDev2() {
            return result.stdDev2;
        }

        public double getMinTime() {
            return result.minTime;
        }

        public double getDuration() {
            return result.duration;
        }
    }

    private static void createIndexHtml(List<DbcField> fields, Map<DbcField, double[]> minMaxMap, DbcFile dbc, double minTime, double duration) throws IOException {
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
            writer.println("<tr><th>Packet Name</th><th>Field Name</th><th>Visualization</th></tr>");
            for (DbcField field : fields) {
                double[] minMax = minMaxMap.get(field);
                double min = minMax != null ? minMax[0] : 0;
                double max = minMax != null ? minMax[1] : 0;

                String packetName = DbcFile.getPacketName(dbc, field.getSid());

                writer.printf("<tr><td>%s</td><td>%s</td><td><img src='processed/images/%s.png' width='750' data-min='%.2f' data-max='%.2f' data-mintime='%.2f' data-duration='%.2f' onmousemove='updateY(event, this)' onmouseout='hideY()'></td></tr>%n",
                        packetName, field.getName(), field.getName(), min, max, minTime, duration);
            }
            writer.println("</table>");
            writer.println("<script>");
            writer.println("function updateY(event, img) {");
            writer.println("  var rect = img.getBoundingClientRect();");
            writer.println("  var x = event.clientX - rect.left;");
            writer.println("  var y = event.clientY - rect.top;");
            writer.println("  var min = parseFloat(img.getAttribute('data-min'));");
            writer.println("  var max = parseFloat(img.getAttribute('data-max'));");
            writer.println("  var minTime = parseFloat(img.getAttribute('data-mintime')) || 0;");
            writer.println("  var duration = parseFloat(img.getAttribute('data-duration')) || 0;");
            writer.println("  var val = max - (y / rect.height) * (max - min);");
            writer.println("  var time = minTime + (x / rect.width) * duration;");
            writer.println("  var label = document.getElementById('yValue');");
            writer.println("  label.innerText = 'Time: ' + (time / 1000).toFixed(3) + 's Value: ' + val.toFixed(2);");
            writer.println("  label.style.display = 'block';");
            writer.println("}");
            writer.println("function hideY() {");
            writer.println("  document.getElementById('yValue').style.display = 'none';");
            writer.println("}");
            writer.println("</script>");
            writer.println("</body></html>");
        }
    }

}
