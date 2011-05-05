package net.lagerwey.gash.command;

import org.openspaces.admin.Admin;

/**
 * Interface to a Command to perform.
 */
public interface Command {

    /**
     * Performs the command.
     *
     * @param admin GigaSpaces Admin object.
     * @param command Command to execute.
     * @param arguments Arguments to the command.
     */
    public void perform(Admin admin, String command, String arguments);

    /**
     * The help description of this command.
     * @return The help description of this command.
     */
    public String description();

    /**
     * Boolean indicating the need for a connection.
     * @return True if a connection is required, false otherwise.
     */
    boolean connectionRequired();
}
