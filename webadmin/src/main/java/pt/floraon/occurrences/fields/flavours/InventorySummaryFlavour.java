package pt.floraon.occurrences.fields.flavours;

public class InventorySummaryFlavour extends GeneralOccurrenceFlavour implements IOccurrenceFlavour {
    @Override
    public String[] getFields() {
        return new String[]{
                "code", "locality", "date", "inventoryCoordinates", "taxaSummary"
        };
    }

    @Override
    public boolean showInOccurrenceView() {
        return false;
    }

    @Override
    public boolean showInInventoryView() {
        return false;
    }

    @Override
    public String getName() {
        return "Resumo inventários";
    }

    @Override
    public boolean containsCoordinates() {
        return true;
    }

    @Override
    public boolean containsInventoryFields() {
        return true;
    }

}
