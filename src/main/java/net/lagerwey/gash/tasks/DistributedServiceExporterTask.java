package net.lagerwey.gash.tasks;

import com.gigaspaces.async.AsyncResult;
import net.lagerwey.gash.Utils;
import org.openspaces.core.executor.DistributedTask;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 */
public class DistributedServiceExporterTask implements DistributedTask<Integer, String>, ApplicationContextAware {

    private transient ApplicationContext applicationContext;

    public DistributedServiceExporterTask() {
    }

    public Integer execute() throws Exception {
        Utils.println("XXX");
        if (applicationContext != null) {
            Utils.println("Found " + applicationContext);
        }
        return 1;
    }

    public String reduce(List<AsyncResult<Integer>> results) throws Exception {
        String sum = "0" + applicationContext;
        for (AsyncResult<Integer> result : results) {
            //noinspection ThrowableResultOfMethodCallIgnored
            if (result.getException() != null) {
                throw result.getException();
            }
            sum += result.getResult();
        }
        return sum;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
