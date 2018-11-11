package pt.floraon.occurrences.flavours;

public class InventoryFlavour extends GeneralOccurrenceFlavour implements IOccurrenceFlavour {
    @Override
    public String[] getFields() {
        return new String[] {
                "taxa", "confidence", "phenoState", "abundance", "coverIndex", "comment", "privateComment"};
    }

    @Override
    public boolean showInOccurrenceView() {
        return false;
    }

    @Override
    public boolean showInInventoryView() {
        return true;
    }

    @Override
    public String getName() {
        return "Inventário";
    }
}
