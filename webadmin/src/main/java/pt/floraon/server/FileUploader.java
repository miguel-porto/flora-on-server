package pt.floraon.server;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;

import com.arangodb.entity.EntityFactory;

import pt.floraon.driver.CSVFileProcessor;
import pt.floraon.driver.FloraOnException;

public class FileUploader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;
	private String filePath;
	
	/*public void init() {
		filePath = getServletContext().getInitParameter("file-upload"); 
	}*/
	
	/**
	 * The GET method is for processing files stored in the local server.
	 */
	@Override
	public void doFloraOnGet() throws ServletException, IOException, FloraOnException {
		CSVFileProcessor cfp=new CSVFileProcessor(driver);
		ListIterator<String> partIt=this.getPathIteratorAfter("upload");
		
		switch(partIt.next()) {
		case "authors":
			success(
				EntityFactory.toJsonElement(cfp.uploadAuthorsFromFile(getParameterAsString("file")), false)	// TODO: don't use EntityFactory 
			);
			break;
		
		case "occurrences":
			success(
				EntityFactory.toJsonElement(cfp.uploadRecordsFromFile(getParameterAsString("file")), false) // TODO: don't use EntityFactory
			);
			break;
		}
	}
	
	/**
	 * Upload a file, store it locally and call the processing routine.
	 */
	@Override
	public void doFloraOnPost() throws ServletException {
      // Check that we have a file upload request
	/*
  boolean isMultipart = ServletFileUpload.isMultipartContent(request);
  response.setContentType("text/html");
  java.io.PrintWriter out = response.getWriter( );
  if( !isMultipart ){
     out.println("<html>");
 out.println("<head>");
 out.println("<title>Servlet upload</title>");  
 out.println("</head>");
 out.println("<body>");
 out.println("<p>No file uploaded</p>"); 
 out.println("</body>");
 out.println("</html>");
     return;
  }
  DiskFileItemFactory factory = new DiskFileItemFactory();
  // maximum size that will be stored in memory
  factory.setSizeThreshold(maxMemSize);
  // Location to save data that is larger than maxMemSize.
  factory.setRepository(new File("c:\\temp"));

  // Create a new file upload handler
  ServletFileUpload upload = new ServletFileUpload(factory);
  // maximum file size to be uploaded.
  upload.setSizeMax( maxFileSize );

  try{ 
  // Parse the request to get file items.
  List fileItems = upload.parseRequest(request);

  // Process the uploaded file items
  Iterator i = fileItems.iterator();

  out.println("<html>");
  out.println("<head>");
  out.println("<title>Servlet upload</title>");  
  out.println("</head>");
  out.println("<body>");
  while ( i.hasNext () ) 
  {
     FileItem fi = (FileItem)i.next();
     if ( !fi.isFormField () )	
     {
        // Get the uploaded file parameters
String fieldName = fi.getFieldName();
String fileName = fi.getName();
String contentType = fi.getContentType();
boolean isInMemory = fi.isInMemory();
long sizeInBytes = fi.getSize();
// Write the file
if( fileName.lastIndexOf("\\") >= 0 ){
   file = new File( filePath + 
   fileName.substring( fileName.lastIndexOf("\\"))) ;
}else{
   file = new File( filePath + 
   fileName.substring(fileName.lastIndexOf("\\")+1)) ;
}
fi.write( file ) ;
out.println("Uploaded Filename: " + fileName + "<br>");
     }
  }
  out.println("</body>");
  out.println("</html>");
   }catch(Exception ex) {
       System.out.println(ex);
   }*/
}
}
