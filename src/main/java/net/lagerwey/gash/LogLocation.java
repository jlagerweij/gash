package net.lagerwey.gash;

import org.openspaces.admin.Admin;
import org.openspaces.admin.machine.Machine;
import org.springframework.util.StringUtils;

/**
 */
public class LogLocation {

    private CurrentWorkingLocation currentWorkingLocation;
    private String hostname;

    public LogLocation(CurrentWorkingLocation currentWorkingLocation) {
        this.currentWorkingLocation = currentWorkingLocation;
    }

    public void changeTo(Admin admin, String location) {
        if (location.equals("..")) {
            hostname = null;
        } else if (location.equals("/")) {
            currentWorkingLocation.clear();
        } else if (currentWorkingLocation.getCurrentMountpoint() == null) {
            currentWorkingLocation.changeLocation(admin, location);
        } else if (StringUtils.hasText(location)) {
            Machine machine = admin.getMachines().getMachineByHostName(location);
            if (machine != null) {
                hostname = location;
            } else {
                Utils.error("Could not find host with name %s", location);
            }
        }
    }

    public void clear() {
        hostname = null;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String locationAsString() {
        if (hostname == null) {
            return "";
        }
        return "/" + hostname;
    }
}
