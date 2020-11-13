package pt.floraon.occurrences.fields.flavours;

public class SimpleFlavour extends GeneralOccurrenceFlavour implements IOccurrenceFlavour {
    @Override
    public String[] getFields() {
        return new String[]{
                "taxa", "confidence", "coordinates", "precision", "comment", "privateComment", "date", "phenoState",
                "observers", "naturalization"
        };
    }

    @Override
    public boolean showInOccurrenceView() {
        return true;
    }

    @Override
    public boolean showInInventoryView() {
        return false;
    }

    @Override
    public String getName() {
        return "Simples";
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
