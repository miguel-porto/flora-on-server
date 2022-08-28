package pt.floraon.occurrences.entities;

import com.arangodb.velocypack.annotations.Expose;
import pt.floraon.driver.Constants;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.annotations.*;
import pt.floraon.occurrences.Abundance;
import pt.floraon.driver.entities.GeneralDBEdge;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.occurrences.fields.parsers.*;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.Serializable;
import java.util.*;

/**
 * See https://github.com/miguel-porto/flora-on-server/wiki/The-data-model#observed_in
 * Fields that don't have a @FieldParser annotation, cannot be updated in the GUI, or added in tables.
 * Created by miguel on 05-02-2017.
 */
public class OBSERVED_IN extends GeneralDBEdge implements Serializable, DiffableBean {
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(LatitudeLongitudeParser.class)
    @PrettyName(value = "Latitude da ocorrência", shortName = "Obs lat")
    private Float observationLatitude;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(LatitudeLongitudeParser.class)
    @PrettyName(value = "Longitude da ocorrência", shortName = "Obs long")
    private Float observationLongitude;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(EnumParser.class)
    @PrettyName(value = "Estado fenológico", shortName = "Fen", important = true)
    @EditWidget(value = EditWidget.Type.DROPDOWN,
            valuesSimple = {"FLOWER", "DISPERSION", "VEGETATIVE"},
            labelsSimple = {"Flower", "Dispersion", "Vegetative"},
            valuesAdvanced = {"FLOWER", "DISPERSION", "VEGETATIVE", "RESTING", "BUD", "FRUIT", "FLOWER_DISPERSION", "FLOWER_FRUIT"},
            labelsAdvanced = {"Flower", "Dispersion", "Vegetative", "Resting", "Flower buds", "Immature fruit", "Flower+Dispersion", "Flower+Fruit"})
    private Constants.PhenologicalStates phenoState;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(EnumParser.class)
    @PrettyName(value = "Espontaneidade", shortName = "Nat")
    @EditWidget(value = EditWidget.Type.DROPDOWN,
            valuesSimple = {"WILD", "CULTIVATED", "ESCAPED", "REINTRODUCTION", "TRANSLOCATION"},
            labelsSimple = {"Wild", "Cultivated", "Escaped", "Reintroduction", "Translocation"},
            valuesAdvanced = {"WILD", "CULTIVATED", "ESCAPED", "REINTRODUCTION", "TRANSLOCATION"},
            labelsAdvanced = {"Wild", "Cultivated", "Escaped", "Reintroduction", "Translocation"})
    private OccurrenceConstants.OccurrenceNaturalization naturalization;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(EnumParser.class)
    @EditWidget(value = EditWidget.Type.DROPDOWN,
            valuesSimple = {"CERTAIN", "ALMOST_SURE", "DOUBTFUL"},
            labelsSimple = {"Certain", "Almost sure", "Doubtful"},
            valuesAdvanced = {"CERTAIN", "ALMOST_SURE", "DOUBTFUL"},
            labelsAdvanced = {"Sure", "Almost sure", "Doubt"})
    @PrettyName(value = "Confiança ID", shortName = "Conf", important = true)
    private OccurrenceConstants.ConfidenceInIdentifiction confidence;
    @HideInCompactView @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Nome original", shortName = "Verb tax")
    private String verbTaxon;
    @HideInCompactView @FieldParser(GeneralFieldParser.class)
    @EditWidget(EditWidget.Type.BIGTEXT)
    @PrettyName(value = "Notas públicas do taxon", shortName = "Notas pub", important = true)
    private String comment;
    @HideInCompactView @FieldParser(GeneralFieldParser.class)
    @EditWidget(EditWidget.Type.BIGTEXT)
    @PrettyName(value = "Notas privadas do taxon", shortName = "Notas priv", alias="privateNote", important = true)
    private String privateComment;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "URL", shortName = "URL")
    private String uri;
    @FieldStyle(FieldStyle.Size.SMALL)
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Etiqueta herbário", shortName = "Etiq")
    private String labelData;
    @FieldStyle(FieldStyle.Size.SMALL)
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Código herbário", shortName = "Cód Herb", alias="codHerbario")
    private String accession;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Nº de indivíduos", shortName = "Nº", description = "Estimated number of individuals", important = true)
    private Abundance abundance;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(EnumParser.class)
    @PrettyName(value = "Método da estimativa", shortName = "Met", description = "Estimation method")
    private RedListEnums.TypeOfPopulationEstimate typeOfEstimate;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(FloatParser.class)
    @PrettyName(value = "Cobertura", shortName = "Cob", description = "Cover")
    private Float cover;
