package net.lagerwey.gash.command;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.cluster.activeelection.SpaceMode;
import net.lagerwey.gash.Gash;
import net.lagerwey.gash.Utils;
import net.lagerwey.gash.tasks.DistributedServiceExporterTask;
import org.openspaces.admin.Admin;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;

import java.util.concurrent.ExecutionException;

/**
 * Detects exported services in the GigaSpaces grid.
 */
public class ServicesCommand extends AbstractConnectedCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public ServicesCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        Admin admin = gash.getWorkingLocation().getCurrentConnection().getAdmin();
        try {
            for (Space space : admin.getSpaces()) {
//                if (space.getName().equals("ServicesLayerSpace")) {
                for (SpaceInstance spaceInstance : space.getInstances()) {
                    if (spaceInstance.getMode().equals(SpaceMode.PRIMARY)) {
                        // Primary space.
                        final AsyncFuture<String> execute = spaceInstance.getGigaSpace()
                                .execute(new DistributedServiceExporterTask());
                        String result = execute.get();
                        Utils.println(result);
                    }
                }
//                }
                /*
                if (space.getName().equals("ServicesLayerSpace")) {
                    final GigaSpace gigaSpace = space.getGigaSpace();
                    Utils.println("Executing on space " + gigaSpace.getName());
                    final AsyncFuture<Long> execute = gigaSpace
                            .execute(new DistributedServiceExporterTask());
                    Long result = execute.get();
                }
                */
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String description() {
        return "Show all services from all spaces";
    }
}
