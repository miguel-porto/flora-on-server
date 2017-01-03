package pt.floraon.redlistdata.entities;

import java.util.Date;

import static pt.floraon.driver.Constants.dateFormat;

/**
 * Created by miguel on 28-12-2016.
 */
public class Revision {
    private Date dateSaved;
    private String user;
// TODO: store the rev field

    public Revision() {}

    public Revision(String user) {
        this.dateSaved = new Date();
        this.user = user;
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    public String getFormattedDateSaved() {
        return dateFormat.format(dateSaved);
    }

    public void setDateSaved(Date dateSaved) {
        this.dateSaved = dateSaved;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
