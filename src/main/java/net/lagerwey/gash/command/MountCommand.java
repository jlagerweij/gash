package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.GashConnection;
import net.lagerwey.gash.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Mounts a GigaSpaces grid by creating a new connection.
 */
public class MountCommand extends AbstractCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public MountCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        String[] args = arguments.split(" ");
        if (args.length > 1) {
            if (args[0].equals("group")) {
                String lookupGroups = args[1];
                if (!gash.getConnectionManager().isConnected(lookupGroups)) {
                    GashConnection connection = gash.getConnectionManager().open(lookupGroups, null);

                    gash.getWorkingLocation().changeLocation(String.format("/%s/spaces", connection.getKey()));

                    Utils.println("Connected to [%s].", connection.getConnectionString());
                } else {
                    Utils.println("Already connected to [%s]", lookupGroups);
                }
                return;
            }
        }

        arguments = resolveKnownLocator(arguments);

        if (!gash.getConnectionManager().isConnected(arguments)) {
            GashConnection connection = gash.getConnectionManager().open(null, arguments);

            gash.getWorkingLocation().changeConnection(connection);
            gash.getWorkingLocation().changeLocation("spaces");

            Utils.println("Connected to [%s].", connection.getConnectionString());
        } else {
            Utils.println("Already connected to [%s]", arguments);
        }
    }

    public static String resolveKnownLocator(String arguments) {
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
        return arguments;
    }

    @Override
    public String description() {
        return "Mounts a GigaSpaces grid using lookupGroups or lookupLocators";
    }

    @Override
    public void showHelp() {
        Utils.println("Usage: mount [group lookupGroups] | [lookupLocators]");
    }
}
