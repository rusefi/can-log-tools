package com.rusefi.can.analysis;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class CanMetaDataContext {
    final Set<Integer> withChecksum = new HashSet<>();
    final Map<Integer, Map<Integer, Integer>> countersMap;

    Set<ByteRateOfChange.ByteId> counterBytes = new HashSet<>();

    private CanMetaDataContext(List<Integer> withChecksum, Map<Integer, Map<Integer, Integer>> countersMap) {
        this.countersMap = countersMap;
        this.withChecksum.addAll(withChecksum);

        for (Map.Entry<Integer, Map<Integer, Integer>> e : countersMap.entrySet()) {
            int sid = e.getKey();
            Map<Integer, Integer> v = e.getValue();

            for (Map.Entry<Integer, Integer> e2 : v.entrySet()) {
                int bitIndex = e2.getKey();
                int size = e2.getValue();

                if (size > 4) {
                    int byteIndex = bitIndex / 8;
                    counterBytes.add(new ByteRateOfChange.ByteId(sid, byteIndex));
                }
            }
        }
    }

    public static CanMetaDataContext read(String inputFolderName) throws FileNotFoundException {
        Yaml checksum = new Yaml();
        String checkSumFileName = inputFolderName + File.separator + ChecksumScanner.CHECKSUM_YAML;
        if (!new File(checkSumFileName).exists())
            return new CanMetaDataContext(new ArrayList<>(), new HashMap<>());

        List<Integer> withChecksum = checksum.load(new FileReader(checkSumFileName));

        Yaml countersYaml = new Yaml();
        Map<Integer, Map<Integer, Integer>> countersMap = countersYaml.load(new FileReader(inputFolderName + File.separator + CounterScanner.COUNTERS_YAML));

        return new CanMetaDataContext(withChecksum, countersMap);
    }
}
