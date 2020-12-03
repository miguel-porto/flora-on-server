package pt.floraon.redlistdata.dataproviders;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import jline.internal.Log;
import org.apache.http.client.utils.URIBuilder;
import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * A class to fetch records from flora-on.pt
 * Created by miguel on 02-11-2016.
 */
public class FloraOnDataProvider extends SimpleOccurrenceDataProvider {
    final private URL floraOnURL;
    final private IFloraOn driver;

    private class FloraOnOccurrence {
        String autor, genero, especie, subespecie, notas, dateinserted;
        int id_reg, id_ent, ano, mes, dia, precisao, espontanea;
        float latitude, longitude;
        boolean duvida, validado, destroyed;
        Boolean floracao;
    }

    public FloraOnDataProvider(URL url, IFloraOn driver) {
        this.floraOnURL = url;
        this.driver = driver;
    }

    private void readBigJsonFromStream(InputStream stream) throws IOException {
        DateFormat df = Constants.dateFormatYMD.get();
        occurrenceList = new ArrayList<>();
        Gson gson = new Gson();
        JsonReader jr;
        jr = new JsonReader(new InputStreamReader(stream));
        //        occArray = new ArrayList<>();
        jr.beginObject();
        while (jr.hasNext()) {
            if(jr.peek() == JsonToken.BEGIN_ARRAY) {
                jr.beginArray();
                while (jr.hasNext()) {
                    FloraOnOccurrence o = gson.fromJson(jr, FloraOnOccurrence.class);
                    Occurrence so = new Occurrence(this.getDataSource(), o.latitude, o.longitude, o.ano, o.mes, o.dia, o.autor, o.genero
                            , o.especie, o.subespecie, o.notas, o.id_reg, o.id_ent
                            , o.precisao == 0 ? "1" : (o.precisao == 1 ? "100" : (o.precisao == 2 ? "1000x1000" : "10000x10000"))
                            , o.duvida ? OccurrenceConstants.ConfidenceInIdentifiction.DOUBTFUL : OccurrenceConstants.ConfidenceInIdentifiction.CERTAIN
                            , o.floracao == null ? null : Constants.PhenologicalStates.FLOWER
                            , o.espontanea == 1);

                    if(o.dateinserted != null) {
                        try {
                            so.getOccurrence().setDateInserted(df.parse(o.dateinserted));
                        } catch (ParseException e) {
                            // just ignore
                        }
                    }

                    if(!o.validado)
                        so.getOccurrence().setPresenceStatus(OccurrenceConstants.PresenceStatus.PROBABLY_MISIDENTIFIED);
                    else if(o.destroyed)
                        so.getOccurrence().setPresenceStatus(OccurrenceConstants.PresenceStatus.DESTROYED);

                    occurrenceList.add(so);
                }
                jr.endArray();
            } else jr.skipValue();
        }
        jr.endObject();
        jr.close();

//        this.occurrences = occurrenceList.iterator();
    }

    @Override
    public void executeOccurrenceQuery(Iterator<TaxEnt> taxa) throws FloraOnException, IOException {
        // FIXME when all records!
        String legacyID;
        if(taxa != null) {
            Set<Integer> oldIds = new HashSet<>();
            while(taxa.hasNext()) {
                TaxEnt te = taxa.next();
                if(te == null) return;
                Iterator<TaxEnt> it1 = driver.wrapTaxEnt(driver.asNodeKey(te.getID())).getInfrataxa(1000);
                while (it1.hasNext()) {
                    TaxEnt t1 = it1.next();
                    if (t1.getOldId() != null) oldIds.add(t1.getOldId());
                }
            }

            legacyID = Arrays.toString(oldIds.toArray(new Integer[oldIds.size()]));
            legacyID = legacyID.substring(1, legacyID.length());
        } else {
            return;
            //legacyID = "all";
        }

        URI oldUri;
        try {
            oldUri = floraOnURL.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new FloraOnException(e.getMessage());
        }

        String newQuery = oldUri.getQuery();
        if (newQuery == null) {
            newQuery = "what=occurrences&id=" + legacyID;
        } else {
            newQuery += "&what=occurrences&id=" + legacyID;
        }

        Log.info("Executing Flora-On query");
        URI newUri;
        URL u;
        try {
            newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery, oldUri.getFragment());
            u = newUri.toURL();
            readBigJsonFromStream(u.openStream());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new FloraOnException("Não foi possível descarregar os registos do Flora-On (" + e.toString() + ")");
        }

//        Log.info("Address: " + u.toString());

/*        Type listType = new TypeToken<List<FloraOnOccurrence>>() {
        }.getType();
*/



/*
        InputStreamReader isr = new InputStreamReader(u.openStream());
        JsonElement resp = new JsonParser().parse(isr);
        if (!resp.getAsJsonObject().getAsJsonPrimitive("success").getAsBoolean()) {
            throw new FloraOnException(resp.getAsJsonObject().getAsJsonPrimitive("msg").getAsString());
        }
        occArray = new Gson().fromJson(resp.getAsJsonObject().getAsJsonArray("msg"), listType);


        occurrenceList = new ArrayList<>();
        for (FloraOnOccurrence o : occArray) {
            if (!o.duvida)
                occurrenceList.add(new Occurrence(o.latitude, o.longitude, o.ano, o.mes, o.dia, o.autor, o.genero
                        , o.especie, o.subespecie, o.notas, o.id_reg, o.id_ent, o.precisao, !o.duvida, o.floracao));
        }
        */
    }

    @Override
    public boolean canQueryText() {
        return true;
    }

    @Override
    public void executeOccurrenceTextQuery(String query) throws FloraOnException, IOException {
        URI oldUri;
        try {
            oldUri = floraOnURL.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new FloraOnException(e.getMessage());
        }
        URIBuilder ub = new URIBuilder(oldUri);
        ub.addParameter("what", "occurrences");
        ub.addParameter("query", query);

        URL u;
        try {
            u = ub.build().toURL();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
//System.out.println(u.toString());
        readBigJsonFromStream(u.openStream());
    }

    @Override
    public Map<String, Object> executeInfoQuery(Object query) throws FloraOnException, IOException {
        int legacyID = (int) query;
        URI oldUri;
        try {
            oldUri = floraOnURL.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new FloraOnException(e.getMessage());
        }

        String newQuery = oldUri.getQuery();
        if (newQuery == null) {
            newQuery = "what=info&id=" + legacyID;
        } else {
            newQuery += "&what=info&id=" + legacyID;
        }

        URI newUri;
        URL u;
        try {
            newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery, oldUri.getFragment());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
        u = newUri.toURL();

        InputStreamReader isr;
        try {
            isr = new InputStreamReader(u.openStream());
        } catch(Exception e) {
            return Collections.emptyMap();
        }
        JsonObject resp = new JsonParser().parse(isr).getAsJsonObject();
        if (!resp.getAsJsonPrimitive("success").getAsBoolean()) {
            return Collections.emptyMap();
//            throw new FloraOnException(resp.getAsJsonPrimitive("msg").getAsString());
        }

        Type listType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> occArray;
        occArray = new Gson().fromJson(resp.getAsJsonObject("msg"), listType);
/*
        System.out.println(resp.toString());
        for (String s : occArray.keySet())
            System.out.println(s);
*/
        return occArray;
    }

    @Override
    public String getDataSource() {
        return "Flora-On";
    }
}
