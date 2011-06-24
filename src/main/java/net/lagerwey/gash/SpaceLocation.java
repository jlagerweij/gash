package net.lagerwey.gash;

import com.j_spaces.jdbc.driver.GConnection;
import org.openspaces.admin.Admin;
import org.openspaces.admin.space.SpacePartition;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class SpaceLocation {

    private List<SpaceLocation> locations = new ArrayList<SpaceLocation>();

    private String spaceName;
    private String partitionId;
    private String objectType;

    public void changeTo(Admin admin, String arguments) {
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
     * @param directory Arguments to change a directory.
     */
    private void changeDirectory(Admin admin, String directory) {
        SpaceLocation spaceLocation;
        if (locations.size() > 0) {
            spaceLocation = locations.get(locations.size() - 1);
        } else {
            spaceLocation = new SpaceLocation();
            locations.add(spaceLocation);
        }
        if (directory.equals("..")) {
            if (spaceLocation.hasObjectType()) {
                spaceLocation.objectType = null;
            } else if (spaceLocation.hasPartitionId()) {
                spaceLocation.partitionId = null;
            } else if (spaceLocation.hasSpaceName()) {
                spaceLocation.spaceName = null;
            }
        } else if (directory.equals("/")) {
            clear();
        } else {
            if (!spaceLocation.hasSpaceName()) {
                if (admin.getSpaces().getSpaceByName(directory) != null) {
                    spaceLocation.spaceName = directory;
                } else {
                    Map<String, String> shortNames = createShortSpaceNameMap(admin);
                    if (shortNames.containsKey(directory)) {
                        String name = shortNames.get(directory);
                        if (admin.getSpaces().getSpaceByName(name) != null) {
                            spaceLocation.spaceName = name;
                        }
                    }
                    if (spaceLocation.spaceName == null) {
                        Utils.info("Space not found '%s'", directory);
                    }
                }
            } else if (!spaceLocation.hasPartitionId()) {
                if (directory.matches("[0-9]*")) {
                    SpacePartition partition = admin.getSpaces().getSpaceByName(spaceLocation.spaceName)
                            .getPartition(Integer.parseInt(
                                    directory));
                    if (partition == null) {
                        Utils.error("Partition %s not found.", directory);
                    } else {
                        spaceLocation.partitionId = "" + partition.getPartitionId();
                    }
                } else {
                    // It's not a partition, so it must be another space.
                    spaceLocation = new SpaceLocation();
                    locations.add(spaceLocation);
                    changeDirectory(admin, directory);
                }
            } else if (!spaceLocation.hasObjectType()) {
                Map<String, String> shortObjectNames = spaceLocation.createShortObjectTypeMap(admin);

                if (shortObjectNames.containsValue(directory)) {
                    spaceLocation.objectType = directory;
                } else if (shortObjectNames.containsKey(directory.toUpperCase())) {
                    spaceLocation.objectType = shortObjectNames.get(directory.toUpperCase());
                } else {
                    Utils.info("Object type not found [%s]", directory);
                }
            } else {
                Utils.info("Nowhere to change to.");
            }
        }
    }

    private boolean hasSpaceName() {
        return StringUtils.hasText(spaceName);
    }

    private boolean hasPartitionId() {
        return StringUtils.hasText(partitionId);
    }

    private boolean hasObjectType() {
        return StringUtils.hasText(objectType);
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

    public String getSpaceName() {
        if (locations.size() > 0) {
            return locations.get(locations.size() - 1).spaceName;
        }
        return null;
    }

    public String getPartitionId() {
        if (locations.size() > 0) {
            return locations.get(locations.size() - 1).partitionId;
        }
        return null;
    }

    public String getObjectType() {
        if (locations.size() > 0) {
            return locations.get(locations.size() - 1).objectType;
        }
        return null;
    }

    public void clear() {
        locations.clear();
    }

    public String locationAsString() {
        String location = "";
        for (SpaceLocation spaceLocation : locations) {
            location += spaceLocation.locationAsString();
        }
        if (locations.size() == 0) {
            if (this.hasSpaceName()) {
                if (this.hasPartitionId()) {
                    if (this.hasObjectType()) {
                        location += String.format("/%s/%s/%s", this.spaceName, this.partitionId, this.objectType);
                    } else {
                        location += String.format("/%s/%s$ ", this.spaceName, this.partitionId);
                    }
                } else {
                    location += String.format("/%s", this.spaceName);
                }
            } else {
                location += "/";
            }
        } else {
            location += "/";
        }
        return location;
    }
}
