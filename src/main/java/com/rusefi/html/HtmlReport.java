package com.rusefi.html;

import com.rusefi.can.analysis.ByteRateOfChange;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HtmlReport {
    public static void main(String[] args) throws IOException {
        List<ByteRateOfChange.ByteId> keys = new ArrayList<>();
        for (int sid = 0x200; sid < 0x210; sid++) {
            for (int index = 0; index < 7; index++) {
                keys.add(new ByteRateOfChange.ByteId(sid, index));
            }
        }

        List<String> fileNames = Arrays.asList("hello", "another2", "another3", "another4");


        printHtml(fileNames, keys);

    }

    private static void printHtml(List<String> fileNames, List<ByteRateOfChange.ByteId> keys) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter("hello.html"));

        writeHtmlHead(w);

        startBodyAndTableHeader(fileNames, w);


        for (ByteRateOfChange.ByteId key : keys) {
            w.write("  <tr>\n");


            w.write("    <td>\n" +
                    key.getLogKey() + "      \n" +
                    "    </td>");

            for (String file : fileNames) {
                w.write("<td>" + key.getLogKey() + " </td>");
            }

            w.write("  </tr>\n");

        }

        endTableAndBody(w);


    }

    private static void endTableAndBody(BufferedWriter w) throws IOException {
        w.write("</table>\n" +
                "</body>\n" +
                "</html>");
    }

    private static void startBodyAndTableHeader(List<String> fileNames, BufferedWriter w) throws IOException {
        w.write(
                "<body>\n" +
                        "\n" +
                        "<table cellpadding=\"2\" border=\"1\" cellspacing=\"2\" align=\"center\">\n" +
                        "  <tr>\n");


        w.write("    <td>\n" +
                "      <div>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/>*<br/></div>\n" +
                "    </td>");

        for (String file : fileNames) {
            w.write("    <td>\n" +
                    "      <div class='rotate'>" + file + "</div>\n" +
                    "    </td>");

        }

        w.write("</tr>");
    }

    private static void writeHtmlHead(BufferedWriter w) throws IOException {
        w.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                ".rotate {\n" +
                "  /* FF3.5+ */\n" +
                "  -moz-transform: rotate(-90.0deg);\n" +
                "  /* Opera 10.5 */\n" +
                "  -o-transform: rotate(-90.0deg);\n" +
                "  /* Saf3.1+, Chrome */\n" +
                "  -webkit-transform: rotate(-90.0deg);\n" +
                "  /* IE6,IE7 */\n" +
                "  filter: progid: DXImageTransform.Microsoft.BasicImage(rotation=0.083);\n" +
                "  /* IE8 */\n" +
                "  -ms-filter: \"progid:DXImageTransform.Microsoft.BasicImage(rotation=0.083)\";\n" +
                "  /* Standard */\n" +
                "  transform: rotate(-90.0deg);\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n");
    }
}
