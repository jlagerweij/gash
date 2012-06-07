package net.lagerwey.gash.tasks;

import net.lagerwey.gash.Utils;
import org.openspaces.core.executor.Task;

/**
 */
public class ServiceExporterTask implements Task<String> {

    @Override
    public String execute() throws Exception {
        Utils.println("TASK");
        return "TASK";
    }
}
