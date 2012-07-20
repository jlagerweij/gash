package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import org.openspaces.admin.Admin;

/**
 * Changes directory by /<spacename>/<partitionId>/<objectType>.
 */
public class ChangeDirectoryCommand extends AbstractConnectedCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public ChangeDirectoryCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        gash.getWorkingLocation().changeLocation(arguments);
    }

    @Override
    public String description() {
        return "Changes current working location";
    }
}
