package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.Utils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Prints out the help for all available commands.
 */
public class HelpCommand extends AbstractCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public HelpCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        if (StringUtils.hasText(arguments)) {
            String[] keys = arguments.split(" ");
            Command commandClass = gash.getCommands().get(keys[0]);
            if (commandClass != null) {
                if (gash.getStateManager().containsAll(commandClass.requiredStates())) {
                    commandClass.showHelp();
                    return;
                }
            }
        }

        List<String> strings = new ArrayList<String>(gash.getCommands().keySet());
        Collections.sort(strings);
        final String longest = Collections.max(strings, new StringLengthComparator());
        final String format = "  %-" + longest.length() + "s\t%s";

        for (String key : strings) {
            Command commandClass = gash.getCommands().get(key);
            if (gash.getStateManager().containsAll(commandClass.requiredStates())) {
                Utils.println(format, key, commandClass.description());
            }
        }
    }

    @Override
    public String description() {
        return "Shows this help info";
    }

    @Override
    public void showHelp() {
        Utils.println("Usage: help [command]");
    }

    private static class StringLengthComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return new Integer(o1.length()).compareTo(o2.length());
        }
    }
}
