package net.lagerwey.gash.command;

import net.lagerwey.gash.GashState;
import org.openspaces.admin.Admin;

/**
 * Interface to a Command to perform.
 */
public interface Command {

    /**
     * Performs the command.
     *
     * @param command   Command to execute.
     * @param arguments Arguments to the command.
     */
    public void perform(String command, String arguments);

    /**
     * The help description of this command.
     *
     * @return The help description of this command.
     */
    public String description();

    /**
     * Returns an array containing the required states for this command.
     *
     * @return Array containing the required states
     */
    GashState[] requiredStates();

    /**
     * Shows the help contents for this command.
     */
    void showHelp();
}
