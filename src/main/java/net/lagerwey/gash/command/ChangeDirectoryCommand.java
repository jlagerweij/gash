package net.lagerwey.gash.command;

import net.lagerwey.gash.CurrentWorkingSpace;
import org.openspaces.admin.Admin;

/**
 * Changes directory by /<spacename>/<partitionId>/<objectType>.
 */
public class ChangeDirectoryCommand implements Command {

    private CurrentWorkingSpace currentWorkingSpace;

    public ChangeDirectoryCommand(CurrentWorkingSpace currentWorkingSpace) {
        this.currentWorkingSpace = currentWorkingSpace;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        currentWorkingSpace.changeLocation(admin, arguments);
    }

    @Override
    public String description() {
        return "Changes current working location.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }
}
