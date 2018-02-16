package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import pt.floraon.driver.datatypes.IntegerInterval;

public class NumericIntervalDeserializer implements VPackDeserializer<IntegerInterval> {
    @Override
    public IntegerInterval deserialize(
            final VPackSlice parent,
            final VPackSlice vpack,
            final VPackDeserializationContext context) throws VPackException {

        String v;
        if(vpack.getType() == ValueType.INT || vpack.getType() == ValueType.UINT || vpack.getType() == ValueType.SMALLINT)
            v = ((Integer) vpack.getAsInt()).toString();
        else if(vpack.getType() == ValueType.DOUBLE)
            throw new NumberFormatException("Can't handle non-integer numbers");
        else
            v = vpack.getAsString();

        return new IntegerInterval(v);
    }
}
