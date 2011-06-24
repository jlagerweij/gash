package net.lagerwey.gash.command;

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

    /**
     * Constructs a MountCommand with a Gash instance.
     * @param gash Gash instance.
     */
    public MountCommand(Gash gash) {
        this.gash = gash;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {

        Properties props = new Properties();
        try {
            File gashConfigDir = new File(System.getProperty("user.home"), ".gash");
            gashConfigDir.mkdirs();
            if (!gashConfigDir.exists()) {
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
        connect(gash.getLookupgroups(), gash.getLookuplocators());
    }

    /**
     * Connects to a GigaSpaces grid.
     * @param lookupgroups Lookupgroups to use in the new connection.
     * @param lookuplocators Lookuplocators to use in the new connection.
     */
    private void connect(String lookupgroups, String lookuplocators) {
        Admin admin = new AdminFactory().addGroups(lookupgroups).addLocators(lookuplocators).createAdmin();
        System.out.print(format("Connecting to groups[%s] and lookuplocators [%s].", lookupgroups, lookuplocators));
        admin.getLookupServices().waitFor(1, 15, TimeUnit.SECONDS);
        System.out.print(".");
        admin.getMachines().waitFor(1, 5, TimeUnit.SECONDS);
        System.out.print(".");
        admin.getGridServiceAgents().waitFor(1, 5, TimeUnit.SECONDS);
        System.out.print(".");
        admin.getGridServiceManagers().waitFor(1, 10, TimeUnit.SECONDS);
        System.out.print(".");
        admin.getGridServiceContainers().waitFor(1, 15, TimeUnit.SECONDS);
        System.out.println(".");
        gash.setAdmin(admin);

        String locator = gash.getConnectionString(gash);
        Utils.info("Connected to [%s].", locator);
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
