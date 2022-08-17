package pt.floraon.driver.jobs;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static pt.floraon.driver.Constants.dateTimeFormat;

/**
 * Created by miguel on 10-11-2016.
 */
public class JobRunnerTask implements JobRunner {
//    private Object options;
    private boolean isClosed, hasError = false;
    private String uuid, desiredFileName, errorMessage;
    private IFloraOn driver;
    private JobTask job;
    private Date date;

    public JobRunnerTask(JobTask job, IFloraOn driver) throws IOException {
        this.isClosed = false;
        this.uuid = UUID.randomUUID().toString();
        this.job = job;
        this.driver = driver;
//        this.options = options;
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
        if(isClosed) {
            return this.hasError() ? "Finished with error: " + this.getErrorMessage()
            : "Finished. Last message: «" + job.getState() + "»";
        } else
            return job.getState();
    }

    @Override
    public String getDescription() {
        return job.getDescription();
    }

    @Override
    public User getOwner() {
        return job.getOwner();
    }

    @Override
    public boolean isFileDownload() {
        return false;
    }

    @Override
    public Job getJob() {
        return job;
    }

    @Override
    public String getDateSubmitted() {
        return dateTimeFormat.get().format(this.date);
    }

    @Override
    public Boolean hasError() {
        return this.hasError;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public void run() {
        this.date = new Date();
        try {
            this.job.run(this.driver);
            isClosed=true;
//            JobSubmitter.jobs.remove(this.uuid);
        } catch (FloraOnException | IOException e) {
            this.hasError=true;
            this.errorMessage=e.getMessage();
            isClosed=true;
            e.printStackTrace();
        }

    }
}
