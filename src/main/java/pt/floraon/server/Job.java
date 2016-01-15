package pt.floraon.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

import pt.floraon.driver.FloraOnException;

public class Job implements Runnable {
	private OutputStream outputStream;
	private File tmpFile;
	private boolean isClosed,hasError=false;
	private String uuid, url, desiredFileName;
	private ServerResponse server;
	
	
	public Job(String url, String desiredFileName, ServerResponse server) throws IOException {
		tmpFile=File.createTempFile("floraon_", null);
		outputStream= new FileOutputStream(tmpFile);
		isClosed=false;
		uuid=UUID.randomUUID().toString();
		this.desiredFileName=desiredFileName;
		this.url=url;
		this.server=server;
	}
	
	public String getID() {
		return this.uuid;
	}
	
	public String getFileName() {
		return desiredFileName;
	}
	
	public Boolean isReady() throws FloraOnException {
		if(hasError) throw new FloraOnException("Error occurred during processing");
		return isClosed;
	}
	
	public InputStream getInputStream() throws FloraOnException, FileNotFoundException {
		if(!isClosed) throw new FloraOnException("Job hasn't finished yet");
		if(hasError) throw new FloraOnException("Error occurred during processing");
		return new FileInputStream(this.tmpFile);
	}

	public InputStreamReader getInputStreamReader(Charset charset) throws FloraOnException, FileNotFoundException {
		if(!isClosed) throw new FloraOnException("Job hasn't finished yet");
		if(hasError) throw new FloraOnException("Error occurred during processing");
		return new InputStreamReader(new FileInputStream(this.tmpFile), charset);
	}
	
	public String getFileType() {
		return FilenameUtils.getExtension(desiredFileName);
	}
	
	@Override
	public void run() {
		try {
			server.processCommand(this.url,this.outputStream,false);
			this.outputStream.close();
			isClosed=true;
		} catch (URISyntaxException | IOException | FloraOnException e) {
			this.hasError=true;
			try {
				this.outputStream.close();
				isClosed=true;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
