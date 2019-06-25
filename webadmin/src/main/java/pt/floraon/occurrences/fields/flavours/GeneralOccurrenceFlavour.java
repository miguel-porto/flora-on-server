package pt.floraon.occurrences.fields.flavours;

import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.fields.FieldReflection;

/**
 * This is basically a class to expose static methods to JSTL.
 */
public abstract class GeneralOccurrenceFlavour implements IOccurrenceFlavour {

    @Override
    public String getFieldName(String field) {
        return FieldReflection.getFieldName(field);
    }

    @Override
    public String getFieldValue(OBSERVED_IN occurrence, Inventory inventory, String field) {
        return FieldReflection.getFieldValue(occurrence, inventory, field);
    }

    @Override
    public Object getFieldValueRaw(OBSERVED_IN occurrence, Inventory inventory, String field) {
        return FieldReflection.getFieldValueRaw(occurrence, inventory, field);
    }

    @Override
    public String getFieldShortName(String field) {
        return FieldReflection.getFieldShortName(field);
    }

    @Override
    public boolean hideFieldInCompactView(String field) {
        return FieldReflection.hideFieldInCompactView(field);
    }


    @Override
    public boolean isInventoryField(String field) {
        return FieldReflection.isInventoryField(field);
    }

    @Override
    public boolean isReadOnly(String field) {
        return FieldReflection.isReadOnly(field);
    }

    @Override
    public boolean isSmallField(String field) {
        return FieldReflection.isSmallField(field);
    }

    @Override
    public boolean isImageField(String field) {
        return FieldReflection.isImageField(field);
    }

}
