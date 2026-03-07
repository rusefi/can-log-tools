package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.can.reader.dbc.ValidateDbc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class MatchFinder {
    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.out.println("Usage: MatchFinder <dbc1> <trc1> <dbc2> <trc2>");
            return;
        }

        String dbcPath1 = args[0];
        String trcPath1 = args[1];
        String dbcPath2 = args[2];
        String trcPath2 = args[3];

        DbcFile dbc1 = ValidateDbc.readFromFileWithValidation(dbcPath1);
        List<CANPacket> packets1 = CANLineReader.getReader().readFile(trcPath1);

        DbcFile dbc2 = ValidateDbc.readFromFileWithValidation(dbcPath2);
        List<CANPacket> packets2 = CANLineReader.getReader().readFile(trcPath2);

        if (packets1.isEmpty() || packets2.isEmpty()) {
            System.err.println("One of the trace files is empty");
            return;
        }

        double minTime1 = packets1.get(0).getTimeStampMs();
        double maxTime1 = packets1.get(packets1.size() - 1).getTimeStampMs();
        double duration1 = maxTime1 - minTime1;

        double minTime2 = packets2.get(0).getTimeStampMs();
        double maxTime2 = packets2.get(packets2.size() - 1).getTimeStampMs();
        double duration2 = maxTime2 - minTime2;

        Map<Integer, List<CANPacket>> packets1ById = packets1.stream().collect(Collectors.groupingBy(CANPacket::getId));
        Map<Integer, List<CANPacket>> packets2ById = packets2.stream().collect(Collectors.groupingBy(CANPacket::getId));

        List<DbcField> fields1 = getAllFields(dbc1);
        List<DbcField> fields2 = getAllFields(dbc2);

        System.out.println("Fields in trace 1: " + fields1.size());
        System.out.println("Fields in trace 2: " + fields2.size());

        String outputDir = "match_report";
        new File(outputDir).mkdirs();
        String imagesDir = outputDir + File.separator + "images";
        new File(imagesDir).mkdirs();

        List<Match> matches = new ArrayList<>();

        for (DbcField f2 : fields2) {
            List<CANPacket> p2 = packets2ById.get(f2.getSid());
            if (p2 == null || p2.isEmpty()) continue;

            double[] ts2 = getNormalizedTimeSeries(f2, p2, minTime2, duration2);
            if (ts2 == null) continue;

            Match bestMatch = null;
            double minDistance = Double.MAX_VALUE;

            for (DbcField f1 : fields1) {
                List<CANPacket> p1 = packets1ById.get(f1.getSid());
                if (p1 == null || p1.isEmpty()) continue;

                double[] ts1 = getNormalizedTimeSeries(f1, p1, minTime1, duration1);
                if (ts1 == null) continue;

                double distance = calculateDistance(ts1, ts2);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestMatch = new Match(f2, f1, distance);
                }
            }

            if (bestMatch != null) {
                matches.add(bestMatch);
                System.out.println("Best match for " + f2.getName() + " is " + bestMatch.f1.getName() + " (dist=" + minDistance + ")");
            }
        }

        createHtmlReport(matches, packets1ById, minTime1, duration1, packets2ById, minTime2, duration2, outputDir);
    }

    private static List<DbcField> getAllFields(DbcFile dbc) {
        List<DbcField> fields = new ArrayList<>();
        for (DbcPacket packet : dbc.values()) {
            fields.addAll(packet.getFields());
        }
        return fields;
    }

    private static final int SAMPLES = 1000;

    private static double[] getNormalizedTimeSeries(DbcField field, List<CANPacket> packets, double minTime, double duration) {
        double[] values = new double[SAMPLES];
        int count = 0;
        double sum = 0;
        double sumSq = 0;

        int packetIdx = 0;
        for (int i = 0; i < SAMPLES; i++) {
            double targetTime = minTime + (double) i / (SAMPLES - 1) * duration;
            while (packetIdx < packets.size() - 1 && packets.get(packetIdx + 1).getTimeStampMs() < targetTime) {
                packetIdx++;
            }
            double val = field.getValue(packets.get(packetIdx));
            values[i] = val;
            sum += val;
            sumSq += val * val;
            count++;
        }

        if (count == 0) return null;
        double mean = sum / count;
        double std = Math.sqrt(Math.max(0, sumSq / count - mean * mean));

        if (std < 1e-6) {
            // Constant value - do not include in report
            return null;
        } else {
            for (int i = 0; i < SAMPLES; i++) {
                values[i] = (values[i] - mean) / std;
            }
        }

        return values;
    }

    private static double calculateDistance(double[] ts1, double[] ts2) {
        double dist = 0;
        for (int i = 0; i < SAMPLES; i++) {
            dist += Math.abs(ts1[i] - ts2[i]);
        }
        return dist / SAMPLES;
    }

    private static void createHtmlReport(List<Match> matches,
                                         Map<Integer, List<CANPacket>> packets1ById, double minTime1, double duration1,
                                         Map<Integer, List<CANPacket>> packets2ById, double minTime2, double duration2,
                                         String outputDir) throws IOException {
        String imagesDir = "images";
        
        matches.sort(Comparator.comparingDouble(m -> m.distance));

        try (PrintWriter pw = new PrintWriter(new FileWriter(outputDir + File.separator + "index.html"))) {
            pw.println("<html><body>");
            pw.println("<h1>Match Finder Report</h1>");
            pw.println("<table border='1'>");
            pw.println("<tr><th>Field from Trace 2</th><th>Best Match from Trace 1</th><th>Distance</th><th>Comparison Image</th></tr>");

            for (Match match : matches) {
                String imgName = DbcImageTool.escapeFileName(match.f2.getName() + "_vs_" + match.f1.getName()) + ".png";
                
                renderComparison(match, packets1ById.get(match.f1.getSid()), minTime1, duration1,
                                       packets2ById.get(match.f2.getSid()), minTime2, duration2,
                                       outputDir + File.separator + imagesDir, imgName);

                String name1 = match.f1.getName();
                String name2 = match.f2.getName();
                String color2 = name1.equals(name2) ? "black" : "red";

                String dbc1 = new File(match.f1.getParentPacket().getFileName()).getName();
                String dbc2 = new File(match.f2.getParentPacket().getFileName()).getName();

                pw.println("<tr>");
                pw.println("<td><font color='" + color2 + "'>" + name2 + "</font><br/>" + dbc2 + "<br/>" +
                        getString(match.f2)


                        + "</td>");
                pw.println("<td>" + name1 + "<br/>" + dbc1 + "<br/>" +
                                getString(match.f1) +
                        "</td>");
                pw.println("<td>" + String.format("%.4f", match.distance) + "</td>");
                pw.println("<td><img src='" + imagesDir + "/" + imgName + "' width='800'></td>");
                pw.println("</tr>");
            }

            pw.println("</table>");
            pw.println("</body></html>");
        }
    }

    private static String getString(DbcField field) {
        return "sid " + field.getSid() +
                " 0x" + Integer.toHexString(field.getSid());
    }

    private static void renderComparison(Match match, List<CANPacket> p1, double minTime1, double duration1,
                                         List<CANPacket> p2, double minTime2, double duration2,
                                         String outputDir, String imgName) throws IOException {
        
        // We want to render both signals on the same image.
        // DbcImageTool.renderComparison takes ONE field and two sets of packets. 
        // But here we have TWO different fields.
        
        // I'll need a custom render function or adapt DbcImageTool.
        
        // Let's implement a simple one here.
        
        int width = DbcImageTool.WIDTH;
        int height = DbcImageTool.HEIGHT;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);

        drawSignal(g, match.f1, p1, minTime1, duration1, java.awt.Color.BLUE, "Trace 1: " + match.f1.getName(), 0);
        drawSignal(g, match.f2, p2, minTime2, duration2, java.awt.Color.RED, "Trace 2: " + match.f2.getName(), 20);

        g.dispose();
        javax.imageio.ImageIO.write(image, "png", new File(outputDir, imgName));
    }

    private static void drawSignal(java.awt.Graphics2D g, DbcField field, List<CANPacket> packets, double minTime, double duration, java.awt.Color color, String label, int labelYOffset) {
        if (packets == null || packets.isEmpty()) return;

        double minVal = Double.MAX_VALUE;
        double maxVal = -Double.MAX_VALUE;
        for (CANPacket p : packets) {
            double v = field.getValue(p);
            minVal = Math.min(minVal, v);
            maxVal = Math.max(maxVal, v);
        }
        
        double range = maxVal - minVal;
        if (range == 0) range = 1;

        g.setColor(color);
        int prevX = -1;
        int prevY = -1;
        
        for (CANPacket p : packets) {
            int x = (int) ((p.getTimeStampMs() - minTime) / duration * (DbcImageTool.WIDTH - 1));
            int y = DbcImageTool.HEIGHT - 1 - (int) ((field.getValue(p) - minVal) / range * (DbcImageTool.HEIGHT - 1));
            
            if (prevX != -1) {
                g.drawLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }
        
        g.drawString(label + " (Min: " + String.format("%.2f", minVal) + " Max: " + String.format("%.2f", maxVal) + ")", 10, 20 + labelYOffset);
    }

    private static class Match {
        DbcField f2;
        DbcField f1;
        double distance;

        Match(DbcField f2, DbcField f1, double distance) {
            this.f2 = f2;
            this.f1 = f1;
            this.distance = distance;
        }
    }
}
