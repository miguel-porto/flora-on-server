package pt.floraon.occurrences.flavours;

public class ManagementFlavour extends GeneralOccurrenceFlavour implements IOccurrenceFlavour {
    @Override
    public String[] getFields() {
        return new String[] {
                "gpsCode_accession", "coordinates", "precision", "taxa", "confidence", "date", "locality_verbLocality"
                , "presenceStatus", "observers_collectors", "comment_labelData", "privateComment", "abundance"
                , "typeOfEstimate", "hasPhoto", "hasSpecimen", "specificThreats", "phenoState"};
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
        return "Gestão";
    }
}
