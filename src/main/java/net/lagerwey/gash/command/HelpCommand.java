package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Prints out the help for all available commands.
 */
public class HelpCommand implements Command {

    private Gash gash;

    /**
     * Constructs the HelpCommand with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public HelpCommand(Gash gash) {
        this.gash = gash;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        List<String> strings = new ArrayList<String>(gash.getCommands().keySet());
        Collections.sort(strings);
        int longest = 0;
        for (String key : strings) {
            if (key.length() > longest) {
                longest = key.length();
            }
        }
        for (String key : strings) {
            Command commandClass = gash.getCommands().get(key);
            if (commandClass.connectionRequired() == gash.isConnected()) {
                String description = commandClass.description();
                while (key.length() < longest) {
                    key = key + " ";
                }
                Utils.println("  %s\t%s", key, description);
            }
        }
    }

    @Override
    public String description() {
        return "Shows this help println";
    }

    @Override
    public boolean connectionRequired() {
        return gash.isConnected();
    }
}
