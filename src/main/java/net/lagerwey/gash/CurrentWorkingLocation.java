package net.lagerwey.gash;

import org.openspaces.admin.Admin;
import org.springframework.util.StringUtils;

/**
 */
public class CurrentWorkingLocation {

    private String mountpoint;
    private SpaceLocation spaceLocation = new SpaceLocation(this);
    private LogLocation logLocation = new LogLocation(this);

    public void changeLocation(Admin admin, String location) {
        if (location == null) {
            return;
        }
        if (location.startsWith("/")) {
            clear();
            location = location.substring(1);
        }
        String[] directories = location.split("/");
        if (mountpoint == null && directories.length > 0) {
            if ("spaces".equals(directories[0])) {
                mountpoint = "spaces";
                location = location.substring(mountpoint.length());
                if (location.startsWith("/")) {
                    location = location.substring(1);
                }
            } else if ("logs".equals(directories[0])) {
                mountpoint = "logs";
                location = location.substring(mountpoint.length());
                if (location.startsWith("/")) {
                    location = location.substring(1);
                }
            }
        }

        if ("logs".equals(mountpoint)) {
            logLocation.changeTo(admin, location);
        }
        if ("spaces".equals(mountpoint)) {
            spaceLocation.changeTo(admin, location);
        }
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
        if (mountpoint != null) {
            sb.append("/");
            sb.append(mountpoint);
            if ("spaces".equals(mountpoint)) {
                sb.append(spaceLocation.locationAsString());
            } else if ("logs".equals(mountpoint)) {
                sb.append(logLocation.locationAsString());
            }
        }
        return sb.toString();
    }

    public String getCurrentMountpoint() {
        return mountpoint;
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
}
