package pt.floraon.redlistdata.occurrences;

import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.geometry.*;
import pt.floraon.geometry.gridmaps.ColoredSquare;
import pt.floraon.geometry.gridmaps.GridMap;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.redlistdata.GridMapExporter;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrenceDataProvider;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Process multiple taxa and aggregate their occurrences in maps
 */
public class TaxonOccurrenceProcessor implements GridMapExporter {
    private final String gradient[] = {"#FFCDD2","#EF9A9A","#E57373","#EF5350","#F44336","#E53935","#D32F2F","#C62828","#B71C1C"};  //"#FFEBEE",
    private final long legendPosition[] = {670000, 4220000};
    private final long legendSize = 10000;
    //    private List<SimpleOccurrenceDataProvider> sodps;
//    private Iterator<TaxEnt> taxa;
//    private Map<Square, Set<String>> squares = new HashMap<>();
    private GridMap<ColoredSquare> squares = new GridMap<>();
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
                    ColoredSquare square = new ColoredSquare(tmp1, sizeOfSquare);

                    // add this taxon to the UTM square map
                    if(squares.containsKey(square)) {
                        squares.get(square).add(te.getNameWithAnnotationOnly(true));
                    } else {
                        Set<String> speciesSet = new HashSet<>();
                        speciesSet.add(te.getNameWithAnnotationOnly(true));
                        squares.put(square, speciesSet);
                    }
                }
            }
        }
/*
        maxNSp = 0;
        for(Map.Entry<ColoredSquare, Set<String>> sqs : squares.entrySet()) {
            if(sqs.getValue().size() > maxNSp)
                maxNSp = sqs.getValue().size();
        }
//        maxNSp = (float) Math.log(maxNSp);
*/
    }

/*
    public void exportSVG(PrintWriter out, boolean showOccurrences, boolean showConvexhull, boolean showBaseMap
            , boolean standAlone, int border, boolean showShadow, boolean showProtectedAreas, RedListSettings redListSettings) {
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


        if(showOccurrences && this.squares != null) {
            // draw occurrence squares
            for (Square s : this.squares.keySet()) {
                Rectangle2D s1 = s.getSquare();
                float prop = (float) this.squares.get(s).getNumber() / maxNSp;
                String color = gradient[(int)(prop * 8f)];
                if(standAlone) {
                    out.print("<rect class=\"utmsquare\" style=\"fill:" + color + "; stroke:white; stroke-width:" + border);
                    out.print("px\" vector-effect=\"non-scaling-stroke\" lvf:quad=\"" + s.getMGRS() + "\" lvf:taxa=\"");
                    out.print("\" x=\"" + s1.getMinX()+ "\" y=\"" + s1.getMinY() + "\" width=\"" + s1.getWidth() + "\" height=\"" + s1.getHeight() + "\">");
                    out.print("<title>" + this.squares.get(s).getText() + "</title></rect>");
                } else
                    out.print("<rect lvf:quad=\"" + s.getMGRS() + "\" x=\"" + s1.getMinX() + "\" y=\"" + s1.getMinY()
                            + "\" width=\"" + s1.getWidth()
                            + "\" height=\"" + s1.getHeight()
                            + "\" style=\"fill:" + color + "\"/>");
            }
        }

        // show legend
*/
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
*//*


        out.print("</g></svg>");
    }
*/

    @Override
//    public Iterable<Map.Entry<ColoredSquare, Set<String>>> squares() {
    public GridMap<ColoredSquare> squares() {
        return this.squares;
/*
        return new Iterable<Map.Entry<Square, Set<String>>>() {
            @Override
            public Iterator<Map.Entry<Square, Set<String>>> iterator() {
                if(TaxonOccurrenceProcessor.this.squares == null)
                    return Collections.emptyIterator();
                else
                    return TaxonOccurrenceProcessor.this.squares.entrySet().iterator();
            }
        };
*/
    }
}
