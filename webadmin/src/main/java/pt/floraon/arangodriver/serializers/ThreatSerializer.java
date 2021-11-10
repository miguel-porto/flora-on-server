package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSerializationContext;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.exception.VPackException;
import pt.floraon.redlistdata.threats.Threat;

public class ThreatSerializer implements VPackSerializer<Threat> {
    @Override
    public void serialize(
            final VPackBuilder builder,
            final String attribute,
            final Threat value,
            final VPackSerializationContext context) throws VPackException {

        builder.add(attribute, value.toString());
    }
}
