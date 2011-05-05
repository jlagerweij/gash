package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;

/**
 * Exits the application.
*/
public class ExitCommand implements Command {
    private Gash gash;

    /**
     * Constructs an ExitCommand with a Gash instance.
     * @param gash Gash instance.
     */
    public ExitCommand(Gash gash) {
        this.gash = gash;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        Utils.info("Exiting...");
        gash.setExitApplication(true);
    }

    @Override
    public String description() {
        return "Exits the application";
    }

    @Override
    public boolean connectionRequired() {
        return gash.isConnected();
    }
}
