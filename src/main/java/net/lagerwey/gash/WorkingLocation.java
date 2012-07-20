package net.lagerwey.gash;

import org.springframework.util.StringUtils;

/**
 */
public class WorkingLocation {

    private String mountpoint;
    private SpaceLocation spaceLocation = new SpaceLocation(this);
    private LogLocation logLocation = new LogLocation(this);
    private GashConnection currentConnection;
    private Gash gash;

    public WorkingLocation(Gash gash) {
        this.gash = gash;
    }

    public GashConnection getCurrentConnection() {
        return currentConnection;
    }

    public boolean hasConnection() {
        return currentConnection != null;
    }

    public void changeConnection(GashConnection connection) {
        this.currentConnection = connection;
        if (connection == null) {
            if (!gash.getConnectionManager().hasConnections()) {
                gash.getStateManager().remove(GashState.CONNECTED);
            }
            gash.getStateManager().remove(GashState.WORKING_LOCATION);
        } else {
            gash.getStateManager().add(GashState.CONNECTED);
        }
    }

    public void changeLocation(String location) {
        if (location == null) {
            return;
        }
        if (location.startsWith("/")) {
            clear();
            changeConnection(null);
            stripLocationItem(location, "");
        }
        String[] directories = location.split("/");
        if (directories.length > 0) {
            String directory = directories[0];
            if (currentConnection == null) {
                String key = directory;
                GashConnection connection = gash.getConnectionManager().getConnection(key);
                if (connection != null) {
                    changeConnection(connection);

                    location = stripLocationItem(location, key);

                    if (directories.length > 1) {
                        directory = directories[1];
                    }
                } else {
                    return;
                }
            }

            if (mountpoint == null) {
                if ("spaces".equals(directory)) {
                    mountpoint = "spaces";
                    location = stripLocationItem(location, mountpoint);
                } else if ("logs".equals(directory)) {
                    mountpoint = "logs";
                    location = stripLocationItem(location, mountpoint);
                } else if ("..".equals(directories[0])) {
                    changeConnection(null);
                }
            }
        }

        if (currentConnection != null) {
            gash.getStateManager().add(GashState.WORKING_LOCATION);
            if ("logs".equals(mountpoint)) {
                logLocation.changeTo(currentConnection.getAdmin(), location);
            }
            if ("spaces".equals(mountpoint)) {
                spaceLocation.changeTo(currentConnection.getAdmin(), location);
            }
        }
    }

    private static String stripLocationItem(String location, String item) {
        location = location.substring(item.length());
        if (location.startsWith("/")) {
            location = location.substring(1);
        }
        return location;
    }

    public String getSpaceName() {
        return spaceLocation.getSpaceName();
    }

    public String getPartitionId() {
        return spaceLocation.getPartitionId();
    }

    public String getObjectType() {
        return spaceLocation.getObjectType();
    }

    /**
     * Clear the spaceName, partitionId and ObjectType.
     */
    public void clear() {
        logLocation.clear();
        spaceLocation.clear();
        mountpoint = null;
    }

    public String locationAsString() {
        StringBuilder sb = new StringBuilder();
        if (currentConnection != null) {
            sb.append(currentConnection.getConnectionString()).append(":");
            if (mountpoint != null) {
                sb.append("/");
                sb.append(mountpoint);
                if ("spaces".equals(mountpoint)) {
                    sb.append(spaceLocation.locationAsString());
                } else if ("logs".equals(mountpoint)) {
                    sb.append(logLocation.locationAsString());
                }
            }
        }
        return sb.toString();
    }

    public String getCurrentMountpoint() {
        return mountpoint;
    }

    public boolean hasMountpoint() {
        return mountpoint != null;
    }

    public boolean isInGrid() {
        return !StringUtils.hasText(this.getSpaceName());
    }

    public boolean isInSpace() {
        return StringUtils.hasText(this.getSpaceName()) && !StringUtils.hasText(this.getPartitionId());
    }

    public boolean isInPartition() {
        return StringUtils.hasText(this.getSpaceName()) && StringUtils.hasText(this.getPartitionId()) && !StringUtils
                .hasText(this.getObjectType());
    }

    public boolean isInObject() {
        return StringUtils.hasText(this.getSpaceName()) && StringUtils.hasText(this.getPartitionId()) && StringUtils
                .hasText(this.getObjectType());
    }

    public LogLocation getLogLocation() {
        return logLocation;
    }

    public boolean isInLogs() {
        return !StringUtils.hasText(logLocation.getHostname());
    }

    public boolean isInHostname() {
        return StringUtils.hasText(logLocation.getHostname());
    }

    public String determinePrompt() {
        return String.format("%s$ ", locationAsString());
    }
}
