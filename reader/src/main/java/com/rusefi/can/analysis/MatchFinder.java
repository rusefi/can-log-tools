package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.analysis.matcher.PacketsHelper;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.DbcPacket;
import com.rusefi.can.render.DbcImageTool;
import com.rusefi.can.tool.ValidateDbc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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

        PacketsHelper content1 = new PacketsHelper(packets1);
        PacketsHelper content2 = new PacketsHelper(packets2);

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
            double[] ts2 = content2.getNormalizedTimeSeries(f2);
            if (ts2 == null)
                continue;

            Match bestMatch = null;
            double minDistance = Double.MAX_VALUE;

            for (DbcField f1 : fields1) {
                double[] ts1 = content1.getNormalizedTimeSeries(f1);
                if (ts1 == null)
                    continue;

                double distance = PacketsHelper.calculateDistance(ts1, ts2);
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

        createHtmlReport(matches, content1, content2, outputDir);
    }

    private static List<DbcField> getAllFields(DbcFile dbc) {
        List<DbcField> fields = new ArrayList<>();
        for (DbcPacket packet : dbc.values()) {
            fields.addAll(packet.getFields());
        }
        return fields;
    }

    private static void createHtmlReport(List<Match> matches,
                                         PacketsHelper content1, PacketsHelper content2,
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

                renderComparison(match, content1.packetsById.get(match.f1.getSid()), content1.getMinTime(), content1.getDuration(),
                        content2.packetsById.get(match.f2.getSid()), content2.getMinTime(), content2.getDuration(),
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
