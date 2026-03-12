package com.rusefi.can.analysis.matcher;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.DbcField;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.rusefi.can.analysis.MatchFinder;

public class PacketsHelper {
    private final List<CANPacket> packets;
    public Map<Integer, List<CANPacket>> packetsById;

    private static final int SAMPLES = 1000;


    public static double calculateDistance(double[] ts1, double[] ts2) {
        double dist = 0;
        for (int i = 0; i < SAMPLES; i++) {
            dist += Math.abs(ts1[i] - ts2[i]);
        }
        return dist / SAMPLES;
    }

    public PacketsHelper(List<CANPacket> packets) {
        this.packets = packets;
        packetsById = packets.stream().collect(Collectors.groupingBy(CANPacket::getId));

    }

    public double[] getNormalizedTimeSeries(DbcField field) {
        List<CANPacket> p2 = packetsById.get(field.getSid());
        if (p2 == null || p2.isEmpty())
            return null;

        double minTime = getMinTime();
        double duration = getDuration();
        double[] values = new double[SAMPLES];
        int count = 0;
        double sum = 0;
        double sumSq = 0;

        int packetIdx = 0;
        for (int i = 0; i < SAMPLES; i++) {
            double targetTime = minTime + (double) i / (SAMPLES - 1) * duration;
            while (packetIdx < p2.size() - 1 && p2.get(packetIdx + 1).getTimeStampMs() < targetTime) {
                packetIdx++;
            }
            double val = field.getValue(p2.get(packetIdx));
            values[i] = val;
            sum += val;
            sumSq += val * val;
            count++;
        }

        if (count == 0)
            return null;
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

    public double getDuration() {
        double minTime = getMinTime();
        double maxTime = getMaxTime();
        return maxTime - minTime;
    }

    private double getMaxTime() {
        return packets.get(packets.size() - 1).getTimeStampMs();
    }

    public double getMinTime() {
        return packets.get(0).getTimeStampMs();
    }
}
