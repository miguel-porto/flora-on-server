package pt.floraon.server;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Diverts an HttpResponse to the provided writer.
 */
public class HttpServletResponse2Writer extends HttpServletResponseWrapper {
//    private final CharArrayWriter charArray = new CharArrayWriter();
    private final PrintWriter writer;

    public HttpServletResponse2Writer(HttpServletResponse response, PrintWriter writer) {
        super(response);
        this.writer = writer;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.writer;
//        return new PrintWriter(charArray);
    }

/*
    public String getOutput() {
        return charArray.toString();
    }
*/
}
