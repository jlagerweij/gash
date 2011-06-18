package net.lagerwey.gash;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.core.cluster.ClusterInfo;

import java.util.Arrays;
import java.util.Comparator;

import static java.lang.String.format;

/**
 */
public class Utils {

    public static boolean debugEnabled = false;

    public static void info(String format, Object... args) {
        System.out.println(format(format, args));
    }

    public static void error(String format, Object... args) {
        System.err.println(format(format, args));
    }

    public static void debug(String format, Object... args) {
        if (debugEnabled) {
            System.out.println(format("DEBUG: " + format, args));
        }
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
