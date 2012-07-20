package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.GashState;
import org.openspaces.admin.Admin;

/**
 */
public abstract class AbstractCommand implements Command {

    private static final GashState[] REQUIRED_STATES = new GashState[0];

    protected final Gash gash;

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public AbstractCommand(final Gash gash) {
        this.gash = gash;
    }

    @Override
    public String description() {
        return "Executes the " + this.getClass().getSimpleName();
    }

    @Override
    public void showHelp() {
    }

    @Override
    public GashState[] requiredStates() {
        return REQUIRED_STATES;
    }
}
