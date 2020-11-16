package pt.floraon.occurrences.fields.flavours;

import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

public interface IOccurrenceFlavour {
    /**
     * @return The fields that are included in this flavour.
     */
    String[] getFields();
    boolean showInOccurrenceView();
    boolean showInInventoryView();
    /**
     * @return The flavour name
     */
    String getName();
    String getFieldName(String field);
    boolean containsCoordinates();

    /**
     * A reflection-based getter, just for use in JSTL, which converts any field value
     * to a human-readable string
     * @param occurrence
     * @param field
     * @return
     */
    String getFieldValue(OBSERVED_IN occurrence, Inventory inventory, String field);

    /**
     * A reflection-based getter, for getting any field in its original type
     * @param occurrence
     * @param inventory
     * @param field
     * @return The field value in its original type.
     */
    Object getFieldValueRaw(OBSERVED_IN occurrence, Inventory inventory, String field);

    String getFieldShortName(String field);

    boolean hideFieldInCompactView(String field);

    boolean isInventoryField(String field);

    boolean isReadOnly(String field);

    boolean isSmallField(String field);

    boolean isImportantField(String field);

    boolean isDateField(String field);

    boolean isMonospaceFont(String field);

    boolean isImageField(String field);

    boolean isAuthorField(String field);

    boolean isAdminField(String field);

    boolean containsInventoryFields();
}