/*
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Escala de cobertura", shortName = "Escala", description = "Custom cover/abundance scale (e.g. Braun-Blanquet, etc.)")
    @Deprecated
    private String coverIndex;
*/
    private OccurrenceConstants.CoverType coverIndexScale;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(EnumParser.class)
    @PrettyName(value = "Tem foto", shortName = "Foto")
    private RedListEnums.HasPhoto hasPhoto;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(IntegerParser.class)
    @PrettyName(value = "Colheita", shortName = "Colh")
    private Integer hasSpecimen;
/*
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView
    @PrettyName(value = "Código Instituição", shortName = "Inst Cod")
*/
    private String institutionCode;
    private OccurrenceConstants.ValidationStatus validationStatus;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @ReadOnly @FieldParser(DateParser.class) @FieldType(FieldType.Type.DATE)
    @PrettyName(value = "Data de inserção", shortName = "Data ins")
    private Date dateInserted;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @ReadOnly @FieldType(FieldType.Type.DATE)
    @PrettyName(value = "Data da última actualização", shortName = "Data alt")
    private Date dateUpdated;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @FieldParser(UUIDParser.class) @ReadOnly
    @PrettyName(value = "Identificador único", shortName = "UUID", alias={"occurrenceuuid"})
    private UUID uuid;
    @FieldStyle(value = FieldStyle.Size.SMALL, monospaceFont = true)
    @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Código GPS", shortName = "GPS", alias={"gps", "gps code"}
        , description = "The GPS point name for this particular taxon. Usage discouraged: not to be confounded with the inventory code!")
    private String gpsCode;
    @FieldParser(GeneralFieldParser.class) @EditWidget(EditWidget.Type.BIGTEXT)
    @PrettyName(value = "Ameaças do taxon", shortName = "Ameaças esp")
    private String specificThreats;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @AdminOnly @FieldParser(EnumParser.class)
    @PrettyName(value = "Exclusion reason", shortName = "Excl", description = "Reason for excluding record from public maps", alias="excludeReason")
    private OccurrenceConstants.PresenceStatus presenceStatus;
    @FieldStyle(FieldStyle.Size.SMALL)
    @HideInCompactView @AdminOnly @FieldParser(GeneralFieldParser.class)
    @PrettyName(value = "Curator comment", shortName = "Curator", description = "Comments from the curator")
    private String curatorComment;
    @FieldType(FieldType.Type.IMAGE) @FieldParser(StringArrayParser.class)
    @PrettyName(value="Fotografias", shortName = "Fotos", important = true)
    private String[] images;
    private Boolean coordinatesChanged;
    /**
     * Field to hold the matched TaxEnt ID
     */
    @PrettyName(value = "TaxEnt match", shortName = "TaxEnt", description = "Internal ID of the taxon match")
    private String taxEntMatch;
    /**
     * Field to be populated, if needed, with the TaxEnt that is matched.
     */
    @Expose(serialize = false)
    private TaxEnt taxEnt;

    public Boolean getCoordinatesChanged() {
        return coordinatesChanged;
    }

    public void setCoordinatesChanged(Boolean coordinatesChanged) {
        this.coordinatesChanged = coordinatesChanged;
    }

    public OBSERVED_IN() {
    }

    public OBSERVED_IN(boolean createNew) {
        if(createNew) {
            dateInserted = new Date();
            uuid = UUID.randomUUID();
        }
    }

    public String getCuratorComment() {
        return curatorComment;
    }

    public void setCuratorComment(String curatorComment) {
        this.curatorComment = curatorComment;
    }

    public Constants.PhenologicalStates getPhenoState() {
        return phenoState;
    }

    public String _getPhenoStateLabel() {
        return phenoState == null ? "" : phenoState.getLabel();
    }

    public void setPhenoState(Constants.PhenologicalStates phenoState) {
        this.phenoState = phenoState;
    }

    public OccurrenceConstants.OccurrenceNaturalization getNaturalization() {
        return naturalization;
    }

    public void setNaturalization(OccurrenceConstants.OccurrenceNaturalization naturalization) {
        this.naturalization = naturalization;
    }

    public OccurrenceConstants.ConfidenceInIdentifiction getConfidence() {
        return confidence;
    }

    public String _getConfidenceLabel() {
        return confidence == null ? "" : confidence.getLabel();
    }

    public void setConfidence(OccurrenceConstants.ConfidenceInIdentifiction confidence) {
        this.confidence = confidence;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public String getVerbTaxon() {
        return verbTaxon;
    }

    public void setVerbTaxon(String verbTaxon) {
        this.verbTaxon = verbTaxon;
    }

    public String getComment() {
        return comment;
    }

    public String getUri() {
        return uri;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabelData() {
        return labelData;
    }

    public void setLabelData(String labelData) {
        this.labelData = labelData;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public Abundance getAbundance() {
        return abundance;
    }

    public void setAbundance(Abundance abundance) {
        this.abundance = abundance;
    }

    public void setAbundance(String abundance) {
        this.abundance = new Abundance(abundance);
    }

    public RedListEnums.TypeOfPopulationEstimate getTypeOfEstimate() {
        return typeOfEstimate;
    }

    public String _getTypeOfEstimateLabel() {
        return typeOfEstimate == null ? "" : typeOfEstimate.toString();//.getLabel();
    }

    public void setTypeOfEstimate(RedListEnums.TypeOfPopulationEstimate typeOfEstimate) {
        this.typeOfEstimate = typeOfEstimate;
    }

    public Float getCover() {
        return cover;
    }

    public void setCover(Float cover) {
        this.cover = cover;
    }

/*
    public String getCoverIndex() {
        return coverIndex;
    }

    public void setCoverIndex(String coverIndex) {
        this.coverIndex = coverIndex;
    }
*/

    public OccurrenceConstants.CoverType getCoverIndexScale() {
        return coverIndexScale;
    }

    public void setCoverIndexScale(OccurrenceConstants.CoverType coverIndexScale) {
        this.coverIndexScale = coverIndexScale;
    }

    public RedListEnums.HasPhoto getHasPhoto() {
        return hasPhoto;
    }

    public String _getHasPhotoLabel() {
        return hasPhoto == null ? "" : hasPhoto.getLabel();
    }

    public void setHasPhoto(RedListEnums.HasPhoto hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public Integer getHasSpecimen() {
        return hasSpecimen;
    }

    public void setHasSpecimen(Integer hasSpecimen) {
        this.hasSpecimen = hasSpecimen;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public OccurrenceConstants.ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(OccurrenceConstants.ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Date getDateInserted() {
        return dateInserted;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateInserted(Date dateInserted) {
        this.dateInserted = dateInserted;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getTaxEntMatch() {
        return taxEntMatch;
    }

    public void setTaxEntMatch(String taxEntMatch) {
        this.taxEntMatch = taxEntMatch;
    }

    /**
     * NOTE: this is not saved in the document, it must be fetched during the query.
     * @return
     */
    public TaxEnt getTaxEnt() {
        return this.taxEnt;
    }

    public void setTaxEnt(TaxEnt taxEnt) {
        this.taxEnt = taxEnt;
    }

    public UUID getUuid() {
        return uuid == null ? (uuid = UUID.randomUUID()) : uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Float getObservationLatitude() {
        return observationLatitude;
//        return (observationLatitude != null && Math.abs(observationLatitude - Constants.NODATA) < 0.00001) ? null : observationLatitude;
    }

    public void setObservationLatitude(Float latitude) {
        this.observationLatitude = latitude;
    }

    public Float getObservationLongitude() {
        return observationLongitude;
//        return (observationLongitude != null && Math.abs(observationLongitude - Constants.NODATA) < 0.00001) ? null : observationLongitude;
    }

    public void setObservationLongitude(Float longitude) {
        this.observationLongitude = longitude;
    }

    public String getGpsCode() {
        return gpsCode;
    }

    public void setGpsCode(String gpsCode) {
        this.gpsCode = gpsCode;
    }

    public String getPrivateComment() {
        return privateComment;
    }

    public void setPrivateComment(String privateComment) {
        this.privateComment = privateComment;
    }

    public String _getObservationCoordinates() {
        if (this.getObservationLatitude() == null || this.getObservationLongitude() == null)
            return "*";
        else
            return String.format(Locale.ROOT, "%.5f, %.5f", this.getObservationLatitude(), this.getObservationLongitude());
    }

    public String getSpecificThreats() {
        return specificThreats;
    }

    public void setSpecificThreats(String specificThreats) {
        this.specificThreats = specificThreats;
    }

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

    @Override
    public pt.floraon.driver.Constants.RelTypes getType() {
        return Constants.RelTypes.OBSERVED_IN;
    }

    public OccurrenceConstants.PresenceStatus getPresenceStatus() {
        return presenceStatus;
    }

    public String _getPresenceStatusLabel() {
        return presenceStatus == null ? "" : presenceStatus.getLabel();
    }

    public void setPresenceStatus(OccurrenceConstants.PresenceStatus presenceStatus) {
        this.presenceStatus = presenceStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OBSERVED_IN that = (OBSERVED_IN) o;
        return Objects.equals(observationLatitude, that.observationLatitude) &&
                Objects.equals(observationLongitude, that.observationLongitude) &&
                phenoState == that.phenoState &&
                naturalization == that.naturalization &&
                confidence == that.confidence &&
                Objects.equals(verbTaxon, that.verbTaxon) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(privateComment, that.privateComment) &&
                Objects.equals(labelData, that.labelData) &&
                Objects.equals(accession, that.accession) &&
                Objects.equals(abundance, that.abundance) &&
                typeOfEstimate == that.typeOfEstimate &&
                Objects.equals(cover, that.cover) &&
//                Objects.equals(coverIndex, that.coverIndex) &&
                coverIndexScale == that.coverIndexScale &&
                hasPhoto == that.hasPhoto &&
                Objects.equals(hasSpecimen, that.hasSpecimen) &&
                Objects.equals(institutionCode, that.institutionCode) &&
                validationStatus == that.validationStatus &&
                Objects.equals(gpsCode, that.gpsCode) &&
                Objects.equals(specificThreats, that.specificThreats) &&
                presenceStatus == that.presenceStatus &&
                Arrays.equals(images, that.images);
    }

    @Override
    public int hashCode() {

//        int result = Objects.hash(observationLatitude, observationLongitude, phenoState, naturalization, confidence, verbTaxon, comment, privateComment, labelData, accession, abundance, typeOfEstimate, cover, coverIndex, coverIndexScale, hasPhoto, hasSpecimen, institutionCode, validationStatus, gpsCode, specificThreats, presenceStatus);
        int result = Objects.hash(observationLatitude, observationLongitude, phenoState, naturalization, confidence, verbTaxon, comment, privateComment, labelData, accession, abundance, typeOfEstimate, cover, coverIndexScale, hasPhoto, hasSpecimen, institutionCode, validationStatus, gpsCode, specificThreats, presenceStatus);
        result = 31 * result + Arrays.hashCode(images);
        return result;
    }
}
