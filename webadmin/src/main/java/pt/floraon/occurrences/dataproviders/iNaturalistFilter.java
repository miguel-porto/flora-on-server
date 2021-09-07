package pt.floraon.occurrences.dataproviders;

import pt.floraon.driver.utils.StringUtils;

import java.util.Locale;

public class iNaturalistFilter {
    private String project_id;
    private String[] taxon_names;
    private String[] ident_user_id;
    private String[] user_id;

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String[] getTaxon_names() {
        return taxon_names;
    }

    public String getTaxon_namesAsString(String separator) {
        return StringUtils.implode(separator, taxon_names);
    }

    public String getTaxon_namesAsString() {
        return getTaxon_namesAsString("\n");
    }

    public void setTaxon_names(String[] taxon_name) {
        this.taxon_names = taxon_name;
    }

    public String[] getIdent_user_id() {
        return ident_user_id;
    }

    public String getIdent_user_idAsString(String separator) {
        return StringUtils.implode(separator, ident_user_id);
    }

    public String getIdent_user_idAsString() {
        return getIdent_user_idAsString("\n");
    }

    public void setIdent_user_id(String[] ident_user_id) {
        this.ident_user_id = ident_user_id;
    }

    public String[] getUser_id() {
        return user_id;
    }

    public String getUser_idAsString(String separator) {
        return StringUtils.implode(separator, user_id);
    }

    public String getUser_idAsString() {
        return getUser_idAsString("\n");
    }

    public void setUser_id(String[] user_id) {
        this.user_id = user_id;
    }

    public iNaturalistFilter withProjectId(String project_id) {
        this.project_id = project_id;
        return (this);
    }

    public iNaturalistFilter withTaxonNames(String taxonNames) {
        if(taxonNames == null)
            this.taxon_names = new String[0];
        else
            this.taxon_names = taxonNames.toLowerCase(Locale.ROOT).split("[,\\n]");
        return (this);
    }

    public iNaturalistFilter withTaxonNames(String[] taxonNames) {
        if(taxonNames == null)
            this.taxon_names = new String[0];
        else {
            for (int i = 0; i < taxonNames.length; i++) {
                taxonNames[i] = taxonNames[i].toLowerCase();
            }
            this.taxon_names = taxonNames;
        }
        return (this);
    }

    public iNaturalistFilter withIdentifiers(String identifiers) {
        this.ident_user_id = identifiers.split("[,\\n]");
        return (this);
    }

    public iNaturalistFilter withObservers(String observers) {
        this.user_id = observers.split("[,\\n]");
        return (this);
    }

    public iNaturalistFilter withObservers(String[] observers) {
        if(observers == null)
            this.user_id = new String[0];
        else {
            for (int i = 0; i < observers.length; i++) {
                observers[i] = observers[i].toLowerCase();
            }
            this.user_id = observers;
        }
        return (this);
    }

    public iNaturalistFilter withIdentifiers(String[] identifiers) {
        if(identifiers == null)
            this.ident_user_id = new String[0];
        else {
            for (int i = 0; i < identifiers.length; i++) {
                identifiers[i] = identifiers[i].toLowerCase();
            }
            this.ident_user_id = identifiers;
        }
        return (this);
    }

    public iNaturalistFilter(iNaturalistFilter other) {
        this.project_id = other.project_id;
        this.taxon_names = other.taxon_names;
        this.ident_user_id = other.ident_user_id;
        this.user_id = other.user_id;
    }

    public iNaturalistFilter() {
    }

/*
    public boolean ident_user_id_Matches(iNaturalistDataProvider.iNaturalistOccurrence o) {
        if(StringUtils.isArrayEmpty(this.ident_user_id)) return true;
        for(iNaturalistDataProvider.iNaturalistIdentification i : o.identifications) {
            if(ArrayUtils.contains(this.ident_user_id, i.user.login)) return true;
        }
        return false;
    }
*/
}
