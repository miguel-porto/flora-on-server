package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import jline.internal.Log;
import pt.floraon.redlistdata.threats.Threat;
import pt.floraon.redlistdata.threats.MultipleChoiceEnumerationBase;

public class ThreatDeserializer implements VPackDeserializer<Threat> {
    @Override
    public Threat deserialize(
            final VPackSlice parent,
            final VPackSlice vpack,
            final VPackDeserializationContext context) throws VPackException {

        final Threat obj;
        String v;
        if(vpack.getType() == ValueType.INT || vpack.getType() == ValueType.UINT || vpack.getType() == ValueType.SMALLINT
                || vpack.getType() == ValueType.DOUBLE)
            throw new NumberFormatException ("Invalid value for threat");
        else
            v = vpack.getAsString();

        obj = MultipleChoiceEnumerationBase.valueOf(v);
        if(obj == null)
            Log.warn("Value " + v + " not found in threat codes.");
        return obj;
    }
}
