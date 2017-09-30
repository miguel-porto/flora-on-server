package pt.floraon.driver.datatypes;

import org.apache.commons.beanutils.converters.AbstractConverter;
import pt.floraon.occurrences.Abundance;

public class AbundanceConverter extends AbstractConverter {
    public AbundanceConverter() {
    }

    public AbundanceConverter(Object defaultValue) {
        super(defaultValue);
    }

    protected Class<?> getDefaultType() {
        return NumericInterval.class;
    }

    protected <T> T convertToType(Class<T> type, Object value) throws Throwable {
//        System.out.println(type.toString());
//        System.out.println(value.getClass().toString());
        if (!Abundance.class.equals(type) && !Object.class.equals(type)) {
            throw this.conversionException(type, value);
        } else {
//            System.out.println(value.toString());
            return type.cast(new Abundance(value.toString()));
//            return type.cast(value.toString());
        }
    }
}

