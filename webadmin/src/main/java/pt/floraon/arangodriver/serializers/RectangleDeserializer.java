package pt.floraon.arangodriver.serializers;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackBuilderException;
import com.arangodb.velocypack.exception.VPackException;
import pt.floraon.driver.datatypes.Rectangle;

public class RectangleDeserializer implements VPackDeserializer<Rectangle> {
    @Override
    public Rectangle deserialize(
            final VPackSlice parent,
            final VPackSlice vpack,
            final VPackDeserializationContext context) throws VPackException {

        final Rectangle obj;
        if(vpack.isObject()) {
            obj = new Rectangle(vpack.get("left").getAsLong(), vpack.get("right").getAsLong()
            , vpack.get("top").getAsLong(), vpack.get("bottom").getAsLong());
        } else
            throw new VPackBuilderException("Expecting Rectangle object");
        return obj;
    }
}
