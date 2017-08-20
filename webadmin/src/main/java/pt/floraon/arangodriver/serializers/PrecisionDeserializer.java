package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;
import pt.floraon.driver.FloraOnException;
import pt.floraon.geometry.Precision;

public class PrecisionDeserializer implements VPackDeserializer<Precision> {
    @Override
    public Precision deserialize(
            final VPackSlice parent,
            final VPackSlice vpack,
            final VPackDeserializationContext context) throws VPackException {

        final Precision obj;
        String v;
        if(vpack.getType() == ValueType.INT || vpack.getType() == ValueType.UINT || vpack.getType() == ValueType.SMALLINT)
            v = ((Integer) vpack.getAsInt()).toString();
        else if(vpack.getType() == ValueType.DOUBLE)
            v = ((Double) vpack.getAsDouble()).toString();
        else
            v = vpack.getAsString();

        try {
            obj = new Precision(v);
        } catch (FloraOnException e) {
            throw new VPackParserException(e);
        }
        return obj;
    }
}
