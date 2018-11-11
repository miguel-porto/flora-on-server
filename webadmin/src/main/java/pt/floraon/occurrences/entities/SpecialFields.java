package pt.floraon.occurrences.entities;

import pt.floraon.driver.annotations.HideInInventoryView;
import pt.floraon.driver.annotations.PrettyName;

/**
 * This is a dummy class, just serving as placeholder for special Inventory and OBSERVED_IN fields.
 */
public class SpecialFields {
    @PrettyName(value = "Taxon", shortName = "Taxon")
    private String taxa;
    @HideInInventoryView
    @PrettyName(value = "Data de observação", shortName = "Data")
    private String date;
    @HideInInventoryView
    @PrettyName(value = "Coordenadas", shortName = "Coord")
    private String coordinates;
    @PrettyName(value = "GPS/Código herbário", shortName = "Code+Acc")
    private String gpsCode_accession;
    @PrettyName(value = "Local/Verb Local", shortName = "Local")
    private String locality_verbLocality;
    @PrettyName(value = "Observadores/Colectores", shortName = "Auth")
    private String observers_collectors;
    @PrettyName(value = "Notas/Etiqueta", shortName = "Notas")
    private String comment_labelData;
}
