package net.lagerwey.gash.command;

import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.vm.VirtualMachineStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Shows a dashboard of the systems health.
 */
public class TopCommand implements Command {

    @Override
    public void perform(Admin admin, String command, String arguments) {
        boolean showDetails = false;
        if ("-a".equals(arguments)) {
            showDetails = true;
        }
        // TODO Nr of: Web (req/sec), Stateful (op/sec), Stateless, Mirrors

//        boolean exitDashboard = false;
//        while (!exitDashboard) {
        Machine[] machines = admin.getMachines().getMachines();
        for (Machine machine : machines) {
            String hostName = machine.getHostName();
            int cores = machine.getOperatingSystem().getDetails().getAvailableProcessors();
            OperatingSystemStatistics operatingSystemStatistics = machine.getOperatingSystem().getStatistics();
            double cpuPerc = operatingSystemStatistics.getCpuPerc();
            double maxMemory = operatingSystemStatistics.getDetails()
                    .getTotalPhysicalMemorySizeInMB();
            double freeMemory = operatingSystemStatistics.getFreePhysicalMemorySizeInMB();
            double memoryUsed = maxMemory - freeMemory;
            double memoryUsedPerc = operatingSystemStatistics.getPhysicalMemoryUsedPerc();
            StringBuilder progressBar = createProgressBar(memoryUsedPerc);
            Utils.info(
                    "Host: %s - Cpu(s): %s, %5.2f%% usage  Mem: %4.0f MB total, %4.0f MB used, %4.0f MB free, %5.2f%%  %s",
                    hostName,
                    cores,
                    cpuPerc * 100,
                    maxMemory,
                    memoryUsed,
                    freeMemory,
                    memoryUsedPerc,
                    progressBar.toString());
        }

        Utils.info("%s Hosts  %s GSA    %s GSM    %s GSC    %s LUS    %s Zones",
                   admin.getMachines().getMachines().length,
                   admin.getMachines().getSize(),
                   admin.getGridServiceManagers().getSize(),
                   admin.getGridServiceContainers().getSize(),
                   admin.getLookupServices().getSize(),
                   admin.getZones().getZones().length);

        int web = 0;
        int statefull = 0;
        int mirror = 0;
        int stateless = 0;
        for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
            for (ProcessingUnitInstance processingUnitInstance : processingUnit.getInstances()) {
                if (processingUnitInstance.isJee()) {
                    web++;
                } else if (processingUnitInstance.isEmbeddedSpaces()) {
                    if (processingUnitInstance.getEmbeddedSpaceDetails().isMirror()) {
                        mirror++;
                    } else if (processingUnitInstance.getSpaceInstance().getBackupId() == 0){
                        statefull++;
                    }
                } else {
                    stateless++;
                }

            }
        }
        Utils.info("%s Web    %s Statefull,    %s Stateless,    %s Mirrors", web, statefull, stateless, mirror);

