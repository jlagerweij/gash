package net.lagerwey.gash.command;

import com.gigaspaces.log.LogEntry;
import net.lagerwey.gash.Gash;
import net.lagerwey.gash.WorkingLocation;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.machine.Machine;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.gigaspaces.log.LogEntryMatchers.lastN;

/**
 */
public class TailCommand extends AbstractWorkingLocationCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public TailCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        String hostname = gash.getWorkingLocation().getLogLocation().getHostname();
        Admin admin = gash.getWorkingLocation().getCurrentConnection().getAdmin();
        if (hostname == null) {
//            Utils.error("Not in /logs/<hostname> location. Tail can only be performed from that location.");
            if (arguments != null) {
                List<LogEntry> logEntries = new ArrayList<LogEntry>();
                if (arguments.toLowerCase().startsWith("gsa")) {
                    for (GridServiceAgent gridServiceAgent : admin.getGridServiceAgents()) {
                        logEntries.addAll(gridServiceAgent.logEntries(lastN(10)).getEntries());
                    }
                } else if (arguments.toLowerCase().startsWith("gsm")) {
                    for (GridServiceManager gridServiceManager : admin.getGridServiceManagers()) {
                        logEntries.addAll(gridServiceManager.logEntries(lastN(10)).getEntries());
                    }
                } else if (arguments.toLowerCase().startsWith("gsc")) {
                    for (GridServiceContainer gridServiceContainer : admin.getGridServiceContainers()) {
                        logEntries.addAll(gridServiceContainer.logEntries(lastN(10)).getEntries());
                    }
                }
                Collections.sort(logEntries, new Comparator<LogEntry>() {
                    @Override
                    public int compare(LogEntry o1, LogEntry o2) {
                        return (int) (o1.getTimestamp() - o2.getTimestamp());
                    }
                });
                for (LogEntry logEntry : logEntries) {
                    Utils.println(logEntry.getText());
                }
            }
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
                Utils.println(logEntry.getText());
            }
        } else {
            Utils.error("Could not find logfile %s", arguments);
        }
    }

    @Override
    public String description() {
        return "Output the last part of files";
    }

    @Override
    public void showHelp() {
        Utils.println("Usage: tail [GSA] [GSM] [GSC]");
    }
}
