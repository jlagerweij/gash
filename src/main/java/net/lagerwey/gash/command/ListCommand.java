package net.lagerwey.gash.command;

import com.j_spaces.jdbc.driver.GConnection;
import net.lagerwey.gash.CurrentWorkingLocation;
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
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static net.lagerwey.gash.Utils.sortProcessingUnits;

/**
 * List directory contents. A directory can be a space, partitionIds, objecttypes or objects.
 */
public class ListCommand implements Command {

    private CurrentWorkingLocation currentWorkingLocation;

    /**
     * Constructs a ListCommand with a ChangeDirectoryCommand which holds the current directory.
     *
     * @param currentWorkingLocation Current directory.
     */
    public ListCommand(CurrentWorkingLocation currentWorkingLocation) {
        this.currentWorkingLocation = currentWorkingLocation;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        if (currentWorkingLocation.getCurrentMountpoint() == null) {
            Utils.info("logs/");
            Utils.info("spaces/");
        } else {
            if ("logs".equals(currentWorkingLocation.getCurrentMountpoint())) {
                listMountpointLogs(admin);
            } else if ("spaces".equals(currentWorkingLocation.getCurrentMountpoint())) {
                listMountpointSpaces(admin, arguments);
            }
        }
    }

    private void listMountpointLogs(Admin admin) {
        if (currentWorkingLocation.isInLogs()) {
            for (Machine machine : admin.getMachines()) {
                Utils.info("%s", machine.getHostName());
            }
        } else if (currentWorkingLocation.isInHostname()) {
            Machine machine = admin.getMachines().getMachineByHostName(currentWorkingLocation.getLogLocation()
                                                                                         .getHostname());
            if (machine != null) {
                for (GridServiceAgent gridServiceAgent : machine.getGridServiceAgents()) {
                    Utils.info("GSA");
                }
                for (GridServiceManager gridServiceManager : machine.getGridServiceManagers()) {
                    Utils.info("GSM-%s", gridServiceManager.getAgentId());
                }
                for (GridServiceContainer gridServiceContainer : machine.getGridServiceContainers()) {
                    Utils.info("GSC-%s", gridServiceContainer.getAgentId());
                }
            }
        }
    }

    private void listMountpointSpaces(Admin admin, String arguments) {
        if (currentWorkingLocation.isInGrid()) {
            listSpaces(admin);
        } else if (currentWorkingLocation.isInSpace()) {
            listPartitions(admin);
        } else if (currentWorkingLocation.isInPartition()) {
            listObjectTypes(admin, arguments);
        } else if (currentWorkingLocation.isInObject()) {
            listObjects(admin, arguments);
        }
    }

    /**
     * Lists the spaces.
     *
     * @param admin GigaSpaces Admin object.
     */
    private void listSpaces(Admin admin) {
        Utils.info("total %s processing units, %s spaces.",
                   admin.getProcessingUnits().getSize(),
                   admin.getSpaces().getNames().size());
        Utils.info("%-35s %-35s %-20s %-10s", "SpaceName", "PU Name", "PU Status", "Type");

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
                Utils.info("%-35s %-35s %-20s %-10s", spaceName, puName, puStatus, type);
            } else {
                for (Space space : processingUnit.getSpaces()) {
                    spaceName = space.getName();
                    String type = "";
                    if (processingUnit.getInstances()[0].isJee()) {
                        type = "Web";
                    }
                    String puStatus = processingUnit.getStatus().toString();
                    Utils.info("%-35s %-35s %-20s %-10s", spaceName, puName, puStatus, type);
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
        Space spaceByName = admin.getSpaces().getSpaceByName(currentWorkingLocation.getSpaceName());
        SpacePartition[] partitions = spaceByName.getPartitions();
        Arrays.sort(partitions, new Comparator<SpacePartition>() {
            @Override
            public int compare(SpacePartition o1, SpacePartition o2) {
                return o1.getPartitionId() - o2.getPartitionId();
            }
        });
        Utils.info("total %s partitions", partitions.length);
        for (SpacePartition spacePartition : partitions) {
            Utils.info("%s", spacePartition.getPartitionId());
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
            Utils.info("%s", space);
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

        Utils.info("%s classes.", objects);
    }

    /**
     * Lists the objects.
     *
     * @param admin     GigaSpaces Admin objects.
     * @param arguments Arguments to filter which objects to list.
     */
    private void listObjects(Admin admin, String arguments) {
        String query = String.format("SELECT * FROM %s %s",
                                     currentWorkingLocation.getObjectType(),
                                     arguments == null ? "WHERE rownum < 10" : arguments);
        int objects = executeQuery(admin, query);
        Utils.info("%s objects.", objects);
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
            System.out.println("Query: " + query);
            Space spaceByName = admin.getSpaces().getSpaceByName(currentWorkingLocation.getSpaceName());
            SpacePartition partition = spaceByName.getPartition(Integer.parseInt(currentWorkingLocation
                                                                                         .getPartitionId()));
            GConnection conn = GConnection.getInstance(partition.getPrimary().getGigaSpace().getSpace());
            conn.setUseSingleSpace(true);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            nrOfObjects = prettyPrintResultSet(conn, rs);
            rs.close();
            st.close();
            conn.close();
        } catch (Exception e) {
            if (Utils.debugEnabled) {
                e.printStackTrace();
            } else {
                Utils.info("ERROR: %s", e.getMessage());
            }
        }
        return nrOfObjects;
    }

    private int prettyPrintResultSet(GConnection conn, ResultSet rs) throws SQLException {
        int nrOfObjects = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String columnName = rs.getMetaData().getColumnName(i);
            sb.append(columnName.substring(columnName.lastIndexOf(".") + 1));
            sb.append("\t");
        }
        Utils.info("%s", "__________________________________________________________________________________");
        Utils.info("%s", sb.toString());
        Utils.info("%s", "----------------------------------------------------------------------------------");

        while (rs.next()) {
            nrOfObjects++;
            sb.setLength(0);
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                sb.append(rs.getString(i));
                sb.append("\t");
            }
            if (!StringUtils.hasText(currentWorkingLocation.getObjectType())) {
                Statement countSt = conn.createStatement();
                ResultSet countRs = countSt.executeQuery("SELECT COUNT(*) FROM " + sb.toString().trim());
                while (countRs.next()) {
                    sb.append(countRs.getString(1));
                }
                countRs.close();
                countSt.close();
            }
            Utils.info("%s", sb.toString());
        }
        return nrOfObjects;
    }

    @Override
    public String description() {
        return "Lists spaces.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }
}
