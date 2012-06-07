package net.lagerwey.gash;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.core.cluster.ClusterInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import static java.lang.String.format;

/**
 */
public class Utils {

    public static boolean debugEnabled = false;

    private static void write(String string) {
        try {
            System.out.write(string.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeError(String string) {
        try {
            System.err.write(string.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void print(String format, Object... args) {
        write(format(format, args));
//        OUT.print(format(format, args));
    }

    public static void println(String format, Object... args) {
        write(format(format, args));
        write("\n");
    }

    public static void error(String format, Object... args) {
        writeError(format(format, args));
        writeError("\n");
    }

    public static void warn(String format, Object... args) {
        write(format("WARN: " + format, args));
        write("\n");
    }

    public static void sortProcessingUnits(ProcessingUnit[] processingUnitsArray) {
        Arrays.sort(processingUnitsArray, new Comparator<ProcessingUnit>() {
            public int compare(ProcessingUnit o1, ProcessingUnit o2) {
                ProcessingUnitInstance[] o1Instances = o1.getInstances();
                if (o1Instances.length == 0) {
                    return 0;
                }
                ProcessingUnitInstance[] o2Instances = o2.getInstances();
                if (o2Instances.length == 0) {
                    return 0;
                }
                if (o1Instances[0].isJee() == o2Instances[0].isJee()) {
                    return o1.getName().compareTo(o2.getName());
                }
                return Boolean.valueOf(o2Instances[0].isJee()).compareTo(o1Instances[0].isJee());
            }
        });
    }

    public static StringBuilder getPUNameAndClusterInfo(ProcessingUnitInstance processingUnitInstance) {
        StringBuilder puNameAndClusterInfo = new StringBuilder(processingUnitInstance.getName());
        ProcessingUnitPartition partition = processingUnitInstance.getPartition();

        ClusterInfo clusterInfo = processingUnitInstance.getClusterInfo();
        if (clusterInfo.getNumberOfBackups() > 0) {
            puNameAndClusterInfo.append(".");
            puNameAndClusterInfo.append(partition.getPartitionId() + 1);

            puNameAndClusterInfo.append(" [");
            puNameAndClusterInfo.append(processingUnitInstance.getBackupId() + 1);
            puNameAndClusterInfo.append("]");
        } else {
            puNameAndClusterInfo.append(" [");
            puNameAndClusterInfo.append(clusterInfo.getInstanceId());
            puNameAndClusterInfo.append("]");
        }
        return puNameAndClusterInfo;
    }


}
