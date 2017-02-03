package pt.floraon.driver.jobs;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.floraon.driver.IFloraOn;

public final class JobSubmitter {
	protected static Map<String, JobRunner> jobs=new HashMap<String, JobRunner>();
	
	public static JobRunnerFileDownload newJobFileDownload(JobFileDownload job, String desiredFileName, IFloraOn driver) throws IOException {
		JobRunnerFileDownload j=new JobRunnerFileDownload(job, desiredFileName, driver);
		JobSubmitter.jobs.put(j.getID(), j);
		new Thread(j).start();
		return j;
	}

	public static JobRunnerTask newJobTask(JobTask job, Object options, IFloraOn driver) throws IOException {
		JobRunnerTask j = new JobRunnerTask(job, options, driver);
		JobSubmitter.jobs.put(j.getID(), j);
		new Thread(j).start();
		return j;
	}

	public static JobRunner getJob(String uuid) {
		return JobSubmitter.jobs.get(uuid);
	}

	public static Set<String> getJobList() {
		return jobs.keySet();
	}
}
