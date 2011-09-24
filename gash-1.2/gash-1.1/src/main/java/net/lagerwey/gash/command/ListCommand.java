package net.lagerwey.gash.command;

import com.j_spaces.jdbc.driver.GConnection;
import net.lagerwey.gash.CurrentWorkingSpace;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpacePartition;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static net.lagerwey.gash.Utils.sortProcessingUnits;

/**
 * List directory contents. A directory can be a space, partitionIds, objecttypes or objects.
 */
public class ListCommand implements Command {

    private CurrentWorkingSpace currentWorkingSpace;

    /**
     * Constructs a ListCommand with a ChangeDirectoryCommand which holds the current directory.
     *
     * @param currentWorkingSpace Current directory.
     */
    public ListCommand(CurrentWorkingSpace currentWorkingSpace) {
        this.currentWorkingSpace = currentWorkingSpace;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        if (!StringUtils.hasText(currentWorkingSpace.getSpaceName())) {
            listSpaces(admin);
        } else {
            if (!StringUtils.hasText(currentWorkingSpace.getPartitionId())) {
                listPartitions(admin);
            } else {
                listObjects(admin, arguments);
            }
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

//        for (Space space : admin.getSpaces()) {
//            String name = space.getName();
//            Utils.info("%s", name);
//        }
    }

    /**
     * Lists the partitions.
     *
     * @param admin GigaSpaces Admin object.
     */
    private void listPartitions(Admin admin) {
        Space spaceByName = admin.getSpaces().getSpaceByName(currentWorkingSpace.getSpaceName());
        Utils.info("total %s partitions", spaceByName.getPartitions().length);
        for (SpacePartition spacePartition : spaceByName.getPartitions()) {
            Utils.info("%s", spacePartition.getPartitionId());
        }

//        for (Space space : spaceByName.getSpaces()) {
//            Utils.info("%s", space.getName());
//        }
    }

    /**
     * Lists the objects.
     *
     * @param admin     GigaSpaces Admin objects.
     * @param arguments Arguments to filter which objects to list.
     */
    private void listObjects(Admin admin, String arguments) {
        String query;
        if (StringUtils.hasText(currentWorkingSpace.getObjectType())) {
            query = String.format("SELECT * FROM %s %s",
                                  currentWorkingSpace.getObjectType(),
                                  arguments == null ? "WHERE rownum < 10" : arguments);
        } else {
            query = "SELECT * FROM SYSTABLES";
        }
        int objects = executeQuery(admin, query);

        if (StringUtils.hasText(currentWorkingSpace.getObjectType())) {
            Utils.info("%s objects.", objects);
        } else {
            Utils.info("%s classes.", objects);
        }
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
            Space spaceByName = admin.getSpaces().getSpaceByName(currentWorkingSpace.getSpaceName());
            SpacePartition partition = spaceByName.getPartition(Integer.parseInt(currentWorkingSpace.getPartitionId()));
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
            if (!StringUtils.hasText(currentWorkingSpace.getObjectType())) {
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
