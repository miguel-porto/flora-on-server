package pt.floraon.images;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.entities.Image;
import pt.floraon.server.FloraOnServlet;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/photos/*")
public class ImageApi extends FloraOnServlet {
    private final Pattern imageFileName = Pattern.compile("^(?<uuid>[a-zA-Z0-9]{4,})(?<size>_\\w+)?.jpg$");
    @Override
    public void doFloraOnGet(ThisRequest thisRequest) throws ServletException, IOException, FloraOnException {
        ListIterator<String> path = thisRequest.getPathIteratorAfter("photos");
        Matcher matcher = imageFileName.matcher(path.next());
        String uuid, size;
        if(matcher.find()) {
            uuid = matcher.group("uuid");
            size = matcher.group("size");
        } else {
            thisRequest.response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Image image = driver.getImageManagement().getImageFromUUID(uuid);
        if(image == null) {
            thisRequest.response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        thisRequest.response.setContentType("image/jpg");
        File imageFile;
        if(size == null) size = "_low";
        switch(size) {
            case "_low":
            default:
                imageFile = new File(driver.getThumbsFolder(), image.getFileName());
                break;

            case "_high":
                imageFile = new File(driver.getOriginalImageFolder(), image.getFileName());
                break;
        }

        InputStream is = new FileInputStream(imageFile);
        BufferedImage bi = ImageIO.read(is);
        ImageIO.write(bi, "jpg", thisRequest.response.getOutputStream());
    }
}
