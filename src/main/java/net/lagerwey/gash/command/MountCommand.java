package net.lagerwey.gash.command;

import net.lagerwey.gash.CurrentWorkingLocation;
import net.lagerwey.gash.Gash;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Mounts a GigaSpaces grid by creating a new connection.
 */
public class MountCommand implements Command {

    private Gash gash;
    private CurrentWorkingLocation currentWorkingLocation;

    /**
     * Constructs a MountCommand with a Gash instance.
     *
     * @param gash                   Gash instance.
     * @param currentWorkingLocation The current working location.
     */
    public MountCommand(Gash gash, CurrentWorkingLocation currentWorkingLocation) {
        this.gash = gash;
        this.currentWorkingLocation = currentWorkingLocation;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {

        String lookupgroups = null;
        String[] args = arguments.split(" ");
        if (args.length > 1) {
            if (args[0].equals("group")) {
                lookupgroups = args[1];
            }
            gash.disconnect();
            gash.setLookupgroups(lookupgroups);
        } else {
            Properties props = new Properties();
            try {
                File gashConfigDir = new File(System.getProperty("user.home"), ".gash");
                if (!gashConfigDir.mkdirs() && !gashConfigDir.exists()) {
                    Utils.warn("Could not create gash config directory at %s", gashConfigDir.getAbsolutePath());
                } else {
                    File gashConfigFile = new File(gashConfigDir, "hosts.properties");
                    if (gashConfigFile.exists()) {
                        props.load(new FileInputStream(gashConfigFile));
                    }
                    String previousValue = props.getProperty(arguments);
                    if (previousValue != null) {
                        arguments = previousValue;
                    } else {
                        String[] hostAndPort = arguments.split(":");
                        props.setProperty(hostAndPort[0], arguments);
                        props.store(new FileOutputStream(gashConfigFile), "Hosts for Gash.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            gash.disconnect();
            gash.setLookuplocators(arguments);
        }
        connect(gash.getLookupgroups(), gash.getLookuplocators());
    }

    /**
     * Connects to a GigaSpaces grid.
     *
     * @param lookupgroups   Lookupgroups to use in the new connection.
     * @param lookuplocators Lookuplocators to use in the new connection.
     */
    private void connect(String lookupgroups, String lookuplocators) {
        AdminFactory adminFactory = new AdminFactory();
        if (lookupgroups != null) {
            adminFactory.addGroups(lookupgroups);
        }
        if (lookuplocators != null) {
            adminFactory.addLocators(lookuplocators);
        }
        Admin admin = adminFactory.createAdmin();
        Utils.print(format("Connecting to groups[%s] and lookuplocators [%s].", lookupgroups, lookuplocators));
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
        gash.setAdmin(admin);

        currentWorkingLocation.changeLocation(admin, "spaces");

        String locator = gash.getConnectionString(gash);
        Utils.println("Connected to [%s].", locator);
    }


    @Override
    public String description() {
        return "Mounts a GigaSpaces grid using lookupgroups and lookuplocators";
    }

    @Override
    public boolean connectionRequired() {
        return false;
    }
}
