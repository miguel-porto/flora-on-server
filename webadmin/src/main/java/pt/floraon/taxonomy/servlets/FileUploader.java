package pt.floraon.taxonomy.servlets;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jline.internal.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pt.floraon.authentication.entities.User;
import pt.floraon.bibliography.entities.Reference;
import pt.floraon.driver.entities.Image;
import pt.floraon.driver.jobs.JobRunnerTask;
import pt.floraon.driver.jobs.JobSubmitter;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geocoding.ToponomyParser;
import pt.floraon.geocoding.entities.Toponym;
import pt.floraon.occurrences.OccurrenceImporterJob;
import pt.floraon.occurrences.dataproviders.iNaturalistDataProvider;
import pt.floraon.occurrences.dataproviders.iNaturalistFilter;
import pt.floraon.occurrences.dataproviders.iNaturalistImporterJob;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.server.FloraOnServlet;

@MultipartConfig
@WebServlet("/upload/*")
public class FileUploader extends FloraOnServlet {
	private static final long serialVersionUID = 1L;
	private String filePath;
	
	/*public void init() {
		filePath = getServletContext().getInitParameter("file-upload"); 
	}*/
	
	/**
	 * The GET method is for processing files stored in the local server. We don't need this!
	 */
	@Override
	@Deprecated
	public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		ListIterator<String> partIt=thisRequest.getPathIteratorAfter("upload");

