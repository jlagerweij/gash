package net.lagerwey.gash;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import net.lagerwey.gash.command.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class Gash {

    private static boolean exitApplication;

    private final Map<String, Command> commands;
    private final WorkingLocation currentWorkingLocation;
    private final GashStateManager stateManager;
    private final GashConnectionManager connectionManager;

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

    public Map<String, Command> getCommands() {
        return commands;
    }

    public WorkingLocation getWorkingLocation() {
        return currentWorkingLocation;
    }

    public GashStateManager getStateManager() {
        return stateManager;
    }

    public GashConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private Gash() {
        connectionManager = new GashConnectionManager();
        stateManager = new GashStateManager();
        currentWorkingLocation = new WorkingLocation(this);
        commands = new HashMap<String, Command>();
    }

    private void start(String[] script) {
        commands.put("cd", new ChangeDirectoryCommand(this));
        commands.put("close", new CloseCommand(this));
        commands.put("exit", new ExitCommand(this));
        commands.put("delete", new SQLCommand(this));
        commands.put("insert", new SQLCommand(this));
        commands.put("help", new HelpCommand(this));
        commands.put("ls", new ListCommand(this));
        commands.put("mount", new MountCommand(this));
        commands.put("select", new SelectCommand(this));
        commands.put("set", new SetCommand(this));
        commands.put("spaces", new SpacesCommand(this));
        commands.put("tail", new TailCommand(this));
        commands.put("top", new TopCommand(this));
        commands.put("units", new UnitCommand(this));
        commands.put("tree", new TreeCommand(this));
        commands.put("services", new ServicesCommand(this));

        if (script != null) {
            for (String aScript : script) {
                executeCommand(aScript);
            }
        }

        try {
            ConsoleReader reader = new ConsoleReader();
            reader.addCompleter(new StringsCompleter(commands.keySet()));

            exitApplication = false;
            while (!exitApplication) {

                String prompt = currentWorkingLocation.determinePrompt();
                reader.setPrompt(prompt);

                String cmd = reader.readLine();

                executeCommand(cmd);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                TerminalFactory.get().restore();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            if (stateManager.containsAll(commandToPerform.requiredStates())) {
                try {
                    commandToPerform.perform(cmd, arguments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (!stateManager.contains(GashState.CONNECTED)) {
                    Utils.error("Could not perform command. Not connected. Use 'mount' to mount GigaSpaces.");
                } else if (!stateManager.contains(GashState.WORKING_LOCATION)) {
                    Utils.error("Could not perform command. No working location. Use 'cd' to change the working location.");
                }
            }
        } else {
            Utils.error("Unknown command '%s'", cmd);
            commands.get("help").perform(cmd, arguments);
        }
    }

    public void setExitApplication(boolean b) {
        exitApplication = b;
    }
}
