package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.GashState;
import net.lagerwey.gash.Utils;

/**
 * Exits the application.
 */
public class ExitCommand extends AbstractCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public ExitCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        if (gash.getStateManager().contains(GashState.CONNECTED)) {
            gash.getConnectionManager().closeAll();
        }
        Utils.println("Exiting...");
        gash.setExitApplication(true);
    }

    @Override
    public String description() {
        return "Exits the application";
    }
}
