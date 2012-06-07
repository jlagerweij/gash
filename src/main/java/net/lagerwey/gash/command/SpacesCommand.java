package net.lagerwey.gash.command;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.core.space.SpaceType;
import org.openspaces.pu.container.jee.JeeServiceDetails;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Displays available spaces in grid service containers.
 */
public class SpacesCommand implements Command {

    private static final char VERTICAL = '|';
    private static final char HORIZONTAL = '-';
    private static final String BEGIN_ELBOW = new String(new char[]{'+', HORIZONTAL, HORIZONTAL});
    private static final String BEGIN_VERTICAL_AND_RIGHT = new String(new char[]{'+', HORIZONTAL, HORIZONTAL});

    @Override
    public void perform(Admin admin, String command, String arguments) {
        boolean showAll = false;
        if ("-a".equals(arguments)) {
            showAll = true;
        }
        for (Machine machine : admin.getMachines()) {
            Utils.println("%s", machine.getHostName());
            GridServiceContainer[] containers = machine.getGridServiceContainers().getContainers();
            Arrays.sort(containers, new Comparator<GridServiceContainer>() {
                @Override
                public int compare(GridServiceContainer o1, GridServiceContainer o2) {
                    return new Integer(o1.getAgentId()).compareTo(o2.getAgentId());
                }
            });
            int containerCount = 0;
            for (GridServiceContainer container : containers) {
                containerCount++;
                String zones = null;
                for (String zoneName : container.getZones().keySet()) {
                    if (zones == null) {
                        zones = zoneName;
                    } else {
                        zones += ", " + zoneName;
                    }
                }
                if (zones == null) {
                    zones = "";
                } else {
                    zones = "Zones [" + zones + "]";
                }
                int i = container.getAgentId();
                String containerTree = BEGIN_VERTICAL_AND_RIGHT;
                char containerVertical = VERTICAL;
                if (containerCount == containers.length) {
                    containerTree = BEGIN_ELBOW;
                    containerVertical = ' ';
                }
                if (showAll) {
                    Utils.println("%s GSC-%s [pid %s] uid[%s] %s",
                            containerTree,
                            i,
                            container.getVirtualMachine().getDetails().getPid(),
                            container.getUid(),
                            zones);
                } else {
                    Utils.println("%s GSC-%s [pid %s] %s",
                            containerTree,
                            i,
                            container.getVirtualMachine().getDetails().getPid(),
                            zones);
                }

                int processingUnitCount = 0;
                ProcessingUnitInstance[] processingUnitInstances = container.getProcessingUnitInstances();
                sortProcessingUnitInstance(processingUnitInstances);
                for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances) {
                    processingUnitCount++;
                    String processingUnitTree = BEGIN_VERTICAL_AND_RIGHT;
                    char processingUnitVertical = VERTICAL;
                    if (processingUnitCount == processingUnitInstances.length) {
                        processingUnitTree = BEGIN_ELBOW;
                        processingUnitVertical = ' ';
                    }
                    String primary = (processingUnitInstance.getSpaceInstance() == null || processingUnitInstance
                            .getSpaceInstance().getMode().equals(SpaceMode.PRIMARY) ? "" : "Backup");
                    StringBuilder puNameAndClusterInfo = Utils.getPUNameAndClusterInfo(processingUnitInstance);
                    if (processingUnitInstance.isJee()) {
                        puNameAndClusterInfo.append(" Web");
                    }

                    if (showAll) {
                        Utils.println("%s   %s %s %s id[%s]",
                                containerVertical,
                                processingUnitTree,
                                puNameAndClusterInfo,
                                primary,
                                processingUnitInstance.getUid());
                    } else {
                        Utils.println("%s   %s %s %s",
                                containerVertical,
                                processingUnitTree,
                                puNameAndClusterInfo,
                                primary);
                    }

                    if (processingUnitInstance.isJee()) {
                        JeeServiceDetails jeeDetails = processingUnitInstance.getJeeDetails();
                        if (jeeDetails.getPort() > 0) {
                            String jeeTree = BEGIN_ELBOW;
                            if (jeeDetails.getSslPort() > 0) {
                                jeeTree = BEGIN_VERTICAL_AND_RIGHT;
                            }
                            Utils.println("%s   %s   %s http://%s:%s%s",
                                    containerVertical,
                                    processingUnitVertical,
                                    jeeTree,
                                    jeeDetails.getHost(),
                                    jeeDetails.getPort(),
                                    jeeDetails.getContextPath());
                        }
                        if (jeeDetails.getSslPort() > 0) {
                            Utils.println("%s   %s   %s https://%s:%s%s",
                                    containerVertical,
                                    processingUnitVertical,
                                    BEGIN_ELBOW,
                                    jeeDetails.getHost(),
                                    jeeDetails.getSslPort(),
                                    jeeDetails.getContextPath());
                        }
                    } else {
                        for (SpaceServiceDetails details : processingUnitInstance.getSpaceDetails()) {

                            if (details.getSpaceType().equals(SpaceType.REMOTE)) {
                                if (showAll) {
                                    Utils.println("%s   %s   %s %s [%s] %s",
                                            containerVertical,
                                            processingUnitVertical,
                                            BEGIN_ELBOW,
                                            details.getId(),
                                            details.getLongDescription(),
                                            "Remote");
                                }
                            } else {
                                String instancePrimary = (
                                        processingUnitInstance.getSpaceInstance().getMode()
                                                .equals(SpaceMode.PRIMARY) ? " " : " Backup");
                                if (showAll) {
                                    Utils.println("%s   %s   %s %s %s serviceId[%s]",
                                            containerVertical,
                                            processingUnitVertical,
                                            BEGIN_ELBOW,
                                            details.getId(),
                                            instancePrimary,
                                            details.getServiceID());
                                } else {
                                    Utils.println("%s   %s   %s %s %s",
                                            containerVertical,
                                            processingUnitVertical,
                                            BEGIN_ELBOW,
                                            details.getId(),
                                            instancePrimary);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String description() {
        return "Lists all Spaces.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }

    /**
     * Sorts processing unit instances by their name.
     *
     * @param processingUnitsArray Sorted list of processing unit instances.
     */
    private static void sortProcessingUnitInstance(ProcessingUnitInstance[] processingUnitsArray) {
        Arrays.sort(processingUnitsArray, new Comparator<ProcessingUnitInstance>() {
            public int compare(ProcessingUnitInstance o1, ProcessingUnitInstance o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

}
