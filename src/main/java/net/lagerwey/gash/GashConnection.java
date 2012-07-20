package net.lagerwey.gash;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;

import java.util.concurrent.TimeUnit;

/**
 */
public class GashConnection {

    private final String lookupGroups;

    private final String lookupLocators;

    private Admin admin;

    public GashConnection(String lookupGroups, String lookupLocators) {
        this.lookupGroups = lookupGroups;
        this.lookupLocators = lookupLocators;
    }

    public void connect() {
        AdminFactory adminFactory = new AdminFactory();
        if (hasLookupLocators()) {
            adminFactory.addLocators(lookupLocators);
            Utils.print("Connecting to locators [%s].", lookupLocators);
        } else if (hasLookupGroups()) {
            adminFactory.addGroups(lookupGroups);
            Utils.print("Connecting to groups [%s]", lookupGroups);
        }
        admin = adminFactory.createAdmin();
        admin.getLookupServices().waitFor(1, 1, TimeUnit.SECONDS);
        Utils.print(".");
        admin.getMachines().waitFor(1, 1, TimeUnit.SECONDS);
        Utils.print(".");
        admin.getGridServiceAgents().waitFor(1, 1, TimeUnit.SECONDS);
        Utils.print(".");
        admin.getGridServiceManagers().waitFor(1, 1, TimeUnit.SECONDS);
        Utils.print(".");
        admin.getGridServiceContainers().waitFor(1, 1, TimeUnit.SECONDS);
        Utils.println(".");
    }

    public void close() {
        if (hasLookupLocators()) {
            Utils.println("Disconnecting from locators [%s]", lookupLocators);
        } else if (hasLookupGroups()) {
            Utils.println("Disconnecting from groups [%s]", lookupGroups);
        }
        admin.close();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        admin = null;
    }

    public Admin getAdmin() {
        return admin;
    }

    public String getLookupGroups() {
        return lookupGroups;
    }

    public String getLookupLocators() {
        return lookupLocators;
    }

    public boolean hasLookupGroups() {
        return lookupGroups != null;
    }

    public boolean hasLookupLocators() {
        return lookupLocators != null;
    }

    public String getKey() {
        if (hasLookupLocators()) {
            return getLookupLocators();
        } else if (hasLookupGroups()) {
            return getLookupGroups();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public String getConnectionString() {
        if (hasLookupLocators()) {
            String locator = lookupLocators;
            if (admin.getLocators().length > 0) {
                locator = admin.getLocators()[0].toString();
            }
            if (locator.endsWith("/")) {
                locator = locator.substring(0, locator.length() - 1);
            }
            return locator;
        } else if (hasLookupGroups()) {
            String groups = lookupGroups;
            if (groups.endsWith("/")) {
                groups = groups.substring(0, groups.length() - 1);
            }
            return groups;
        }
        return "";
    }
}
