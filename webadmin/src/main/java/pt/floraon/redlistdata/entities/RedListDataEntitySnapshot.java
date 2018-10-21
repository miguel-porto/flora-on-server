package pt.floraon.redlistdata.entities;

import pt.floraon.driver.Constants;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Occurrence;

import java.util.Date;
import java.util.List;

public class RedListDataEntitySnapshot extends RedListDataEntity {
    private List<Occurrence> occurrences;
    private Date dateSaved;
    private String savedByUser;
    private String versionTag;

    public String getVersionTag() {
        return versionTag;
    }

    public boolean hasVersionTag() {
        return !StringUtils.isStringEmpty(versionTag);
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    public String getSavedByUser() {
        return savedByUser;
    }

    public void setSavedByUser(String savedByUser) {
        this.savedByUser = savedByUser;
    }

    public List<Occurrence> getOccurrences() {
        return this.occurrences;
    }

    public void setOccurrences(List<Occurrence> occurrences) {
        this.occurrences = occurrences;
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    public String _getDateSavedFormatted() {
        return Constants.dateTimeFormat.get().format(this.dateSaved);
    }
    public String _getDateSavedFormattedTwoLine() {
        return Constants.dateTimeFormatTwoLine.get().format(this.dateSaved);
    }

    public void setDateSaved(Date dateSaved) {
        this.dateSaved = dateSaved;
    }

    public void updateDateSaved() {
        this.dateSaved = new Date();
    }
}
