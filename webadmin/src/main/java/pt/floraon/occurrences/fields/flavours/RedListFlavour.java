package pt.floraon.occurrences.fields.flavours;

public class RedListFlavour extends GeneralOccurrenceFlavour implements IOccurrenceFlavour {
    @Override
    public String[] getFields() {
        return new String[] {
                "date", "observers", "coordinates", "locality", "precision", "gpsCode", "taxa", "presenceStatus"
                , "confidence", "phenoState", "abundance", "typeOfEstimate", "hasPhoto", "hasSpecimen", "specificThreats"
                , "comment", "privateComment"};
    }

    @Override
    public boolean showInOccurrenceView() {
        return true;
    }

    @Override
    public boolean showInInventoryView() {
        return true;
    }

    @Override
    public String getName() {
        return "Red List";
    }

    @Override
    public boolean containsCoordinates() {
        return true;
    }

}
