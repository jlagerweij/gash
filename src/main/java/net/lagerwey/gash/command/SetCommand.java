package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.springframework.util.StringUtils;

/**
 * Sets parameters, such as debugging on or off.
 */
public class SetCommand extends AbstractConnectedCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public SetCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        if (command.equals("set")) {
            if (arguments == null) {
                Utils.println("'%s' is set to '%s'", "debug", Utils.debugEnabled);
            } else {
                if (arguments.startsWith("debug ")) {
                    String debugArg = arguments.substring("debug ".length());
                    if (StringUtils.hasText(debugArg)) {
                        Utils.debugEnabled = Boolean.valueOf(debugArg);
                    } else {
                        Utils.println("'%s' is set to '%s'", "debug", Utils.debugEnabled);
                    }
                }
            }
        }
    }

    @Override
    public String description() {
        return "Sets or shows settings";
    }
}
