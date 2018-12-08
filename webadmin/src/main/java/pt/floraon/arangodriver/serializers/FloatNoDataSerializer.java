package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSerializationContext;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.exception.VPackException;
import pt.floraon.driver.Constants;

/**
 * A serializer that converts no data values to null, so effectively erasing them.
 */
public class FloatNoDataSerializer implements VPackSerializer<Float> {
    @Override
    public void serialize(
            final VPackBuilder builder,
            final String attribute,
            final Float value,
            final VPackSerializationContext context) throws VPackException {

        if(Constants.isNoData(value))
            builder.add(attribute, (Float) null);
        else
            builder.add(attribute, value);
    }
}
