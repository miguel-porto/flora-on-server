package pt.floraon.occurrences;

public interface OccurrenceFlavour {
    String[] getFields();
    boolean showInOccurrenceView();
    boolean showInInventoryView();
    String getName();
}
