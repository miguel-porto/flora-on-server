package pt.floraon.redlistdata.entities;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import static pt.floraon.driver.Constants.dateFormat;
import static pt.floraon.driver.Constants.dateTimeFormat;

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

    public Date getDateTimeSaved() {
        return dateSaved;
    }

    public String getFormattedDateTimeSaved() {
        return dateTimeFormat.format(dateSaved);
    }

    public String getFormattedDateSaved() {
        return dateFormat.format(dateSaved);
    }

    public Revision getDayWiseRevision() {
        Revision out = new Revision();
        Calendar c1 = new GregorianCalendar();
        Calendar c2 = new GregorianCalendar();
        c1.setTime(dateSaved);
        c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
        c2.set(Calendar.MONTH, c1.get(Calendar.MONTH));
        c2.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));
        c2.set(Calendar.HOUR, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);
        out.setDateSaved(c2.getTime());
        out.setUser(user);
        return out;
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

    public static class RevisionComparator implements Comparator<Revision> {
        public int compare(Revision o1, Revision o2) {
            return o1.getDateTimeSaved().compareTo(o2.getDateTimeSaved());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Revision revision = (Revision) o;

        if (!dateSaved.equals(revision.dateSaved)) return false;
        return user != null ? user.equals(revision.user) : revision.user == null;
    }

    @Override
    public int hashCode() {
        int result = dateSaved.hashCode();
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
