package pt.floraon.driver.utils;

import org.apache.commons.beanutils.converters.AbstractConverter;
import pt.floraon.driver.SafeHTMLString;

public final class SafeHTMLStringConverter extends AbstractConverter {
    public SafeHTMLStringConverter() {
    }

    public SafeHTMLStringConverter(Object defaultValue) {
        super(defaultValue);
    }

    protected Class<?> getDefaultType() {
        return SafeHTMLString.class;
    }

    protected <T> T convertToType(Class<T> type, Object value) throws Throwable {
        System.out.println(type.toString());
        System.out.println(value.getClass().toString());
        if (!SafeHTMLString.class.equals(type) && !Object.class.equals(type)) {
            throw this.conversionException(type, value);
        } else {
//            System.out.println(value.toString());
            return type.cast(new SafeHTMLString(value.toString()));
//            return type.cast(value.toString());
        }
    }
}

