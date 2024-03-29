package pt.floraon.occurrences.dataproviders;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import jline.internal.Log;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.client.utils.URIBuilder;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.geometry.Precision;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.entities.Occurrence;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iNaturalistDataProvider implements Iterable<Occurrence> {
    private static final Pattern coordParse = Pattern.compile("^(?<lat>[0-9-]+(\\.[0-9]+)?)[\\s,;]+(?<lng>[0-9-]+(\\.[0-9]+)?)$");
    static private URL iNatURL;
    // do not request faster than one per this number of milliseconds
    static private long iNaturalistRequestThrottle = 1000;
    private final iNaturalistFilter iNaturalistFilter;
    final private iNaturalistDataProvider.iNaturalistTranslate iNaturalistTranslator = new iNaturalistTranslate();
    private Integer resultsPerPage;
    final private Map<String, String> requestParameters = new HashMap<>();
    private Integer totalCount;
    private StopWatch stopWatch;
    private URL currentRequestURL;
    private String errorMessage = null;

    static {
        try {
            iNatURL = new URL("https://api.inaturalist.org/v1/observations");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    {
        requestParameters.put("order_by", "id");
        requestParameters.put("page", "1");
    }

    public iNaturalistDataProvider(iNaturalistFilter iNaturalistFilter) throws IllegalArgumentException {
        this(iNaturalistFilter, 200);
    }

    public URL getCurrentRequestURL() {
        return this.currentRequestURL;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public iNaturalistDataProvider(iNaturalistFilter iNaturalistFilter, Integer resultsPerPage) throws IllegalArgumentException {
        if(!StringUtils.isArrayEmpty(iNaturalistFilter.getTaxon_names()) && iNaturalistFilter.getTaxon_names().length > 1)
            throw new IllegalArgumentException("Only one taxon is permitted by the current API version");
        this.iNaturalistFilter = iNaturalistFilter;
        this.resultsPerPage = resultsPerPage;
        requestParameters.put("per_page", resultsPerPage.toString());
    }

    public int size() {
        return totalCount == null ? -1 : totalCount;
    }

    @Override
    public Iterator<Occurrence> iterator() {
        try {
            return new iNaturalistDataIterator();
        } catch (FloraOnException e) {
            e.printStackTrace();
            return Collections.emptyIterator();
        }
    }

    class iNaturalistDataIterator implements Iterator<Occurrence> {
        private iNaturalistOccurrence lastOccurrence;
        private Long lastId = null;
        private JsonReader reader;
        private final Gson gson = new Gson();

        iNaturalistDataIterator() throws FloraOnException {
            doRequest(null);
        }

        private void doRequest(Long idBelow) throws FloraOnException {
            URI oldUri;
            try {
                oldUri = iNatURL.toURI();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw new FloraOnException(e.getMessage());
            }
            URIBuilder ub = new URIBuilder(oldUri);
            for(Map.Entry<String, String> p : requestParameters.entrySet())
                ub.addParameter(p.getKey(), p.getValue());

            if(idBelow != null)
                ub.addParameter("id_below", idBelow.toString());

            if(iNaturalistFilter != null) {
                if(!StringUtils.isStringEmpty(iNaturalistFilter.getProject_id()))
                    ub.addParameter("project_id", iNaturalistFilter.getProject_id());
                if(!StringUtils.isArrayEmpty(iNaturalistFilter.getTaxon_names()))
                    ub.addParameter("taxon_name", iNaturalistFilter.getTaxon_names()[0]);
                if(!StringUtils.isArrayEmpty(iNaturalistFilter.getUser_id()))
                    ub.addParameter("user_id", StringUtils.implode(",", iNaturalistFilter.getUser_id()));
                if(!StringUtils.isArrayEmpty(iNaturalistFilter.getIdent_user_id()))
                    ub.addParameter("ident_user_id", StringUtils.implode(",", iNaturalistFilter.getIdent_user_id()));
                if(!StringUtils.isStringEmpty(iNaturalistFilter.getCreated_d1()))
                    ub.addParameter("created_d1", iNaturalistFilter.getCreated_d1());
            }

            Log.info("Executing iNaturalist query:");
            try {
                currentRequestURL = ub.build().toURL();
                Log.info(currentRequestURL.toString());
            } catch (URISyntaxException | MalformedURLException e) {
                throw new FloraOnException("Não foi possível descarregar os registos do iNaturalist (" + e + ")");
            }
//            reader = null;
            try {
                if(stopWatch == null) {
                    stopWatch = new StopWatch();
                } else {
                    stopWatch.stop();
                    if(stopWatch.getTime() < iNaturalistRequestThrottle) {
                        Log.info("Waiting", (iNaturalistRequestThrottle - stopWatch.getTime()));
                        Thread.sleep(iNaturalistRequestThrottle - stopWatch.getTime());
                        Log.info("Go!");
                    }
                }
                stopWatch.reset();
                stopWatch.start();
                // do the actual request!
                reader = new JsonReader(new InputStreamReader(currentRequestURL.openStream()));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    switch(name) {
                        case "results":
                            reader.beginArray();
                            return;
                        case "total_results":
                            Integer tmp = gson.fromJson(reader, Integer.class);
                            if(totalCount == null) {
                                totalCount = tmp;
                                Log.info("Total results:", totalCount);
                            }
                            break;

                        default:
                            reader.skipValue();
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new FloraOnException(e.getMessage());
            }
        }

        @Override
        public boolean hasNext() {
            if(reader == null) return false;
            boolean hasNext;
            boolean freshRequest = false;
            do {
                try {
                    hasNext = reader.hasNext();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                if (hasNext) {
//                    freshRequest = false;
                    try {
                        // TODO: JsonSyntaxException: com.google.gson.stream.MalformedJsonException: Unterminated string at line 1 column 10708594 path $.results[194].identifications[1].taxon.ancestors[0].
                        lastOccurrence = gson.fromJson(reader, iNaturalistOccurrence.class);
                        lastId = lastOccurrence.id;
                    } catch (JsonSyntaxException e) {   // Fatal error in JSON, stop here
                        e.printStackTrace();
                        lastOccurrence = null;
                        iNaturalistDataProvider.this.errorMessage = e.getMessage();
                        return false;
                    }
//                    return true;

                    if(!StringUtils.isArrayEmpty(iNaturalistFilter.getExclude_user_id()) && lastOccurrence != null) {
                        if(Arrays.asList(iNaturalistFilter.getExclude_user_id()).contains(lastOccurrence.user.login)) {
                            Log.info("Skipped record", lastOccurrence.id, ", observer excluded by filter");
                            continue;
                        }
                    }
                    return true;
/*
                    if(iNaturalistFilter.taxon_name_Matches(lastOccurrence))
                        return true;
                    else {
                        Log.info("Skipped record", lastOccurrence.id, ", does not match taxon name filter");
                        continue;
                    }
*/
                }

                // this happens when the requested page is empty. If it is, we reached the end.
                if(freshRequest) break;

                // results array ends for this page
                try {
                    reader.endArray();
                    reader.endObject();
                    reader.close();
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // do another request using last occurrence ID as condition
                try {
                    if (lastId == null)
                        doRequest(null);
                    else
                        doRequest(lastId);
                } catch (FloraOnException e) {
                    e.printStackTrace();
                    return false;
                }
                freshRequest = true;
                // now we have a new reader
            } while(true);
            return false;
        }

        @Override
        public Occurrence next() {
            if(reader == null) return null;
            if(lastOccurrence != null) {
                Occurrence o = iNaturalistTranslator.translate(lastOccurrence);
                // if one of the trusted identifiers disagrees with the identification, mark as PROBABLY_MISIDENTIFIED
                for(iNaturalistDataProvider.iNaturalistIdentification i : lastOccurrence.identifications) {
                    if(ArrayUtils.contains(iNaturalistFilter.getIdent_user_id(), i.user.login)) {
                        if(!i.taxon.name.equals(o.getOccurrence().getVerbTaxon()))
                            o.getOccurrence().setPrivateComment("MISIDENTIFIED? " + o.getOccurrence().getPrivateComment());
//                            o.getOccurrence().setPresenceStatus(OccurrenceConstants.PresenceStatus.PROBABLY_MISIDENTIFIED);
                    }
                }
                return o;
            } else return null;
//            lastOccurrence = gson.fromJson(reader, iNaturalistOccurrence.class);
        }

        @Override
        public void remove() {
        }
    }

    static class iNaturalistTaxon {
        String name, rank, min_species_ancestry;
        int min_species_taxon_id;
    }

    static class iNaturalistUser {
        String name, login;
    }

    static class iNaturalistIdentification {
        iNaturalistUser user;
        iNaturalistTaxon taxon;
    }

    static class iNaturalistObservedOnDetails {
        Date date;
    }

    static class iNaturalistOccurrence {
        long id;
        UUID uuid;
        Integer public_positional_accuracy;
        String quality_grade, location, uri;
        Date time_observed_at;
        iNaturalistObservedOnDetails observed_on_details;
        iNaturalistTaxon taxon;
        iNaturalistUser user;
        iNaturalistIdentification[] identifications;
    }

    public static class iNaturalistTranslate implements DataProviderTranslator {
        @Override
        public Occurrence translate(Object o) {
            iNaturalistDataProvider.iNaturalistOccurrence iNaturalistOccurrence = (iNaturalistDataProvider.iNaturalistOccurrence) o;
            Occurrence so = new Occurrence();
            so.setSource("iNaturalist");
            so.setReadOnly(true);
            // NOTE: we temporarily set the observers with the user names (not IDs), but later on, we will lookup
            // and replace with IDs
            so.setObservers(new String[]{iNaturalistOccurrence.user.login, iNaturalistOccurrence.user.name});
            //so.setPubNotes("ID: " + iNaturalistOccurrence.id);

//            if(iNaturalistOccurrence.time_observed_at != null) {
            if(iNaturalistOccurrence.observed_on_details != null && iNaturalistOccurrence.observed_on_details.date != null) {
                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(iNaturalistOccurrence.time_observed_at);
                calendar.setTime(iNaturalistOccurrence.observed_on_details.date);
                so.setYear(calendar.get(Calendar.YEAR));
                so.setMonth(calendar.get(Calendar.MONTH) + 1);
                so.setDay(calendar.get(Calendar.DAY_OF_MONTH));
            }
            if(!StringUtils.isStringEmpty(iNaturalistOccurrence.location)) {
                Matcher mat = coordParse.matcher(iNaturalistOccurrence.location);
                if (mat.find()) {
                    so.setLatitude(Float.parseFloat(mat.group("lat")));
                    so.setLongitude(Float.parseFloat(mat.group("lng")));
                }
            }

            if(iNaturalistOccurrence.public_positional_accuracy != null) {
                try {
                    so.setPrecision(new Precision(iNaturalistOccurrence.public_positional_accuracy + "m"));
                } catch (FloraOnException e) {
                    // ignore precision
                }
            }

            // fill in the occurrence data
            so.setOccurrence(new OBSERVED_IN());
            StringBuilder sb = new StringBuilder();
            for(iNaturalistIdentification i : iNaturalistOccurrence.identifications) {
                sb.append(i.user.login).append(": ").append(i.taxon.name).append("; ");
            }
            so.getOccurrence().setPrivateComment("IDs: " + sb);

            so.getOccurrence().setDateInserted(new Date());
            so.getOccurrence().setUuid(iNaturalistOccurrence.uuid);
            so.getOccurrence().setUri(iNaturalistOccurrence.uri);
            if(iNaturalistOccurrence.taxon != null)
                so.getOccurrence().setVerbTaxon(iNaturalistOccurrence.taxon.name);

            return so;
        }
    }
}
