package pt.floraon.jobs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pt.floraon.driver.FloraOn;

public final class JobSubmitter {
	private static Map<String, JobRunner> jobs=new HashMap<String, JobRunner>();
	
	public static JobRunner newJob(Job job, String desiredFileName, FloraOn driver) throws IOException {
		JobRunner j=new JobRunner(job, desiredFileName, driver);
		JobSubmitter.jobs.put(j.getID(), j);
		new Thread(j).start();
		return j;
	}
	
	public static JobRunner getJob(String uuid) {
		return JobSubmitter.jobs.get(uuid);
	}
}
