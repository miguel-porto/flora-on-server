package pt.floraon.occurrences.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.driver.entities.GeneralDBEdge;
import pt.floraon.occurrences.OccurrenceConstants;
import pt.floraon.redlistdata.RedListEnums;

import java.io.Serializable;
import java.util.Date;

/**
 * See https://github.com/miguel-porto/flora-on-server/wiki/The-data-model#observed_in
 * Created by miguel on 05-02-2017.
 */
public class newOBSERVED_IN extends GeneralDBEdge implements Serializable {
    private Constants.PhenologicalStates phenoState;
    private OccurrenceConstants.OccurrenceNaturalization naturalization;
    private OccurrenceConstants.ConfidenceInIdentifiction confidence;
    private String verbTaxon;
    private String comment;
    private String labelData;
    private String accession;
    private String abundance;
    private RedListEnums.TypeOfPopulationEstimate typeOfEstimate;
    private String cover;
    private OccurrenceConstants.CoverType coverScale;
    private boolean hasPhoto;
    private boolean hasSpecimen;
    private String institutionCode;
    private OccurrenceConstants.ValidationStatus validationStatus;
    private Date dateInserted;

    public Constants.PhenologicalStates getPhenoState() {
        return phenoState;
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

    public String getAbundance() {
        return abundance;
    }

    public void setAbundance(String abundance) {
        this.abundance = abundance;
    }

    public RedListEnums.TypeOfPopulationEstimate getTypeOfEstimate() {
        return typeOfEstimate;
    }

    public void setTypeOfEstimate(RedListEnums.TypeOfPopulationEstimate typeOfEstimate) {
        this.typeOfEstimate = typeOfEstimate;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public OccurrenceConstants.CoverType getCoverScale() {
        return coverScale;
    }

    public void setCoverScale(OccurrenceConstants.CoverType coverScale) {
        this.coverScale = coverScale;
    }

    public boolean isHasPhoto() {
        return hasPhoto;
    }

    public void setHasPhoto(boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public boolean isHasSpecimen() {
        return hasSpecimen;
    }

    public void setHasSpecimen(boolean hasSpecimen) {
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

    @Override
    public String getTypeAsString() {
        return this.getType().toString();
    }

    @Override
    public pt.floraon.driver.Constants.RelTypes getType() {
        return Constants.RelTypes.OBSERVED_IN;
    }

    @Override
    public JsonObject toJson() {
        return super._toJson();
    }

    @Override
    public String toJsonString() {
        return this.toJson().toString();
    }
}
