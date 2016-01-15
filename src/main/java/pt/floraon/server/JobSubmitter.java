package pt.floraon.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class JobSubmitter {
	private static Map<String, Job> jobs=new HashMap<String, Job>();
	
	public static Job newJob(String url, String desiredFileName, ServerResponse server) throws IOException {
		Job j=new Job(url, desiredFileName, server);
		JobSubmitter.jobs.put(j.getID(), j);
		new Thread(j).start();
		return j;
	}
	
	public static Job getJob(String uuid) {
		return JobSubmitter.jobs.get(uuid);
	}
}
