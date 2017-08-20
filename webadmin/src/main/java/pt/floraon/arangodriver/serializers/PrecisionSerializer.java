package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSerializationContext;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.exception.VPackException;
import pt.floraon.geometry.Precision;

public class PrecisionSerializer implements VPackSerializer<Precision> {
    @Override
    public void serialize(
            final VPackBuilder builder,
            final String attribute,
            final Precision value,
            final VPackSerializationContext context) throws VPackException {
//						builder.add(attribute, ValueType.STRING);
        builder.add(attribute, value.toString());
//						builder.close();
    }
}
