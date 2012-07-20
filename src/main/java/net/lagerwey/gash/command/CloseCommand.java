package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;

/**
 * Closes the connection.
 */
public class CloseCommand extends AbstractConnectedCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public CloseCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        gash.getConnectionManager().close(gash.getWorkingLocation().getCurrentConnection());
        gash.getWorkingLocation().changeConnection(null);
    }

    @Override
    public String description() {
        return "Closes the current connection";
    }
}
