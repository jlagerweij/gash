package net.lagerwey.gash.command;

import com.j_spaces.core.IJSpace;
import com.j_spaces.jdbc.driver.GConnection;
import net.lagerwey.gash.CurrentWorkingLocation;
import net.lagerwey.gash.PrettyPrintUtils;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.core.GigaSpace;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * SelectCommand performs a query in the GigaSpaces grid.
 */
public class SelectCommand implements Command {

    private CurrentWorkingLocation currentWorkingLocation;

    /**
     * Constructs a SelectCommand with a ChangeDirectoryCommand for the current working directory.
     *
     * @param currentWorkingLocation Current working directory location.
     */
    public SelectCommand(CurrentWorkingLocation currentWorkingLocation) {
        this.currentWorkingLocation = currentWorkingLocation;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        GigaSpace gigaSpace;
        if (StringUtils.hasText(currentWorkingLocation.getSpaceName())) {
            if (StringUtils.hasText(currentWorkingLocation.getPartitionId())) {
                gigaSpace = admin.getSpaces().getSpaceByName(currentWorkingLocation.getSpaceName()).getPartition(Integer.parseInt(
                        currentWorkingLocation.getPartitionId())).getPrimary().getGigaSpace();
            } else {
                gigaSpace = admin.getSpaces().getSpaceByName(currentWorkingLocation.getSpaceName()).getGigaSpace();
            }
            if (gigaSpace != null) {
                Utils.println("Querying space with query [%s %s]", command, arguments);

                try {
                    IJSpace space = gigaSpace.getSpace();
                    GConnection conn = GConnection.getInstance(space);

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
                    PrettyPrintUtils.prettyPrintResultSet(currentWorkingLocation, space, conn, rs);
                    rs.close();
                    st.close();
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Utils.println("Found no GigaSpaces space.");
            }
        } else {
            Utils.println("No space selected.");
        }
    }

    @Override
    public String description() {
        return "Selects from a space.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }

}
