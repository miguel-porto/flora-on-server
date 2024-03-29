package pt.floraon.occurrences.fields.flavours;

import pt.floraon.driver.annotations.EditWidget;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.fields.FieldReflection;

import java.lang.reflect.Field;

/**
 * This is basically a class to expose static methods to JSTL.
 */
public abstract class GeneralOccurrenceFlavour implements IOccurrenceFlavour {
    @Override
    public String[] getFieldValues(String field, boolean advanced) {
        return FieldReflection.getFieldValues(field, advanced);
    }

    @Override
    public String[] getFieldLabels(String field, boolean advanced) {
        return FieldReflection.getFieldLabels(field, advanced);
    }

    @Override
    public EditWidget.Type getFieldWidget(String field, boolean advanced) {
        return FieldReflection.getFieldWidget(field, advanced);
    }

    @Override
    public String getFieldName(String field, boolean advanced) {
        return FieldReflection.getFieldName(field, advanced);
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
    public String getFieldShortName(String field, boolean advanced) {
        return FieldReflection.getFieldShortName(field, advanced);
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
    public boolean isReadOnly(String field, boolean isAdmin) {
        return FieldReflection.isReadOnly(field, isAdmin);
    }

    @Override
    public String getFieldSize(String field) {
        return FieldReflection.getFieldSize(field);
    }

    @Override
    public boolean isImportantField(String field) {
        return FieldReflection.isImportantField(field);
    }

    @Override
    public boolean isDateField(String field) {
        return FieldReflection.isDateField(field);
    }

    @Override
    public boolean isBooleanField(String field) {
        return FieldReflection.isBooleanField(field);
    }

    @Override
    public boolean isMonospaceFont(String field) {
        return FieldReflection.isMonospaceFont(field);
    }

    @Override
    public boolean breakLines(String field) {
        return FieldReflection.breakLines(field);
    }

    @Override
    public boolean isBigEditWidget(String field) {
        return FieldReflection.isBigEditWidget(field);
    }

    @Override
    public boolean isImageField(String field) {
        return FieldReflection.isImageField(field);
    }

    @Override
    public boolean isAuthorField(String field) {
        return FieldReflection.isAuthorField(field);
    }

    @Override
    public boolean isAdminField(String field) {
        return FieldReflection.isAdminField(field);
    }

}
