package pt.floraon.bibliography.entities;

import pt.floraon.driver.Constants;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.entities.GeneralDBNode;
import pt.floraon.driver.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reference extends GeneralDBNode implements Comparable<Reference> {
    private String authors, year, title, publication, coords, volume, pages, editor, city, code;
    private Constants.PublicationType publicationType;
    private transient Character suffix;

/*
    private transient static Pattern authorList = Pattern.compile(
            "^\\s*(?<first>[\\w çãõáàâéêíóôú.-]+)(?<middle> *, *[\\w çãõáàâéêíóôú.-]+)* *(?:,|& *(?<last>[\\w çãõáàâéêíóôú.-]+))?$", Pattern.CASE_INSENSITIVE);
*/
    private transient static Pattern authorList = Pattern.compile(
            "^\\s*(?<first>[\\w çãõáàâéêíóôúñÁüöÅ-]+)[, ]? *(?:[\\wçãõáàâéêíóôúÁüöÅ]{1,2}\\. *)*" +
                    "(?:(?<middle> *, *(?:[\\w çãõáàâéêíóôúñÁüöÅ-]+)[, ] *(?:[\\wçãõáàâéêíóôúÁüöÅ]{1,2}\\. *)+)* *" +
                    ",? *&? *(?<last>[\\w çãõáàâéêíóôúñÁüöÅ-]+)[, ] *(?:[\\wçãõáàâéêíóôúÁüöÅ]{1,2}\\. *)+)?\\s*$", Pattern.CASE_INSENSITIVE);

/*
    private transient static Pattern oneAuthor = Pattern.compile(
            "^(?<surname>[\\w çãõáàâéêíóôú-]+)[, ] *(?:[\\wçãõáàâéêíóôú]+\\. *)+$", Pattern.CASE_INSENSITIVE);
*/

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) throws FloraOnException {
        if(authors == null) return;
        Matcher m = authorList.matcher(authors);
        if(!m.find()) throw new FloraOnException("Author list not properly formatted: " + authors);
        this.authors = authors;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public String getCoords() {
        return coords;
    }

    public void setCoords(String coords) {
        this.coords = coords;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPublicationType() {
        return publicationType == null ? null : publicationType.toString();
    }

    public void setPublicationType(String publicationType) throws FloraOnException {
        if(publicationType == null || publicationType.trim().equals("")) return;
        try {
            this.publicationType = Constants.PublicationType.valueOf(publicationType);
        } catch (IllegalArgumentException e) {
            throw new FloraOnException(e.getMessage());
        }
    }

    public String _getCitation() throws FloraOnException {
        String names, first, last = null;
        Matcher m = authorList.matcher(this.getAuthors());
        if(!m.find()) return "ERROR";
/*
        System.out.println("FIRST: "+m.group("first"));
        System.out.println("MIDDLE: "+m.group("middle"));
        System.out.println("LAST: "+m.group("last"));
*/
        boolean hasMiddle = m.group("middle") != null;

        first = m.group("first");
/*
        Matcher f = oneAuthor.matcher(m.group("first"));
        first = f.find() ? f.group("surname") : m.group("first");
*/
        last = m.group("last");

        names = hasMiddle ?
            first + " <i>et al.</i>"
            : (last == null ? first : first + " & " + last);
        return names + " " + this.getYear();
    }

    public String _getBibliographyEntry() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getAuthors()).append(" ")
                .append(this.getYear()).append(this.suffix == null ? "" : this.suffix).append(". ")
                .append("<i>").append(this.getTitle()).append("</i>.");

        if(!StringUtils.isStringEmpty(this.getPublication())) {
            sb.append(" ").append(this.getPublication()).append(".");
        }

        if(!StringUtils.isStringEmpty(this.getVolume())) {
            sb.append(" ");
            if(!StringUtils.isStringEmpty(this.getPages()))
                sb.append(this.getVolume()).append(": ").append(this.getPages());
            else
                sb.append(this.getVolume()).append(". ");
        } else {
            if(!StringUtils.isStringEmpty(this.getPages()))
                sb.append(": ").append(this.getPages());
            else
                sb.append(". ");
        }

        sb.append(this.getCode());
        return sb.toString();
    }

    /**
     * Sets the suffix to append after the year to avoid citation collisions.
     * This is done by the {@link pt.floraon.bibliography.BibliographyCompiler}.
     * @param suffix
     */
    public void _setSuffix(char suffix) {
        this.suffix = suffix;
    }

    @Override
    public Constants.NodeTypes getType() {
        return Constants.NodeTypes.reference;
    }

    @Override
    public String getTypeAsString() {
        return getType().toString();
    }

    @Override
    public int compareTo(Reference reference) {
        String cmp1 = this.getAuthors() + this.getYear() + this.suffix;
        String cmp2 = reference.getAuthors() + reference.getYear() + reference.suffix;
        return cmp1.toLowerCase().compareTo(cmp2.toLowerCase());
    }
}
