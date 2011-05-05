package net.lagerwey.gash.command;

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

    @Override
    public void perform(Admin admin, String command, String arguments) {
        boolean showAll = false;
        if ("-a".equals(arguments)) {
            showAll = true;
        }
        for (Machine machine : admin.getMachines()) {
            Utils.info("%s", machine.getHostName());
            GridServiceContainer[] containers = machine.getGridServiceContainers().getContainers();
            Arrays.sort(containers, new Comparator<GridServiceContainer>() {
                @Override
                public int compare(GridServiceContainer o1, GridServiceContainer o2) {
                    return new Integer(o1.getAgentId()).compareTo(o2.getAgentId());
                }
            });
            for (GridServiceContainer container : containers) {
                int i = container.getAgentId();
                if (showAll) {
                    Utils.info("- GSC-%s [pid %s] uid[%s]",
                               i,
                               container.getVirtualMachine().getDetails().getPid(),
                               container.getUid());
                } else {
                    Utils.info("- GSC-%s [pid %s]", i, container.getVirtualMachine().getDetails().getPid());
                }

                ProcessingUnitInstance[] processingUnitInstances = container.getProcessingUnitInstances();
                sortProcessingUnitInstance(processingUnitInstances);
                for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances) {
                    String primary = (processingUnitInstance.getBackupId() == 0 ? "" : "Backup");
                    StringBuilder puNameAndClusterInfo = Utils.getPUNameAndClusterInfo(processingUnitInstance);
                    if (processingUnitInstance.isJee()) {
                        puNameAndClusterInfo.append(" Web");
                    }

                    if (showAll) {
                        Utils.info("   + %s %s id[%s]", puNameAndClusterInfo, primary, processingUnitInstance.getUid());
                    } else {
                        Utils.info("   + %s %s", puNameAndClusterInfo, primary);
                    }

                    if (processingUnitInstance.isJee()) {
                        JeeServiceDetails jeeDetails = processingUnitInstance.getJeeDetails();
                        if (jeeDetails.getPort() > 0) {
                            Utils.info("      - http://%s:%s%s",
                                       jeeDetails.getHost(),
                                       jeeDetails.getPort(),
                                       jeeDetails.getContextPath());
                        }
                        if (jeeDetails.getSslPort() > 0) {
                            Utils.info("      - https://%s:%s%s",
                                       jeeDetails.getHost(),
                                       jeeDetails.getSslPort(),
                                       jeeDetails.getContextPath());
                        }
                    } else {
                        for (SpaceServiceDetails details : processingUnitInstance.getSpaceDetails()) {

                            if (details.getSpaceType().equals(SpaceType.REMOTE)) {
                                if (showAll) {
                                    Utils.info("      - %s [%s] %s",
                                               details.getId(),
                                               details.getLongDescription(),
                                               "Remote");
                                }
                            } else {
                                String instancePrimary = (
                                        processingUnitInstance.getSpaceInstance().getBackupId()
                                                == 0 ? " " : " Backup");
                                if (showAll) {
                                    Utils.info("      - %s %s serviceId[%s]",
                                               details.getId(),
                                               instancePrimary,
                                               details.getServiceID());
                                } else {
                                    Utils.info("      - %s %s", details.getId(), instancePrimary);
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
