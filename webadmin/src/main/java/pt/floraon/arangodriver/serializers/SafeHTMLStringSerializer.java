package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSerializationContext;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.exception.VPackException;
import pt.floraon.driver.datatypes.SafeHTMLString;

public class SafeHTMLStringSerializer implements VPackSerializer<SafeHTMLString> {
    @Override
    public void serialize(
            final VPackBuilder builder,
            final String attribute,
            final SafeHTMLString value,
            final VPackSerializationContext context) throws VPackException {

        builder.add(attribute, value.toString());
    }
}
