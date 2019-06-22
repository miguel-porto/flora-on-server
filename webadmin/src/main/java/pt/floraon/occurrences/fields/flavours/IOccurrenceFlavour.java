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

    String getFieldShortName(String field);

    boolean hideFieldInCompactView(String field);

    boolean isInventoryField(String field);

    boolean isReadOnly(String field);

    boolean isSmallField(String field);
}
