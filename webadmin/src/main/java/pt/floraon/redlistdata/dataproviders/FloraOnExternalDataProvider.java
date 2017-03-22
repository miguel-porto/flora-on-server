package pt.floraon.redlistdata.dataproviders;

import com.google.gson.*;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import pt.floraon.driver.FloraOnException;
import pt.floraon.redlistdata.ExternalDataProvider;
import pt.floraon.geometry.IPolygonTheme;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Polygon;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * A class to fetch records from flora-on.pt
 * Created by miguel on 02-11-2016.
 */
public class FloraOnExternalDataProvider extends ExternalDataProvider {
    final private URL floraOnURL;

    private class FloraOnOccurrence {
        String autor, genero, especie, subespecie, notas;
        int id_reg, id_ent, ano, mes, dia, precisao;
        float latitude, longitude;
        boolean duvida;
        Boolean floracao;
    }

    public FloraOnExternalDataProvider(URL url) {
        this.floraOnURL = url;
    }

    @Override
    public void executeOccurrenceQuery(Object query) throws FloraOnException, IOException {
        // FIXME when all records!
        String legacyID;
        if(query != null)
            legacyID = query.toString();
        else
            legacyID = "all";

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

        URI newUri;
        URL u;
        try {
            newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery, oldUri.getFragment());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new FloraOnException(e.getMessage());
        }
        u = newUri.toURL();

        Type listType = new TypeToken<List<FloraOnOccurrence>>() {
        }.getType();
        List<FloraOnOccurrence> occArray;

        Gson gson = new Gson();
        JsonReader jr = new JsonReader(new InputStreamReader(u.openStream()));
        JsonElement resp;
//        occArray = new ArrayList<>();
        occurrenceList = new ArrayList<>();
        jr.beginObject();
        int c=0;
        while (jr.hasNext()) {
//            System.out.print("OBJ");
            if(jr.peek() == JsonToken.BEGIN_ARRAY) {
                jr.beginArray();
                while (jr.hasNext()) {
                    FloraOnOccurrence o = gson.fromJson(jr, FloraOnOccurrence.class);
                    occurrenceList.add(new SimpleOccurrence(o.latitude, o.longitude, o.ano, o.mes, o.dia, o.autor, o.genero
                            , o.especie, o.subespecie, o.notas, o.id_reg, o.id_ent, o.precisao, !o.duvida, o.floracao));

//                    occArray.add((FloraOnOccurrence) gson.fromJson(jr, FloraOnOccurrence.class));
//                    System.out.print(c+" ");
                    c++;
//                    System.out.flush();
                }
                jr.endArray();
            } else jr.skipValue();
        }
        jr.endObject();
        jr.close();

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
                occurrenceList.add(new SimpleOccurrence(o.latitude, o.longitude, o.ano, o.mes, o.dia, o.autor, o.genero
                        , o.especie, o.subespecie, o.notas, o.id_reg, o.id_ent, o.precisao, !o.duvida, o.floracao));
        }
        */
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

        InputStreamReader isr = new InputStreamReader(u.openStream());
        JsonObject resp = new JsonParser().parse(isr).getAsJsonObject();
        if (!resp.getAsJsonPrimitive("success").getAsBoolean()) {
            return Collections.emptyMap();
//            throw new FloraOnException(resp.getAsJsonPrimitive("msg").getAsString());
        }

        Type listType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> occArray;
        occArray = new Gson().fromJson(resp.getAsJsonObject("msg"), listType);
        System.out.println(resp.toString());
        for (String s : occArray.keySet())
            System.out.println(s);
        return occArray;
    }
}
