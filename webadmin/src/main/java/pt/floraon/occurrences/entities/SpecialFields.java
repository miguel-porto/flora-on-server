package pt.floraon.occurrences.entities;

import pt.floraon.driver.annotations.*;
import pt.floraon.occurrences.fields.parsers.DateParser;
import pt.floraon.occurrences.fields.parsers.LatitudeLongitudeParser;
import pt.floraon.occurrences.fields.parsers.TaxaParser;

/**
 * This is a dummy class, just serving as placeholder for special Inventory and OBSERVED_IN fields.
 */
public class SpecialFields {
    @SpecialField @FieldParser(TaxaParser.class)
    @PrettyName(value = "Taxon", shortName = "Taxon", important = true)
    private String taxa;
    @SpecialField @InventoryField @FieldParser(LatitudeLongitudeParser.class) @FieldType(FieldType.Type.COORDINATES)
    @PrettyName(value = "Coordenadas", shortName = "Coord", important = true)
    private String coordinates;
    @SpecialField @InventoryField @FieldParser(DateParser.class) @FieldType(FieldType.Type.DATE)
    @PrettyName(value = "Data de observação", shortName = "Data", important = true)
    private String date;
    @SpecialField(hideFromCustomFlavour = true) @InventoryField @FieldParser(LatitudeLongitudeParser.class) @FieldType(FieldType.Type.COORDINATES)
    @PrettyName(value = "Coordenadas do inventário", shortName = "Coord inv")
    private String inventoryCoordinates;

    @SpecialField(hideFromCustomFlavour = true) @InventoryField @ReadOnly
    @PrettyName(value = "Taxon summary", shortName = "Taxa")
    private String taxaSummary;

    @SpecialField @ReadOnly
    @PrettyName(value = "GPS/Código herbário", shortName = "Code+Acc")
    private String gpsCode_accession;
    @SpecialField @ReadOnly
    @PrettyName(value = "Local/Verb Local", shortName = "Local")
    private String locality_verbLocality;
    @SpecialField @ReadOnly
    @PrettyName(value = "Observadores/Colectores", shortName = "Auth")
    private String observers_collectors;
    @SpecialField @ReadOnly
    @PrettyName(value = "Notas/Etiqueta", shortName = "Notas")
    private String comment_labelData;

    public static String getGpsCode_accession(Inventory inventory, OBSERVED_IN occurrence) {
        return String.format("%s %s"
                , inventory == null ? "" : (inventory.getCode() == null ? "" : inventory.getCode())
                , occurrence == null ? "" : (occurrence.getAccession() == null ? "" : occurrence.getAccession())
        );
    }

    public static String getLocality_verbLocality(Inventory inventory, OBSERVED_IN occurrence) {
        return String.format("%s %s"
                , inventory == null ? "" : (inventory.getVerbLocality() == null ? "" : inventory.getVerbLocality())
                , inventory == null ? "" : (inventory.getLocality() == null ? "" : inventory.getLocality())
        );
    }

    public static String getComment_labelData(Inventory inventory, OBSERVED_IN occurrence) {
        return String.format("%s %s"
                , occurrence == null ? "" : (occurrence.getComment() == null ? "" : occurrence.getComment())
                , occurrence == null ? "" : (occurrence.getLabelData() == null ? "" : occurrence.getLabelData())
        );
    }

}
