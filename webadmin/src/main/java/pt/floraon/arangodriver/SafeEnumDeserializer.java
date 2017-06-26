package pt.floraon.arangodriver;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import jline.internal.Log;

/**
 * Created by miguel on 15-12-2016.
 */
public class SafeEnumDeserializer<T extends Enum> implements VPackDeserializer<T> {
    private final Class<T> tClass;
    private final T nullValue;

    public SafeEnumDeserializer(Class<T> tClass) {
        this.tClass = tClass;
        this.nullValue = null;
    }

    public SafeEnumDeserializer(Class<T> tClass, T nullValue) {
        this.tClass = tClass;
        this.nullValue = nullValue;
    }

    @Override
    public T deserialize(VPackSlice parent, VPackSlice vpack, VPackDeserializationContext vPackDeserializationContext) throws VPackException {
        String v;
        if(vpack.getType() == ValueType.INT || vpack.getType() == ValueType.UINT || vpack.getType() == ValueType.SMALLINT)
            v = ((Integer) vpack.getAsInt()).toString();
        else if(vpack.getType() == ValueType.DOUBLE)
            v = ((Double) vpack.getAsDouble()).toString();
        else if(vpack.getType() == ValueType.BOOL)
            v = ((Boolean) vpack.getAsBoolean()).toString().toUpperCase();
        else
            v = vpack.getAsString();

        if(v.equals("")) return nullValue;
        try {
            return (T) T.valueOf(tClass, v);
        } catch (IllegalArgumentException e) {
            Log.warn("Value " + v + " not found in enum constant.");
            return nullValue;
        }

    }
}
