package pt.floraon.redlistdata.occurrences;

import org.apache.commons.io.IOUtils;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Square;
import pt.floraon.geometry.UTMCoordinate;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.SVGMapExporter;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

/**
 * Process multiple taxa and aggregate their occurrences in maps
 */
public class TaxonOccurrenceProcessor implements SVGMapExporter {
    private final String gradient[] = {"#FFCDD2","#EF9A9A","#E57373","#EF5350","#F44336","#E53935","#D32F2F","#C62828","#B71C1C"};  //"#FFEBEE",
    private final long legendPosition[] = {670000, 4220000};
    private final long legendSize = 10000;
    //    private List<SimpleOccurrenceDataProvider> sodps;
//    private Iterator<TaxEnt> taxa;
    private Map<Square, Set<String>> squares = new HashMap<>();
//    private long sizeOfSquare;
    private float maxNSp;
//    private OccurrenceFilter filter;

    public TaxonOccurrenceProcessor(List<SimpleOccurrenceDataProvider> providers, Iterator<TaxEnt> taxa, long sizeOfSquare
            , OccurrenceFilter filter) throws IOException, FloraOnException {
//        this.sodps = providers;
//        this.taxa = taxa;
//        this.sizeOfSquare = sizeOfSquare;
//        this.filter = filter;

        while(taxa.hasNext()) {
            // for each taxon execute query
            TaxEnt te = taxa.next();
            System.out.println("Querying "+te.getFullName());
            for(SimpleOccurrenceDataProvider edp : providers) {
//                if(edp instanceof FloraOnDataProvider) continue;

                edp.executeOccurrenceQuery(te);
                Iterator<Occurrence> it = edp.iterator();

                Occurrence so;
                UTMCoordinate tmp;
                Point2D tmp1;
                // iterate through all occurrences of this taxon
                while(it.hasNext()) {
                    so = it.next();
                    if(!filter.enter(so)) continue;

                    tmp = so._getUTMCoordinates();
                    if(tmp == null) continue;
                    if(tmp.getXZone() != 29) continue;  // TODO: must check if within the map in other way!

                    tmp1 = new Point2D(tmp.getX(), tmp.getY());

                    // compute in which UTM square it falls
                    Square sq = new Square(tmp1, sizeOfSquare);

                    // add this taxon to the UTM square map
                    if(squares.containsKey(sq)) {
                        squares.get(sq).add(te.getNameWithAnnotationOnly(true));
                    } else {
                        Set<String> tmpset = new HashSet<>();
                        tmpset.add(te.getNameWithAnnotationOnly(true));
                        squares.put(sq, tmpset);
                    }
                }
            }
        }
        maxNSp = 0;
        for(Map.Entry<Square, Set<String>> sqs : squares.entrySet()) {
            if(sqs.getValue().size() > maxNSp)
                maxNSp = sqs.getValue().size();
        }
//        maxNSp = (float) Math.log(maxNSp);
    }

    @Override
    public void exportSVG(PrintWriter out, boolean showOccurrences, boolean showConvexhull, boolean showBaseMap
            , boolean standAlone, int border, boolean showShadow, boolean showProtectedAreas) {
        if(showBaseMap) {
            InputStream str = TaxonOccurrenceProcessor.class.getResourceAsStream(showShadow ? "../basemap.svg" : "../basemap-noshadow.svg");
            try {
                IOUtils.copy(str, out);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            out.print("<svg class=\"svgmap\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:lvf=\"http://flora-on.pt\" preserveAspectRatio=\"xMidYMin meet\" viewBox=\"440000 4090000 300000 597000\">" +
                    "<g transform=\"translate(0,8767000) scale(1,-1)\">");
        }

/*
        if(protectedAreas != null) {
            // draw protected areas
            List<UTMCoordinate> tmp;
            for (Map.Entry<String, pt.floraon.geometry.Polygon> p : protectedAreas) {
                tmp = p.getValue().getUTMCoordinates();
                out.print("<path class=\"protectedarea\" d=\"M" + tmp.get(0).getX() + " " + tmp.get(0).getY());
                for (int i = 1; i < tmp.size(); i++) {
                    out.print("L" + tmp.get(i).getX() + " " + tmp.get(i).getY());
                }
                out.print("\"></path>");
            }
        }
        // draw convex hull
        if(showConvexhull && convexHull != null) {
            out.print("<path class=\"convexhull\" d=\"M" + (int) convexHull.get(0).x() + " " + (int) convexHull.get(0).y());
            for (int i = 1; i < convexHull.size(); i++) {
                out.print("L" + (int) convexHull.get(i).x() + " " + (int) convexHull.get(i).y());
            }
            out.print("\"></path>");
        }
*/


        if(showOccurrences && this.squares != null) {
            // draw occurrence squares
            for (Square s : this.squares.keySet()) {
                Rectangle2D s1 = s.getSquare();
                float prop = (float) this.squares.get(s).size() / maxNSp;
//                float prop = (float) Math.log(this.squares.get(s).size()) / maxNSp;
//                String color = String.format("#%02x%02x%02x", (int) (prop * 255f), 60, 0);
                String color = gradient[(int)(prop * 8f)];
                if(standAlone) {
                    out.print("<rect class=\"utmsquare\" style=\"fill:" + color + "; stroke:white; stroke-width:" + border);
                    out.print("px\" vector-effect=\"non-scaling-stroke\" lvf:quad=\"" + s.getMGRS() + "\" lvf:taxa=\"");
                    out.print("\" x=\"" + s1.getMinX()+ "\" y=\"" + s1.getMinY() + "\" width=\"" + s1.getWidth() + "\" height=\"" + s1.getHeight() + "\">");
                    out.print("<title>" + StringUtils.implode("\n", this.squares.get(s).toArray(new String[0])) + "</title></rect>");
                } else
                    out.print("<rect lvf:quad=\"" + s.getMGRS() + "\" x=\"" + s1.getMinX() + "\" y=\"" + s1.getMinY()
                            + "\" width=\"" + s1.getWidth()
                            + "\" height=\"" + s1.getHeight()
                            + "\" style=\"fill:" + color + "\"/>");
            }
        }

        // show legend
/*
        out.print("<g style=\"transform-origin: center\" transform=\"scale(1,-1)\">");
        for(int i = 0; i < gradient.length; i++) {
            String color = gradient[i];
            int nsp = (int)((float) i / ((float) gradient.length - 1f) * (float) maxNSp);

            out.print("<rect class=\"legendsquare\" style=\"fill:" + color + "; stroke:white; stroke-width:" + border
                    + "px\" vector-effect=\"non-scaling-stroke\" x=\"" + legendPosition[0]
                    + "\" y=\"" + (legendPosition[1] + i * legendSize) + "\" width=\"" + legendSize + "\" height=\"" + legendSize + "\"/>");
            out.print("<text x=\"" + legendPosition[0] + "\" y=\"" + (legendPosition[1] + i * legendSize) + "\">" + nsp + "</text>");
        }
        out.print("</g>");
*/

        out.print("</g></svg>");
    }
}
