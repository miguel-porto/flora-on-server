package pt.floraon.driver.jobs;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.IFloraOn;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by miguel on 10-11-2016.
 */
public class JobRunnerTask implements JobRunner {
    private Object options;
    private boolean isClosed, hasError = false;
    private String uuid, desiredFileName, errorMessage;
    private IFloraOn driver;
    private JobTask job;

    public JobRunnerTask(JobTask job, Object options, IFloraOn driver) throws IOException {
        this.isClosed = false;
        this.uuid = UUID.randomUUID().toString();
        this.job = job;
        this.driver = driver;
        this.options = options;
    }

    public String getID() {
        return this.uuid;
    }

    @Override
    public Boolean isReady() throws FloraOnException {
        return isClosed;
    }

    @Override
    public String getState() throws FloraOnException {
        return job.getState();
    }

    @Override
    public boolean isFileDownload() {
        return false;
    }

    @Override
    public void run() {
        try {
            this.job.run(this.driver, this.options);
            isClosed=true;
            JobSubmitter.jobs.remove(this.uuid);
        } catch (FloraOnException | IOException e) {
            this.hasError=true;
            this.errorMessage=e.getMessage();
            isClosed=true;
            e.printStackTrace();
        }

    }
}
