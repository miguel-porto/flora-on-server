package pt.floraon.redlistdata.entities;

import pt.floraon.driver.Constants;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.redlistdata.dataproviders.SimpleOccurrence;

import java.util.Date;
import java.util.List;

public class RedListDataEntitySnapshot extends RedListDataEntity {
    private List<SimpleOccurrence> occurrences;
    private Date dateSaved;

    public String getVersionTag() {
        return versionTag;
    }

    public boolean hasVersionTag() {
        return !StringUtils.isStringEmpty(versionTag);
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    private String versionTag;

    public List<SimpleOccurrence> getOccurrences() {
        return this.occurrences;
    }

    public void setOccurrences(List<SimpleOccurrence> occurrences) {
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
