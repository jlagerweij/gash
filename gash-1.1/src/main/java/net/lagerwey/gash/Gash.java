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
    private CurrentWorkingSpace currentWorkingSpace;

    public static void main(String[] args) {
        System.setProperty("com.gigaspaces.exceptions.level", "SEVERE");
        System.setProperty("org.openspaces.level", "SEVERE");
        for (String arg : args) {
            if (arg.equals("--debug")) {
                Utils.debugEnabled = true;
            }
        }

        Gash gash = new Gash();
        gash.start();
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getLookupgroups() {
        return lookupgroups;
    }

    public String getLookuplocators() {
        return lookuplocators;
    }

    public HashMap<String, Command> getCommands() {
        return commands;
    }

    private void start() {
        currentWorkingSpace = new CurrentWorkingSpace();
        commands = new HashMap<String, Command>();
        commands.put("cd", new ChangeDirectoryCommand(currentWorkingSpace));
        commands.put("close", new CloseCommand(this));
        commands.put("exit", new ExitCommand(this));
        commands.put("delete", new SQLCommand(currentWorkingSpace));
        commands.put("insert", new SQLCommand(currentWorkingSpace));
        commands.put("help", new HelpCommand(this));
        commands.put("ls", new ListCommand(currentWorkingSpace));
        commands.put("mount", new MountCommand(this));
        commands.put("select", new SelectCommand(currentWorkingSpace));
        commands.put("set", new SetCommand());
        commands.put("spaces", new SpacesCommand());
        commands.put("top", new TopCommand());
        commands.put("units", new UnitCommand());
        commands.put("tree", new TreeCommand());
        commands.put("services", new ServicesCommand());

        // TODO Dashboard with:
        // TODO CPU Cores and usage
        // TODO Memory usage
        // TODO Nr of: Machines, GSA, GSM, GSC, LUS, Web (req/sec), Stateful (op/sec), Stateless, Mirrors

        // TODO Improve Topology overview with CPU and Memory usage
        // TODO Machine, GSA, GSC, GSM, LUS => CPU, Memory, PID, Zones, Thread count

        exitApplication = false;
        while (!exitApplication) {

            String locator = getConnectionString(this);

            String cmd = readCommandWithJLine(locator, commands);

            String arguments = null;
            if (cmd.indexOf(" ") > 0) {
                arguments = cmd.substring(cmd.indexOf(" ") + 1);
                cmd = cmd.substring(0, cmd.indexOf(" "));
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

        if (isConnected()) {
            admin.close();
        }
        System.exit(0);
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
        String s = currentWorkingSpace.locationAsString();
        return String.format("%s:%s$ ", locator, s);
    }

    public void disconnect() {
        if (isConnected()) {
            currentWorkingSpace.clear();
            Utils.info("Disconnecting from groups [%s] and locators [%s].", lookupgroups, lookuplocators);
            admin.close();
            admin = null;
        }
    }

    public String getConnectionString(Gash gash) {
        if (gash.isConnected()) {
            return admin.getLocators()[0].toString();
        }
        return "*not connected*";
    }

    public boolean isConnected() {
        return admin != null;
    }


    public void setLookuplocators(String arguments) {
        lookuplocators = arguments;
    }


    public void setExitApplication(boolean b) {
        exitApplication = b;
    }
}
