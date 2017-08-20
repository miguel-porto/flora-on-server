package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import pt.floraon.driver.datatypes.SafeHTMLString;

public class SafeHTMLStringDeserializer implements VPackDeserializer<SafeHTMLString> {
    @Override
    public SafeHTMLString deserialize(
            final VPackSlice parent,
            final VPackSlice vpack,
            final VPackDeserializationContext context) throws VPackException {

        final SafeHTMLString obj;
        String v;
        if(vpack.getType() == ValueType.INT || vpack.getType() == ValueType.UINT || vpack.getType() == ValueType.SMALLINT)
            v = ((Integer) vpack.getAsInt()).toString();
        else if(vpack.getType() == ValueType.DOUBLE)
            v = ((Double) vpack.getAsDouble()).toString();
        else
            v = vpack.getAsString();
        obj = new SafeHTMLString(v);
        return obj;
    }
}
