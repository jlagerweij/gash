package net.lagerwey.gash.command;

import com.j_spaces.jdbc.driver.GConnection;
import net.lagerwey.gash.Gash;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.core.GigaSpace;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * SQLCommand performs a SQL command in the GigaSpaces grid.
 */
public class SQLCommand extends AbstractWorkingLocationCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public SQLCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {

        if (!StringUtils.hasText(gash.getWorkingLocation().getSpaceName())) {
            Utils.println("No space selected.");
            return;
        }

        Admin admin = gash.getWorkingLocation().getCurrentConnection().getAdmin();
        GigaSpace gigaSpace;
        if (StringUtils.hasText(gash.getWorkingLocation().getPartitionId())) {
            gigaSpace = admin.getSpaces().getSpaceByName(gash.getWorkingLocation().getSpaceName())
                    .getPartition(Integer.parseInt(
                            gash.getWorkingLocation().getPartitionId())).getPrimary().getGigaSpace();
        } else {
            gigaSpace = admin.getSpaces().getSpaceByName(gash.getWorkingLocation().getSpaceName()).getGigaSpace();
        }
        if (gigaSpace == null) {
            Utils.println("Found no GigaSpaces space.");
            return;
        }

        Utils.println("Querying space with query [%s %s]", command, arguments);
        try {
            GConnection conn = GConnection.getInstance(gigaSpace.getSpace());

            Map<String, String> shortNames = new HashMap<String, String>();
            Statement shortSt = conn.createStatement();
            ResultSet shortRs = shortSt.executeQuery("SELECT * FROM SYSTABLES");
            while (shortRs.next()) {
                String name = shortRs.getString(1);
                String shortName = name.substring(name.lastIndexOf(".") + 1).replaceAll("[a-z]", "");
                shortNames.put(shortName.toUpperCase(), name);
            }
            shortRs.close();
            shortSt.close();

            for (String shortName : shortNames.keySet()) {
                if (arguments.contains(" " + shortName)) {
                    arguments = arguments.replaceAll(" " + shortName, " " + shortNames.get(shortName));
                }
            }
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(command + " " + arguments);
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                sb.append(columnName.substring(columnName.lastIndexOf(".") + 1));
                sb.append("\t");
            }
            Utils.println("%s",
                          "__________________________________________________________________________________");
            Utils.println("%s", sb.toString());
            Utils.println("%s",
                          "----------------------------------------------------------------------------------");

            while (rs.next()) {
                sb.setLength(0);
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    sb.append(rs.getString(i));
                    sb.append("\t");
                }
                Utils.println("%s", sb.toString());
            }
            rs.close();
            st.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String description() {
        return "Executes queries on a space";
    }
}
