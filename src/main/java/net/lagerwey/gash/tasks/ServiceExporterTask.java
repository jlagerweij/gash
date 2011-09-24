package net.lagerwey.gash.tasks;

import org.openspaces.core.executor.Task;

/**
 */
public class ServiceExporterTask implements Task<String> {

    @Override
    public String execute() throws Exception {
        System.out.println("TASK");
        return "TASK";
    }
}
