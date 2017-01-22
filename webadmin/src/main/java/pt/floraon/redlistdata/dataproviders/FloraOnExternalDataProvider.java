package pt.floraon.redlistdata.dataproviders;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
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
public class FloraOnExternalDataProvider implements ExternalDataProvider {
    final private URL floraOnURL;
    private List<SimpleOccurrence> occurrenceList;
    private IPolygonTheme clippingPolygon;
    private Integer minimumYear;

    @Override
    public int size() {
        if(clippingPolygon == null && minimumYear == null)
            return occurrenceList.size();
        else {
            int count = 0;
            boolean enter;
            for(SimpleOccurrence so : occurrenceList) {
                enter = !(minimumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() < minimumYear);

                if(clippingPolygon != null) {
                    boolean tmp2 = false;
                    for(Map.Entry<String, Polygon> po : clippingPolygon) {
                        if(po.getValue().contains(new Point2D(so.getLongitude(), so.getLatitude()))) {
                            tmp2 = true;
                            break;
                        }
                    }
                    enter &= tmp2;
                }

                if(enter) count++;
            }
            return count;
        }
    }

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

        Type listType = new TypeToken<List<FloraOnOccurrence>>() {}.getType();
        List<FloraOnOccurrence> occArray;

        InputStreamReader isr = new InputStreamReader(u.openStream());
        JsonElement resp = new JsonParser().parse(isr);
        if(!resp.getAsJsonObject().getAsJsonPrimitive("success").getAsBoolean()) {
            throw new FloraOnException(resp.getAsJsonObject().getAsJsonPrimitive("msg").getAsString());
        }
        occArray = new Gson().fromJson(resp.getAsJsonObject().getAsJsonArray("msg"), listType);

        occurrenceList = new ArrayList<>();
        for(FloraOnOccurrence o : occArray) {
            if(!o.duvida) occurrenceList.add(new SimpleOccurrence(o.latitude, o.longitude, o.ano, o.mes, o.dia, o.autor, o.genero
                    , o.especie, o.subespecie, o.notas, o.id_reg, o.id_ent, o.precisao, !o.duvida, o.floracao));
        }
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
        if(!resp.getAsJsonPrimitive("success").getAsBoolean()) {
            return Collections.emptyMap();
//            throw new FloraOnException(resp.getAsJsonPrimitive("msg").getAsString());
        }

        Type listType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> occArray;
        occArray = new Gson().fromJson(resp.getAsJsonObject("msg"), listType);
        System.out.println(resp.toString());
        for(String s : occArray.keySet())
            System.out.println(s);
        return occArray;
    }

    @Override
    public Iterator<SimpleOccurrence> iterator() {
        if(clippingPolygon == null && minimumYear == null)
            return occurrenceList.iterator();
        else {
            List<SimpleOccurrence> out = new ArrayList<>();
            boolean enter;
            for(SimpleOccurrence so : occurrenceList) {
                enter = true;
                if(minimumYear != null && so.getYear() != null && so.getYear() != 0 && so.getYear() < minimumYear) enter = false;

                if(clippingPolygon != null) {
                    boolean tmp2 = false;
                    for(Map.Entry<String, Polygon> po : clippingPolygon) {
                        if(po.getValue().contains(new Point2D(so.getLongitude(), so.getLatitude()))) {
                            tmp2 = true;
                            break;
                        }
                    }
                    enter &= tmp2;
                }

                if(enter) out.add(so);
            }
            return out.iterator();
        }
    }

    @Override
    public void setMinimumYear(Integer minimumYear) {
        this.minimumYear = minimumYear;
    }

    @Override
    public void setClippingPolygon(IPolygonTheme theme) {
        clippingPolygon = theme;
    }
}
