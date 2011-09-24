package net.lagerwey.gash;

import jline.ConsoleReader;
import jline.SimpleCompletor;
import net.lagerwey.gash.command.ChangeDirectoryCommand;
import net.lagerwey.gash.command.CloseCommand;
import net.lagerwey.gash.command.Command;
import net.lagerwey.gash.command.ExitCommand;
import net.lagerwey.gash.command.HelpCommand;
import net.lagerwey.gash.command.ListCommand;
import net.lagerwey.gash.command.MountCommand;
import net.lagerwey.gash.command.SQLCommand;
import net.lagerwey.gash.command.SelectCommand;
import net.lagerwey.gash.command.ServicesCommand;
import net.lagerwey.gash.command.SetCommand;
import net.lagerwey.gash.command.SpacesCommand;
import net.lagerwey.gash.command.TailCommand;
import net.lagerwey.gash.command.TopCommand;
import net.lagerwey.gash.command.TreeCommand;
import net.lagerwey.gash.command.UnitCommand;
import org.openspaces.admin.Admin;

import java.io.IOException;
import java.util.HashMap;

/**
 */
public class Gash {

    private Admin admin;
    private String lookupgroups = null;
    private String lookuplocators = lookupgroups;
    static boolean exitApplication;
    private HashMap<String, Command> commands;
    private CurrentWorkingLocation currentWorkingLocation;

    public static void main(String[] args) {
        System.setProperty("com.gigaspaces.exceptions.level", "SEVERE");
        System.setProperty("org.openspaces.level", "SEVERE");
        String argLine = null;
        for (String arg : args) {
            if (arg.equals("--debug")) {
                Utils.debugEnabled = true;
            } else {
                if (argLine == null) {
                    argLine = "";
                }
                argLine += arg + " ";
            }
        }

        String[] script = null;
        if (argLine != null) {
            script = argLine.split(";");
        }

        Gash gash = new Gash();
        gash.start(script);
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public HashMap<String, Command> getCommands() {
        return commands;
    }

    private void start(String[] script) {
        currentWorkingLocation = new CurrentWorkingLocation();
        commands = new HashMap<String, Command>();
        commands.put("cd", new ChangeDirectoryCommand(currentWorkingLocation));
        commands.put("close", new CloseCommand(this));
        commands.put("exit", new ExitCommand(this));
        commands.put("delete", new SQLCommand(currentWorkingLocation));
        commands.put("insert", new SQLCommand(currentWorkingLocation));
        commands.put("help", new HelpCommand(this));
        commands.put("ls", new ListCommand(currentWorkingLocation));
        commands.put("mount", new MountCommand(this, currentWorkingLocation));
        commands.put("select", new SelectCommand(currentWorkingLocation));
        commands.put("set", new SetCommand());
        commands.put("spaces", new SpacesCommand());
        commands.put("tail", new TailCommand(currentWorkingLocation));
        commands.put("top", new TopCommand());
        commands.put("units", new UnitCommand());
        commands.put("tree", new TreeCommand());
        commands.put("services", new ServicesCommand());

        if (script != null) {
            for (String aScript : script) {
                executeCommand(aScript);
            }
        }

        exitApplication = false;
        while (!exitApplication) {

            String locator = getConnectionString(this);

            String cmd = readCommandWithJLine(locator, commands);

            executeCommand(cmd);
        }

        if (isConnected()) {
            admin.close();
        }
        System.exit(0);
    }

    private void executeCommand(String cmd) {
        String arguments = null;
        if (cmd.indexOf(" ") > 0) {
            arguments = cmd.substring(cmd.indexOf(" ") + 1);
            cmd = cmd.substring(0, cmd.indexOf(" ")).toLowerCase();
        } else {
            cmd = cmd.toLowerCase();
        }
        if (commands.containsKey(cmd)) {
            Command commandToPerform = commands.get(cmd);
            if (!commandToPerform.connectionRequired() || isConnected()) {
                try {
                    commandToPerform.perform(admin, cmd, arguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Utils.error("Could not perform command. Not connected. Use 'mount' to mount GigaSpaces.");
            }
        } else {
            Utils.error("Unknown command '%s'", cmd);
            commands.get("help").perform(admin, cmd, arguments);
        }
    }

    private String readCommandWithJLine(String locator, HashMap<String, Command> commands) {
//        List<Completor> completors = new LinkedList<Completor>();

        try {
            ConsoleReader reader = new ConsoleReader();
//            reader.addCompletor (new ArgumentCompletor(completors));

            for (String s : commands.keySet()) {
                reader.addCompletor(new SimpleCompletor(s));
            }

            String prompt = determinePrompt(locator);

            return reader.readLine(prompt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String determinePrompt(Object locator) {
        String s = currentWorkingLocation.locationAsString();
        return String.format("%s:%s$ ", locator, s);
    }

    public void disconnect() {
        if (isConnected()) {
            currentWorkingLocation.clear();
            Utils.info("Disconnecting from groups [%s] and locators [%s].", lookupgroups, lookuplocators);
            admin.close();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            admin = null;
        }
    }

    public String getConnectionString(Gash gash) {
        if (gash.isConnected()) {
            if (admin.getLocators().length > 0) {
                String locator = admin.getLocators()[0].toString();
                if (locator.endsWith("/")) {
                    locator = locator.substring(0, locator.length() - 1);
                }
                return locator;
            } else if (admin.getGroups().length > 0) {
                String lookupgroup = admin.getGroups()[0];
                if (lookupgroup.endsWith("/")) {
                    lookupgroup = lookupgroup.substring(0, lookupgroup.length() - 1);
                }
                return lookupgroup;
            }
        }
        return "*not connected*";
    }

    public boolean isConnected() {
        return admin != null;
    }


    public String getLookupgroups() {
        return lookupgroups;
    }

    public void setLookupgroups(String lookupgroups) {
        this.lookupgroups = lookupgroups;
    }

    public String getLookuplocators() {
        return lookuplocators;
    }

    public void setLookuplocators(String arguments) {
        lookuplocators = arguments;
    }


    public void setExitApplication(boolean b) {
        exitApplication = b;
    }
}
