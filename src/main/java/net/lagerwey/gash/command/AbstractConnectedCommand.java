package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.GashState;

/**
 */
public abstract class AbstractConnectedCommand extends AbstractCommand {

    private static final GashState[] REQUIRED_STATES = new GashState[]{GashState.CONNECTED};

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public AbstractConnectedCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public GashState[] requiredStates() {
        return REQUIRED_STATES;
    }
}