        List<TopEntry> topEntries = new ArrayList<TopEntry>();
        if (showDetails) {
            for (GridServiceAgent gridServiceAgent : admin.getGridServiceAgents()) {
                final VirtualMachineStatistics statistics = gridServiceAgent.getVirtualMachine().getStatistics();
                double cpuPerc = statistics.getCpuPerc();
                double memoryUsedPerc = statistics.getMemoryHeapUsedPerc();
                double memoryUsed = statistics.getMemoryHeapUsedInMB();
                double maxMemory = statistics.getDetails().getMemoryHeapMaxInMB();
                double freeMemory = maxMemory - memoryUsed;

                final String name = "GSA [" + gridServiceAgent.getVirtualMachine().getDetails().getPid() + "]";
                topEntries.add(new TopEntry(name, cpuPerc, memoryUsedPerc, maxMemory, memoryUsed, freeMemory));
            }
            for (GridServiceManager gridServiceManager : admin.getGridServiceManagers()) {
                final VirtualMachineStatistics statistics = gridServiceManager.getVirtualMachine().getStatistics();
                double cpuPerc = statistics.getCpuPerc();
                double memoryUsedPerc = statistics.getMemoryHeapUsedPerc();
                double memoryUsed = statistics.getMemoryHeapUsedInMB();
                double maxMemory = statistics.getDetails().getMemoryHeapMaxInMB();
                double freeMemory = maxMemory - memoryUsed;

                final String name = "GSM-" + gridServiceManager.getAgentId();
                topEntries.add(new TopEntry(name, cpuPerc, memoryUsedPerc, maxMemory, memoryUsed, freeMemory));
            }
        }
        for (GridServiceContainer gridServiceContainer : admin.getGridServiceContainers()) {
            final VirtualMachineStatistics statistics = gridServiceContainer.getVirtualMachine().getStatistics();
            double cpuPerc = statistics.getCpuPerc();
            double memoryUsedPerc = statistics.getMemoryHeapUsedPerc();
            double memoryUsed = statistics.getMemoryHeapUsedInMB();
            double maxMemory = statistics.getDetails().getMemoryHeapMaxInMB();
            double freeMemory = maxMemory - memoryUsed;

            final String name = "GSC-" + gridServiceContainer.getAgentId();
            topEntries.add(new TopEntry(name, cpuPerc, memoryUsedPerc, maxMemory, memoryUsed, freeMemory));
        }
        Collections.sort(topEntries, new Comparator<TopEntry>() {
            @Override
            public int compare(TopEntry o1, TopEntry o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
//                return Double.compare(o2.getCpuPerc(), o1.getCpuPerc());
            }
        });
        for (TopEntry topEntry : topEntries) {
            double memoryUsedPerc = topEntry.getMemoryUsedPerc();
            StringBuilder progressbar = createProgressBar(memoryUsedPerc);
            Utils.info("%-15s  %5.2f%% usage  Mem: %4.0f MB total, %4.0f MB used, %4.0f MB free, %5.2f %%  %s",
                       topEntry.getName(),
                       topEntry.getCpuPerc() * 100,
                       topEntry.getMaxMemory(),
                       topEntry.getMemoryUsed(),
                       topEntry.getFreeMemory(),
                       topEntry.getMemoryUsedPerc(),
                       progressbar.toString());
        }


        /*
        try {
            ConsoleReader reader = new ConsoleReader();
            System.out.print("Command>");
            int ch = reader.readVirtualKey();
            switch (ch) {
                case 'q': exitDashboard = true;
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
//        }
    }

    /**
     * Creates a progress bar in text.
     * @param percentage Current percentage of the progress.
     * @return A progress bar in text.
     */
    private StringBuilder createProgressBar(double percentage) {
        StringBuilder progressbar = new StringBuilder();
        progressbar.append("|");
        for (int i = 0; i < 10; i++) {
            if (i < percentage / 10) {
                progressbar.append(".");
            } else {
                progressbar.append(" ");
            }
        }
        progressbar.append("|");
        return progressbar;
    }

    @Override
    public String description() {
        return "Provides a dynamic real-time view of a running system.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }

    /**
     * An entry in the top list.
     */
    private class TopEntry {

        private String name;
        private double cpuPerc;
        private double memoryUsedPerc;
        private double maxMemory;
        private double memoryUsed;
        private double freeMemory;

        /**
         * Constructs a TopEntry.
         * @param name The name.
         * @param cpuPerc The CPU percentage.
         * @param memoryUsedPerc The used memory percentage.
         * @param maxMemory The maximum memory.
         * @param memoryUsed The used memory.
         * @param freeMemory The free memory.
         */
        public TopEntry(String name, double cpuPerc, double memoryUsedPerc, double maxMemory, double memoryUsed,
                        double freeMemory) {
            this.name = name;
            this.cpuPerc = cpuPerc;
            this.memoryUsedPerc = memoryUsedPerc;
            this.maxMemory = maxMemory;
            this.memoryUsed = memoryUsed;
            this.freeMemory = freeMemory;
        }

        public String getName() {
            return name;
        }

        public double getCpuPerc() {
            return cpuPerc;
        }

        public double getMemoryUsedPerc() {
            return memoryUsedPerc;
        }

        public double getMaxMemory() {
            return maxMemory;
        }

        public double getMemoryUsed() {
            return memoryUsed;
        }

        public double getFreeMemory() {
            return freeMemory;
        }
    }
}
