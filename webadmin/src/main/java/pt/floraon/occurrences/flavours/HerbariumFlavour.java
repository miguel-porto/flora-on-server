package pt.floraon.occurrences.flavours;

public class HerbariumFlavour extends GeneralOccurrenceFlavour implements IOccurrenceFlavour {
    @Override
    public String[] getFields() {
        return new String[] {
                "accession", "taxa", "presenceStatus", "coordinates", "precision", "verbLocality", "date"
                , "collectors", "labelData", "privateComment"};
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
        return "Herbário";
    }
}
