package pt.floraon.occurrences.fields.flavours;

public class CuratorFlavour extends GeneralOccurrenceFlavour implements IOccurrenceFlavour {
    @Override
    public String[] getFields() {
        return new String[] {
                "taxa", "coordinates", "date", "precision", "observers", "privateComment", "uri", "curatorComment", "presenceStatus"};
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
        return "Curation";
    }

    @Override
    public boolean containsCoordinates() {
        return true;
    }

    @Override
    public boolean containsInventoryFields() {
        return false;
    }
}
