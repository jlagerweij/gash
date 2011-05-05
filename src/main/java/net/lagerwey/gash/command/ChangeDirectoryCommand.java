package net.lagerwey.gash.command;

import com.j_spaces.jdbc.driver.GConnection;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.space.SpacePartition;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Changes directory by /<spacename>/<partitionId>/<objectType>.
 */
public class ChangeDirectoryCommand implements Command {

    private String spaceName;
    private String partitionId;
    private String objectType;

    @Override
    public void perform(Admin admin, String command, String arguments) {
        String[] directories = arguments.split("/");
        if (arguments.startsWith("/")) {
            if (directories.length == 0) {
                directories = new String[]{"/"};
            } else {
                directories[0] = "/";
            }
        }
        for (String directory : directories) {
            changeDirectory(admin, directory);
        }
    }

    /**
     * Changes to a 'directory'.
     *
     * @param admin     GigaSpaces Admin object.
     * @param arguments Arguments to change a directory.
     */
    public void changeDirectory(Admin admin, String arguments) {
        if (arguments.equals("..")) {
            if (StringUtils.hasText(objectType)) {
                objectType = null;
            } else if (StringUtils.hasText(partitionId)) {
                partitionId = null;
            } else if (StringUtils.hasText(spaceName)) {
                spaceName = null;
            }
        } else if (arguments.equals("/")) {
            spaceName = null;
            partitionId = null;
            objectType = null;
        } else {
            if (StringUtils.hasText(spaceName)) {
                if (StringUtils.hasText(partitionId)) {
                    if (StringUtils.hasText(objectType)) {
                        Utils.info("Nowhere to change to.");
                    } else {
                        Map<String, String> shortObjectNames = createShortObjectTypeMap(admin);

                        if (shortObjectNames.containsValue(arguments)) {
                            objectType = arguments;
                        } else if (shortObjectNames.containsKey(arguments.toUpperCase())) {
                            objectType = shortObjectNames.get(arguments.toUpperCase());
                        } else {
                            Utils.info("Object type not found [%s]", arguments);
                        }
                    }
                } else {
                    if (arguments.matches("[0-9]*")) {
                        SpacePartition partition = admin.getSpaces().getSpaceByName(spaceName)
                                .getPartition(Integer.parseInt(
                                        arguments));
                        partitionId = "" + partition.getPartitionId();
                    } else {
                        Utils.info("Could not parse partition id '%s'", arguments);
                    }
                }
            } else {
                if (admin.getSpaces().getSpaceByName(arguments) != null) {
                    spaceName = arguments;
                } else {
                    Map<String, String> shortNames = createShortSpaceNameMap(admin);
                    if (shortNames.containsKey(arguments)) {
                        String name = shortNames.get(arguments);
                        if (admin.getSpaces().getSpaceByName(name) != null) {
                            spaceName = name;
                        }
                    }
                    if (spaceName == null) {
                        Utils.info("Space not found '%s'", arguments);
                    }
                }
            }
        }
    }

    /**
     * Creates a Map of short space names and its long one.
     *
     * @param admin GigaSpaces Admin object.
     * @return Map for short space names to the long space name.
     */
    private Map<String, String> createShortSpaceNameMap(Admin admin) {
        Map<String, String> shortNames = new HashMap<String, String>();
        for (String name : admin.getSpaces().getNames().keySet()) {
            String shortName = name.charAt(0) + name.substring(1).replaceAll("[a-z]", "");
            shortNames.put(shortName.toUpperCase(), name);
        }
        return shortNames;
    }

    /**
     * Creates a Map of short objecttypes and the long one.
     *
     * @param admin GigaSpaces Admin object.
     * @return Map for short objecttypes to the long objecttypes.
     */
    private Map<String, String> createShortObjectTypeMap(Admin admin) {
        Map<String, String> shortObjectNames = new HashMap<String, String>();
        try {
            SpacePartition partition = admin.getSpaces().getSpaceByName(spaceName)
                    .getPartition(Integer.parseInt(
                            partitionId));
            GConnection conn = GConnection.getInstance(partition.getPrimary().getGigaSpace()
                                                               .getSpace());
            Statement shortSt = conn.createStatement();
            ResultSet shortRs = shortSt.executeQuery("SELECT * FROM SYSTABLES");
            while (shortRs.next()) {
                String name = shortRs.getString(1);
                String shortestName = name.substring(name.lastIndexOf(".") + 1).replaceAll("[a-z]", "");
                String shortName = name.substring(name.lastIndexOf(".") + 1);
                shortObjectNames.put(shortestName.toUpperCase(), name);
                shortObjectNames.put(shortName.toUpperCase(), name);
            }
            shortRs.close();
            shortSt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shortObjectNames;
    }

    @Override
    public String description() {
        return "Changes current working location.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public String getObjectType() {
        return objectType;
    }

    /**
     * Clear the spaceName, partitionId and ObjectType.
     */
    public void clear() {
        spaceName = null;
        partitionId = null;
        objectType = null;
    }
}
