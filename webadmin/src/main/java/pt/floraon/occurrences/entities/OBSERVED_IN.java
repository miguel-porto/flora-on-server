package pt.floraon.occurrences.entities;

import com.arangodb.velocypack.annotations.Expose;
import pt.floraon.driver.Constants;
import pt.floraon.driver.DiffableBean;
import pt.floraon.driver.annotations.HideInCompactView;
import pt.floraon.driver.annotations.PrettyName;
import pt.floraon.driver.annotations.ReadOnly;
import pt.floraon.driver.annotations.SmallField;
import pt.floraon.occurrences.Abundance;
import pt.floraon.driver.entities.GeneralDBEdge;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.redlistdata.RedListEnums;
import pt.floraon.taxonomy.entities.TaxEnt;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * See https://github.com/miguel-porto/flora-on-server/wiki/The-data-model#observed_in
 * Created by miguel on 05-02-2017.
 */
public class OBSERVED_IN extends GeneralDBEdge implements Serializable, DiffableBean {
    @SmallField @HideInCompactView
    @PrettyName(value = "Latitude da ocorrência", shortName = "Obs lat")
    private Float observationLatitude;
    @SmallField @HideInCompactView
    @PrettyName(value = "Longitude da ocorrência", shortName = "Obs long")
    private Float observationLongitude;
    @SmallField @HideInCompactView
    @PrettyName(value = "Estado fenológico", shortName = "Fen")
    private Constants.PhenologicalStates phenoState;
    @HideInCompactView @ReadOnly
    @PrettyName(value = "Espontaneidade", shortName = "Nat")
    private OccurrenceConstants.OccurrenceNaturalization naturalization;
    @SmallField @HideInCompactView
    @PrettyName(value = "Confiança ID", shortName = "Conf")
    private OccurrenceConstants.ConfidenceInIdentifiction confidence;
    @HideInCompactView
    @PrettyName(value = "Nome original", shortName = "Verb tax")
    private String verbTaxon;
    @HideInCompactView
    @PrettyName(value = "Notas públicas", shortName = "Notas pub")
    private String comment;
    @HideInCompactView
    @PrettyName(value = "Notas privadas", shortName = "Notas priv")
    private String privateComment;
    @SmallField
    @PrettyName(value = "Etiqueta herbário", shortName = "Etiq")
    private String labelData;
    @SmallField
    @PrettyName(value = "Código herbário", shortName = "Cód Herb")
    private String accession;
    @SmallField @HideInCompactView
    @PrettyName(value = "Nº indivíduos", shortName = "Nº", description = "Estimated number of individuals")
    private Abundance abundance;
    @SmallField @HideInCompactView
    @PrettyName(value = "Método da estimativa", shortName = "Met", description = "Estimation method")
    private RedListEnums.TypeOfPopulationEstimate typeOfEstimate;
    @SmallField @HideInCompactView
    @PrettyName(value = "Cobertura", shortName = "Cob", description = "Cover")
    private Float cover;
    @SmallField @HideInCompactView
    @PrettyName(value = "Escala de cobertura", shortName = "Cob", description = "Custom cover/abundance scale (e.g. Braun-Blanquet, etc.)")
    private String coverIndex;
    private OccurrenceConstants.CoverType coverIndexScale;
    @SmallField @HideInCompactView
    @PrettyName(value = "Foto", shortName = "Foto")
    private RedListEnums.HasPhoto hasPhoto;
    @SmallField @HideInCompactView
    @PrettyName(value = "Colheita", shortName = "Colh")
    private Integer hasSpecimen;
/*
    @SmallField @HideInCompactView
    @PrettyName(value = "Código Instituição", shortName = "Inst Cod")
*/
    private String institutionCode;
    private OccurrenceConstants.ValidationStatus validationStatus;
    @SmallField @HideInCompactView @ReadOnly
    @PrettyName(value = "Data de inserção", shortName = "Data ins")
    private Date dateInserted;
    @SmallField @HideInCompactView @ReadOnly
    @PrettyName(value = "Identificador único", shortName = "UUID")
    private UUID uuid;
    @SmallField
    @PrettyName(value = "Código GPS", shortName = "GPS")
    private String gpsCode;
    @PrettyName(value = "Ameaças do taxon", shortName = "Ameaças esp")
    private String specificThreats;
    @SmallField @HideInCompactView
    @PrettyName(value = "Exclusion reason", shortName = "Excl", description = "Reason for excluding record")
    private OccurrenceConstants.PresenceStatus presenceStatus;
    private Boolean coordinatesChanged;
    /**
     * Field to hold the matched TaxEnt ID
     */
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

    public String getVerbTaxon() {
        return verbTaxon;
    }

    public void setVerbTaxon(String verbTaxon) {
        this.verbTaxon = verbTaxon;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public String getCoverIndex() {
        return coverIndex;
    }

    public void setCoverIndex(String coverIndex) {
        this.coverIndex = coverIndex;
    }

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

    public void setDateInserted(Date dateInserted) {
        this.dateInserted = dateInserted;
    }

    public String getTaxEntMatch() {
        return taxEntMatch;
    }

    public void setTaxEntMatch(String taxEntMatch) {
        this.taxEntMatch = taxEntMatch;
    }

    public TaxEnt getTaxEnt() {
        return this.taxEnt;
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
}
