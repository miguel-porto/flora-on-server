package pt.floraon.redlistdata.occurrenceproviders;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import pt.floraon.driver.FloraOnException;
import pt.floraon.redlistdata.OccurrenceProvider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A class to fetch records from flora-on.pt
 * Created by miguel on 02-11-2016.
 */
public class FloraOnOccurrenceProvider implements OccurrenceProvider {
    final private URL floraOnURL;
    private List<SimpleOccurrence> occurrenceList;

    @Override
    public int size() {
        return occurrenceList.size();
    }

    private class FloraOnOccurrence {
        String autor, genero, especie, subespecie, notas;
        int id_reg, id_ent, ano, mes, dia, precisao;
        float latitude, longitude;
        boolean duvida;
        Boolean floracao;
    }

    public FloraOnOccurrenceProvider(URL url) {
        this.floraOnURL = url;
    }

    @Override
    public void executeOccurrenceQuery(Object query) throws FloraOnException, URISyntaxException, IOException {
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
            newQuery = "id=" + legacyID;
        } else {
            newQuery += "&id=" + legacyID;
        }

        URI newUri;
        URL u;
        newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery, oldUri.getFragment());
        u = newUri.toURL();

        Type listType = new TypeToken<List<FloraOnOccurrence>>() {}.getType();
        List<FloraOnOccurrence> occArray;

        InputStreamReader isr = new InputStreamReader(u.openStream());
        JsonElement resp = new JsonParser().parse(isr);
        occArray = new Gson().fromJson(resp.getAsJsonObject().getAsJsonArray("msg"), listType);

        occurrenceList = new ArrayList<>();
        for(FloraOnOccurrence o : occArray) {
            if(!o.duvida) occurrenceList.add(new SimpleOccurrence(o.latitude, o.longitude, o.ano, o.mes, o.dia, o.autor, o.genero
                    , o.especie, o.subespecie, o.notas, o.id_reg, o.id_ent, o.precisao, !o.duvida, o.floracao));
        }
    }

    @Override
    public Iterator<SimpleOccurrence> iterator() {
        return occurrenceList.iterator();
    }
}
