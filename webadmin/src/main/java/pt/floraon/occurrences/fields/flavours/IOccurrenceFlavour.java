package pt.floraon.occurrences.fields.flavours;

import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;

public interface IOccurrenceFlavour {
    String[] getFields();
    boolean showInOccurrenceView();
    boolean showInInventoryView();
    String getName();
    String getFieldName(String field);

    /**
     * A reflection-based getter, just for use in JSTL
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
