package pt.floraon.arangodriver;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import jline.internal.Log;

/**
 * Created by miguel on 15-12-2016.
 */
public class SafeEnumDeserializer<T extends Enum> implements VPackDeserializer<T> {
    final Class<T> tClass;
    public SafeEnumDeserializer(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public T deserialize(VPackSlice parent, VPackSlice vpack, VPackDeserializationContext vPackDeserializationContext) throws VPackException {
        String tmp = vpack.getAsString();
        if(tmp.equals("")) return null;
        try {
            return (T) T.valueOf(tClass, tmp);
        } catch (IllegalArgumentException e) {
            Log.warn("Value " + tmp + " not found in enum constant.");
            return null;
        }

    }
}
