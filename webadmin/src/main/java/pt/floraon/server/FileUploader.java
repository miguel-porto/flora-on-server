package pt.floraon.server;

import java.io.File;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUploader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;
	private String filePath;
	
	public void init() {
		filePath = getServletContext().getInitParameter("file-upload"); 
	}
	
	
	
public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
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
