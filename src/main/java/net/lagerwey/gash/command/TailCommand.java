package net.lagerwey.gash.command;

import com.gigaspaces.log.LogEntry;
import net.lagerwey.gash.CurrentWorkingLocation;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.machine.Machine;
import org.springframework.util.StringUtils;

import static com.gigaspaces.log.LogEntryMatchers.lastN;

/**
 */
public class TailCommand implements Command{

    private CurrentWorkingLocation currentWorkingLocation;

    public TailCommand(CurrentWorkingLocation currentWorkingLocation) {
        this.currentWorkingLocation = currentWorkingLocation;
    }

    @Override
    public void perform(Admin admin, String command, String arguments) {
        String hostname = currentWorkingLocation.getLogLocation().getHostname();
        if (hostname == null) {
            Utils.error("Not in /logs/<hostname> location. Tail can only be performed from that location.");
            return;
        }
        Machine machine = admin.getMachines().getMachineByHostName(hostname);
        // TODO determining which logfile is requested should be improved.
        LogProviderGridComponent logProviderGridComponent = null;
        if (arguments != null && arguments.toLowerCase().startsWith("gsa")) {
            logProviderGridComponent = machine.getGridServiceAgent();
        } else if (arguments != null && arguments.toLowerCase().startsWith("gsc")) {
            String number = arguments.substring("gsc-".length());
            for (GridServiceContainer gridServiceContainer : machine.getGridServiceContainers()) {
                if (gridServiceContainer.getAgentId() == Integer.parseInt(number)) {
                    logProviderGridComponent = gridServiceContainer;
                }
            }
        } else if (arguments != null && arguments.toLowerCase().startsWith("gsm")) {
            String number = arguments.substring("gsm-".length());
            for (GridServiceManager gridServiceManager : machine.getGridServiceManagers()) {
                if (gridServiceManager.getAgentId() == Integer.parseInt(number)) {
                    logProviderGridComponent = gridServiceManager;
                }
            }
        } else if (!StringUtils.hasText(arguments)) {
            Utils.error("Tail requires logfile as argument.");
        }
        if (logProviderGridComponent != null) {
            for (LogEntry logEntry : logProviderGridComponent.logEntries(lastN(10))) {
                System.out.println(logEntry.getText());
            }
        } else {
            Utils.error("Could not find logfile %s", arguments);
        }
    }

    @Override
    public String description() {
        return "Output the last part of files.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }
}
