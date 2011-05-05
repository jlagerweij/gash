package net.lagerwey.gash.command;

import com.j_spaces.jdbc.driver.GConnection;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpacePartition;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;

import static net.lagerwey.gash.Utils.sortProcessingUnits;

/**
 * List directory contents.
 * A directory can be a space, partitionIds, objecttypes or objects.
 */
public class ListCommand implements Command {

    private ChangeDirectoryCommand directoryCmd;

    /**
     * Constructs a ListCommand with a ChangeDirectoryCommand which holds the current directory.
     * @param directoryCmd Current directory.
     */
    public ListCommand(ChangeDirectoryCommand directoryCmd) {
        this.directoryCmd = directoryCmd;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        if (!StringUtils.hasText(directoryCmd.getSpaceName())) {
            listSpaces(admin);
        } else {
            if (!StringUtils.hasText(directoryCmd.getPartitionId())) {
                listPartitions(admin);
            } else {
                listObjects(admin, arguments);
            }
        }
    }

    /**
     * Lists the spaces.
     * @param admin GigaSpaces Admin object.
     */
    private void listSpaces(Admin admin) {
        Utils.info("total %s processing units, %s spaces.",
                   admin.getProcessingUnits().getSize(),
                   admin.getSpaces().getNames().size());

        ProcessingUnits processingUnits = admin.getProcessingUnits();
        ProcessingUnit[] processingUnitsArray = processingUnits.getProcessingUnits();
        sortProcessingUnits(processingUnitsArray);
        for (ProcessingUnit processingUnit : processingUnitsArray) {
            String puName = processingUnit.getName();
            String spaceName = "-";
            if (processingUnit.getSpaces().length == 0) {
                String puStatus = processingUnit.getStatus().toString();
                String type = "";
                if (processingUnit.getInstances()[0].isJee()) {
                    type = "Web";
                }
                Utils.info("%-35s %-30s %-20s %-10s", puName, spaceName, puStatus, type);
            } else {
                for (Space space : processingUnit.getSpaces()) {
                    spaceName = space .getName();
                    String type = "";
                    if (processingUnit.getInstances()[0].isJee()) {
                        type = "Web";
                    }
                    String puStatus = processingUnit.getStatus().toString();
                    Utils.info("%-35s %-30s %-20s %-10s", puName, spaceName, puStatus, type);
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
     * @param admin GigaSpaces Admin object.
     */
    private void listPartitions(Admin admin) {
        Space spaceByName = admin.getSpaces().getSpaceByName(directoryCmd.getSpaceName());
        Utils.info("total %s partitions", spaceByName.getPartitions().length);
        for (SpacePartition spacePartition : spaceByName.getPartitions()) {
            Utils.info("%s", spacePartition.getPartitionId());
        }
    }

    /**
     * Lists the objects.
     * @param admin GigaSpaces Admin objects.
     * @param arguments Arguments to filter which objects to list.
     */
    private void listObjects(Admin admin, String arguments) {
        String query;
        if (StringUtils.hasText(directoryCmd.getObjectType())) {
            query = String.format("SELECT * FROM %s %s",
                                  directoryCmd.getObjectType(),
                                  arguments == null ? "" : arguments);
        } else {
            query = "SELECT * FROM SYSTABLES";
        }
        SpacePartition partition = admin.getSpaces().getSpaceByName(directoryCmd.getSpaceName())
                .getPartition(
                        Integer.parseInt(directoryCmd.getPartitionId()));
        int objects = 0;
        try {
            GConnection conn = GConnection.getInstance(partition.getPrimary().getGigaSpace()
                                                               .getSpace());
            conn.setUseSingleSpace(true);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
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
                objects++;
                sb.setLength(0);
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    sb.append(rs.getString(i));
                    sb.append("\t");
                }
                if (!StringUtils.hasText(directoryCmd.getObjectType())) {
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

        if (StringUtils.hasText(directoryCmd.getObjectType())) {
            Utils.info("%s objects.", objects);
        } else {
            Utils.info("%s classes.", objects);
        }
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
