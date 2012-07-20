package net.lagerwey.gash.command;

import com.j_spaces.core.IJSpace;
import com.j_spaces.jdbc.driver.GConnection;
import net.lagerwey.gash.Gash;
import net.lagerwey.gash.GashConnection;
import net.lagerwey.gash.PrettyPrintUtils;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.core.space.SpaceType;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static net.lagerwey.gash.Utils.sortProcessingUnits;

/**
 * List directory contents. A directory can be a space, partitionIds, objecttypes or objects.
 */
public class ListCommand extends AbstractCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public ListCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        if (!gash.getWorkingLocation().hasConnection()) {
            if (gash.getConnectionManager().hasConnections()) {
                gash.getConnectionManager().listConnections();
            } else {
                Utils.error("No available connections");
            }
        } else {
            if (!gash.getWorkingLocation().hasMountpoint()) {
                Utils.println("logs/");
                Utils.println("spaces/");
            } else {
                Admin admin = gash.getWorkingLocation().getCurrentConnection().getAdmin();
                if ("logs".equals(gash.getWorkingLocation().getCurrentMountpoint())) {
                    listMountpointLogs(admin);
                } else if ("spaces".equals(gash.getWorkingLocation().getCurrentMountpoint())) {
                    listMountpointSpaces(admin, arguments);
                }
            }
        }
    }

    private void listMountpointLogs(Admin admin) {
        if (gash.getWorkingLocation().isInLogs()) {
            Machine[] machines = admin.getMachines().getMachines();
            Arrays.sort(machines, new Comparator<Machine>() {
                @Override
                public int compare(Machine o1, Machine o2) {
                    return o1.getHostName().compareToIgnoreCase(o2.getHostName());
                }
            });
            for (Machine machine : machines) {
                Utils.println("%s", machine.getHostName());
            }
            Utils.println("GSA");
            Utils.println("GSM");
            Utils.println("GSC");
        } else if (gash.getWorkingLocation().isInHostname()) {
            Machine machine = admin.getMachines().getMachineByHostName(gash.getWorkingLocation().getLogLocation()
                                                                               .getHostname());
            if (machine != null) {
                for (GridServiceAgent gridServiceAgent : machine.getGridServiceAgents()) {
                    Utils.println("GSA");
                }
                for (GridServiceManager gridServiceManager : machine.getGridServiceManagers()) {
                    Utils.println("GSM-%s", gridServiceManager.getAgentId());
                }
                for (GridServiceContainer gridServiceContainer : machine.getGridServiceContainers()) {
                    Utils.println("GSC-%s", gridServiceContainer.getAgentId());
                }
            }
        }
    }

    private void listMountpointSpaces(Admin admin, String arguments) {
        if (gash.getWorkingLocation().isInGrid()) {
            listSpaces(admin);
        } else if (gash.getWorkingLocation().isInSpace()) {
            listPartitions(admin);
        } else if (gash.getWorkingLocation().isInPartition()) {
            listObjectTypes(admin, arguments);
        } else if (gash.getWorkingLocation().isInObject()) {
            listObjects(admin, arguments);
        }
    }

    /**
     * Lists the spaces.
     *
     * @param admin GigaSpaces Admin object.
     */
    private void listSpaces(Admin admin) {
        Utils.println("total %s processing units, %s spaces.",
                      admin.getProcessingUnits().getSize(),
                      admin.getSpaces().getNames().size());
        Utils.println("%-35s %-35s %-20s %-10s", "SpaceName", "PU Name", "PU Status", "Type");

        ProcessingUnits processingUnits = admin.getProcessingUnits();
        ProcessingUnit[] processingUnitsArray = processingUnits.getProcessingUnits();
        sortProcessingUnits(processingUnitsArray);
        for (ProcessingUnit processingUnit : processingUnitsArray) {
            String puName = processingUnit.getName();
            String spaceName = "-";
            if (processingUnit.getSpaces().length == 0) {
                String puStatus = processingUnit.getStatus().toString();
                String type = "";
                if (processingUnit.getInstances().length > 0 && processingUnit.getInstances()[0].isJee()) {
                    type = "Web";
                }
                Utils.println("%-35s %-35s %-20s %-10s", spaceName, puName, puStatus, type);
            } else {
                for (Space space : processingUnit.getSpaces()) {
                    spaceName = space.getName();
                    String type = "";
                    if (processingUnit.getInstances()[0].isJee()) {
                        type = "Web";
                    }
                    String puStatus = processingUnit.getStatus().toString();
                    Utils.println("%-35s %-35s %-20s %-10s", spaceName, puName, puStatus, type);
                }
            }
        }
    }

    /**
     * Lists the partitions.
     *
     * @param admin GigaSpaces Admin object.
     */
    private void listPartitions(Admin admin) {
        Space spaceByName = admin.getSpaces().getSpaceByName(gash.getWorkingLocation().getSpaceName());
        SpacePartition[] partitions = spaceByName.getPartitions();
        Arrays.sort(partitions, new Comparator<SpacePartition>() {
            @Override
            public int compare(SpacePartition o1, SpacePartition o2) {
                return o1.getPartitionId() - o2.getPartitionId();
            }
        });
        Utils.println("total %s partitions", partitions.length);
        for (SpacePartition spacePartition : partitions) {
            Utils.println("%s", spacePartition.getPartitionId());
        }

        List<String> spaces = new ArrayList<String>();
        for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
            for (ProcessingUnitInstance processingUnitInstance : processingUnit.getInstances()) {
                if (processingUnitInstance.getSpaceInstance() == null) {
                    continue;
                }
                if (processingUnitInstance.getSpaceInstance().getSpace() == null) {
                    continue;
                }
                if (processingUnitInstance.getSpaceInstance().getSpace().getName().equals(spaceByName.getName())) {
                    for (SpaceServiceDetails spaceServiceDetails : processingUnitInstance.getSpaceDetails()) {
                        String spaceServiceDetailsName = spaceServiceDetails.getName();
                        if (spaceServiceDetails.getSpaceType().equals(SpaceType.REMOTE)
                                || spaceServiceDetails.getSpaceType().equals(SpaceType.LOCAL_CACHE)
                                || spaceServiceDetails.getSpaceType().equals(SpaceType.LOCAL_VIEW)) {
                            // Remote space, Local Cache or Local views
                            if (!spaces.contains(spaceServiceDetailsName) && !spaceByName.getName().equals(
                                    spaceServiceDetailsName)) {
                                spaces.add(spaceServiceDetailsName);
                            }
                        }
                    }
                }
            }
        }
        for (String space : spaces) {
            Utils.println("%s", space);
        }
    }

    /**
     * Lists the objects.
     *
     * @param admin     GigaSpaces Admin objects.
     * @param arguments Arguments to filter which objects to list.
     */
    private void listObjectTypes(Admin admin, String arguments) {
        String query = "SELECT * FROM SYSTABLES";
        int objects = executeQuery(admin, query);

        Utils.println("%s classes.", objects);
    }

    /**
     * Lists the objects.
     *
     * @param admin     GigaSpaces Admin objects.
     * @param arguments Arguments to filter which objects to list.
     */
    private void listObjects(Admin admin, String arguments) {
        String query = String.format("SELECT * FROM %s %s",
                                     gash.getWorkingLocation().getObjectType(),
                                     arguments == null ? "WHERE rownum < 10" : arguments);
        int objects = executeQuery(admin, query);
        Utils.println("%s objects.", objects);
    }

    /**
     * Executes a query.
     *
     * @param admin GigaSpaces Admin object.
     * @param query Query to execute.
     * @return Number of rows as the result of the query.
     */
    private int executeQuery(Admin admin, String query) {
        int nrOfObjects = 0;
        try {
            Utils.println("Query: " + query);
            Space spaceByName = admin.getSpaces().getSpaceByName(gash.getWorkingLocation().getSpaceName());
            SpacePartition partition = spaceByName.getPartition(Integer.parseInt(gash.getWorkingLocation()
                                                                                         .getPartitionId()));
            IJSpace space = partition.getPrimary().getGigaSpace().getSpace();
            GConnection conn = GConnection.getInstance(space);
            conn.setUseSingleSpace(true);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            nrOfObjects = PrettyPrintUtils.prettyPrintResultSet(gash.getWorkingLocation(), space, conn, rs);
            rs.close();
            st.close();
            conn.close();
        } catch (Exception e) {
            if (Utils.debugEnabled) {
                e.printStackTrace();
            } else {
                Utils.println("ERROR: %s", e.getMessage());
            }
        }
        return nrOfObjects;
    }

    @Override
    public String description() {
        return "Lists the items in the current working location";
    }

    @Override
    public void showHelp() {
        Utils.println("Usage: ls");
    }
}
