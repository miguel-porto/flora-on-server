package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import jline.internal.Log;
import pt.floraon.redlistdata.threats.ConservationAction;
import pt.floraon.redlistdata.threats.MultipleChoiceEnumerationConservationActions;
import pt.floraon.redlistdata.threats.MultipleChoiceEnumerationThreats;
import pt.floraon.redlistdata.threats.Threat;

public class ConservationActionDeserializer implements VPackDeserializer<ConservationAction> {
    @Override
    public ConservationAction deserialize(
            final VPackSlice parent,
            final VPackSlice vpack,
            final VPackDeserializationContext context) throws VPackException {

        final ConservationAction obj;
        String v;
        if(vpack.getType() == ValueType.INT || vpack.getType() == ValueType.UINT || vpack.getType() == ValueType.SMALLINT
                || vpack.getType() == ValueType.DOUBLE)
            throw new NumberFormatException ("Invalid value for conservation action");
        else
            v = vpack.getAsString();

        obj = MultipleChoiceEnumerationConservationActions.valueOf(v);
        if(obj == null)
            Log.warn("Value " + v + " not found in conservation action codes.");
        return obj;
    }
}
