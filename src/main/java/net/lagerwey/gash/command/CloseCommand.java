package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import org.openspaces.admin.Admin;

/**
 * Closes the connection.
*/
public class CloseCommand implements Command {

    private Gash gash;

    /**
     * Constructs the CloseCommand with a Gash instance.
     * @param gash Gash instance.
     */
    public CloseCommand(Gash gash) {
        this.gash = gash;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        gash.setLookuplocators(arguments);
        gash.disconnect();
    }

    @Override
    public String description() {
        return "Closes the connection to the grid.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }
}
