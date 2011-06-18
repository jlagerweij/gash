package net.lagerwey.gash;

import org.openspaces.admin.Admin;

/**
 */
public class CurrentWorkingSpace {
    private SpaceLocation spaceLocation = new SpaceLocation();

    public void changeLocation(Admin admin, String location) {
        spaceLocation.changeTo(admin, location);
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
        spaceLocation.clear();
    }

    public String locationAsString() {
        return spaceLocation.locationAsString();
    }

}
