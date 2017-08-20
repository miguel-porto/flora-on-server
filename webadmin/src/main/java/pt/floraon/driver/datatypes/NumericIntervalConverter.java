package pt.floraon.driver.datatypes;

import org.apache.commons.beanutils.converters.AbstractConverter;

public class NumericIntervalConverter extends AbstractConverter {
    public NumericIntervalConverter() {
    }

    public NumericIntervalConverter(Object defaultValue) {
        super(defaultValue);
    }

    protected Class<?> getDefaultType() {
        return NumericInterval.class;
    }

    protected <T> T convertToType(Class<T> type, Object value) throws Throwable {
//        System.out.println(type.toString());
//        System.out.println(value.getClass().toString());
        if (!NumericInterval.class.equals(type) && !Object.class.equals(type)) {
            throw this.conversionException(type, value);
        } else {
//            System.out.println(value.toString());
            return type.cast(new NumericInterval(value.toString()));
//            return type.cast(value.toString());
        }
    }
}