		switch(partIt.next()) {
		case "authors":
/*
			success(
				new Gson().toJsonTree(driver.getCSVFileProcessor().uploadAuthorsFromFile(getParameterAsString("file")))
			);
*/
			break;
		
		case "occurrences":
			String type = thisRequest.getParameterAsString("type");
			File file = new File(thisRequest.getParameterAsString("file"));
			if(!file.canRead()) throw new IOException("Cannot read file "+thisRequest.getParameterAsString("file"));
			JobRunnerTask job = JobSubmitter.newJobTask(new OccurrenceImporterJob(new FileInputStream(file), driver, thisRequest.getUser(), type, true, false, false, null), driver);
			thisRequest.success(job.getID());
/*
			success(
				new Gson().toJsonTree(driver.getCSVFileProcessor().getOccurrenceImporter().uploadRecordsFromFile(getParameterAsString("file")))
			);
*/
			break;
		}
	}
	
	/**
	 * Upload a file, call the processing routine and store it locally in /tmp
	 */
	@Override
	public void doFloraOnPost(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
		String type = thisRequest.getParameterAsString("type");
		Part filePart;
		InputStream fileContent = null;
		Reader freader;

		ListIterator<String> partIt=thisRequest.getPathIteratorAfter("upload");
		switch(partIt.next()) {
        case "image":
            try {
                filePart = thisRequest.request.getPart("imageFile");
//				System.out.println(filePart.getSize());
                if(filePart.getSize() == 0) throw new FloraOnException("You must select a file.");
                fileContent = filePart.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(fileContent != null) {
            	Image imageNode = Image.createNew();
                File newImage = new File(driver.getOriginalImageFolder(), imageNode.getUuid() + ".jpg");
				if(!newImage.createNewFile())
                    throw new FloraOnException("Could not store image.");

				imageNode.setFileName(newImage.getName());

                OutputStream out = new FileOutputStream(newImage);
                org.apache.commons.io.IOUtils.copy(fileContent, out);
                fileContent.close();
                out.close();

                // resize to thumbnail
                BufferedImage original = ImageIO.read(newImage);
                imageNode.setWidth(original.getWidth());
                imageNode.setHeight(original.getHeight());
                java.awt.Image tmp = original.getScaledInstance(-1, 500, java.awt.Image.SCALE_SMOOTH);
                BufferedImage scaledImage = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
				Graphics2D g2d = scaledImage.createGraphics();
				g2d.drawImage(tmp, 0, 0, null);
				g2d.dispose();
				Kernel kernel = new Kernel(3, 3, new float[] { -1, -1, -1, -1, 9, -1, -1,
						-1, -1 });
				BufferedImageOp op = new ConvolveOp(kernel);

//				BufferedImageOp op = new UnsharpMaskFilter(50, 4, 20);
				scaledImage = op.filter(scaledImage, null);
				ImageIO.write(scaledImage, "jpg", new File(driver.getThumbsFolder(), imageNode.getUuid() + ".jpg"));
//                newImage.length()

/*
                BufferedImage tThumbImage = new BufferedImage( tThumbWidth, tThumbHeight, BufferedImage.TYPE_INT_RGB );
                Graphics2D tGraphics2D = tThumbImage.createGraphics(); //create a graphics object to paint to
                tGraphics2D.setBackground( Color.WHITE );
                tGraphics2D.setPaint( Color.WHITE );
                tGraphics2D.fillRect( 0, 0, tThumbWidth, tThumbHeight );
                tGraphics2D.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
                tGraphics2D.drawImage( tOriginalImage, 0, 0, tThumbWidth, tThumbHeight, null ); //draw the image scaled

                ImageIO.write( tThumbImage, "JPG", tThumbnailTarget ); //write the image to a fi
*/

				imageNode = driver.getImageManagement().addNewImage(imageNode);
                thisRequest.success(imageNode.getUuid());
            }
            break;

		case "occurrences":
			if("iNat".equals(type)) {
/*
				iNaturalistDataProvider iNat = new iNaturalistDataProvider();
				iNat.executeOccurrenceQuery((TaxEnt) null);
				System.out.println(iNat.size());
				for (Occurrence o : iNat) {
					System.out.println(o._getCoordinates() + o.getPubNotes());
				}
				for(Inventory inv : iNat) {
					inv.setMaintainer(thisRequest.getUser().getID());
					driver.getOccurrenceDriver().matchTaxEntNames(inv, false, false, null);
					driver.getOccurrenceDriver().createInventory(inv);
				}
*/
				User user = thisRequest.getUser();
				JobRunnerTask job;
				if(user.canMODIFY_OCCURRENCES()) {
					job = JobSubmitter.newJobTask(new iNaturalistImporterJob(user,
							user.getiNaturalistFilter().getTaxon_names(), user.getiNaturalistFilter().getIdent_user_id(),
							user.getiNaturalistFilter().getProject_id(), user.getiNaturalistFilter().getUser_id()), driver);
				} else {
					String pId;
					if((pId = driver.getDefaultINaturalistProject()) == null)
						throw new FloraOnException("Default iNaturalist project ID not defined");
					else {
						job = JobSubmitter.newJobTask(new iNaturalistImporterJob(user, null, null,
								pId, new String[]{user.getiNaturalistUserName()}), driver);
					}
				}
				thisRequest.success(job.getID());
			} else {

				boolean main = thisRequest.getParameterAsBoolean("mainobserver", false);
				boolean create = thisRequest.getParameterAsBoolean("createUsers", false);
				boolean createTaxa = thisRequest.getParameterAsBoolean("createTaxa", false);

				try {
					filePart = thisRequest.request.getPart("occurrenceTable");
					//				System.out.println(filePart.getSize());

					if (filePart.getSize() == 0) throw new FloraOnException("You must select a file.");
					fileContent = filePart.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (fileContent != null) {
					JobRunnerTask job = JobSubmitter.newJobTask(new OccurrenceImporterJob(
							fileContent, driver, thisRequest.getUser(), type, main, create, createTaxa, thisRequest.getUser()), driver);
					thisRequest.success(job.getID());
				}
			}
			break;

		case "toponyms":
			filePart = thisRequest.request.getPart("toponymTable");
			System.out.println(filePart.getSize());

			int counter = 0;
			ToponomyParser topoParser = new ToponomyParser();
			Gson gs = new GsonBuilder().setPrettyPrinting().create();

			freader = new InputStreamReader(filePart.getInputStream(), StandardCharsets.UTF_8);
			CSVParser records = CSVFormat.EXCEL.withDelimiter('\t').withHeader().parse(freader);
			Map<String, Integer> headers = records.getHeaderMap();

			List<Toponym> toponyms = new ArrayList<>();
			int chunk = 0;
			for (CSVRecord record : records) {
				Toponym topo = new Toponym();
				Map<String, String> recordValues = new HashMap<>();
				try {
					for (String col : headers.keySet())
						recordValues.put(col, record.get(col));

					topoParser.parseFields(recordValues, topo);
				} catch (FloraOnException | IllegalArgumentException e) {
					Log.warn(e.getMessage());
				}
				counter++;
				chunk++;
				toponyms.add(topo);

				if ((counter % 2500) == 0) {
					System.out.println(counter + " records processed.");
				}

				if(chunk > 500) {	// flush
					driver.getNodeWorkerDriver().createDocuments(toponyms);
					toponyms.clear();
					chunk = 0;
				}
			}
			driver.getNodeWorkerDriver().createDocuments(toponyms);

			thisRequest.success(filePart.getName());
			break;

		case "references":
			filePart = thisRequest.request.getPart("referenceTable");
			System.out.println(filePart.getSize());

			freader = new InputStreamReader(filePart.getInputStream(), StandardCharsets.UTF_8);
			CSVParser records1 = CSVFormat.EXCEL.withDelimiter('\t').withHeader().parse(freader);

			List<Long> errorLines = new ArrayList<>();
			List<Reference> references = new ArrayList<>();
			for (CSVRecord record : records1) {
				Reference tmp = new Reference();
				try {
					tmp.setPublicationType(record.get(0));
					tmp.setAuthors(record.get(1));
					tmp.setYear(record.get(2));
					tmp.setTitle(record.get(3));
					tmp.setPublication(record.get(4));
					tmp.setCoords(record.get(5));
					tmp.setVolume(record.get(6));
					tmp.setEditor(record.get(7));
					tmp.setCity(record.get(8));
					tmp.setPages(record.get(9));
					tmp.setCode(record.get(10));
					references.add(tmp);
				} catch (FloraOnException e) {
					System.out.println(e.getMessage());
					errorLines.add(record.getRecordNumber());
				}
			}

			if(errorLines.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for(Long l : errorLines)
					sb.append(l.toString()).append(" ");
				thisRequest.error("The following lines have errors: " + sb);
				return;
			}

			driver.getNodeWorkerDriver().createDocuments(references);
			thisRequest.success(filePart.getName());
			break;

			case "getInatCount":
				User user = thisRequest.getUser();
				if(user.canMODIFY_OCCURRENCES()) {
					ArrayList<String> sizes = new ArrayList<>();
					for(String tax : user.getiNaturalistFilter().getTaxon_names()) {
						iNaturalistFilter copy = new iNaturalistFilter(user.getiNaturalistFilter());
						copy.setTaxon_names(new String[] {tax});
						iNaturalistDataProvider idp = new iNaturalistDataProvider(copy, 1);
						Iterator<Occurrence> it = idp.iterator();
						sizes.add(Integer.toString(idp.size()));
					}
					thisRequest.success("Number of records that will be fetched: " + StringUtils.implode(" + ", sizes.toArray(new String[0])),
							true);
				}
		}

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
