package pt.floraon.redlistdata.entities;

/**
 * Created by miguel on 06-01-2017.
 */
public class PreviousAssessment {
    private Integer year;
    private RedListEnums.RedListCategories category;

    public PreviousAssessment() {}

    public PreviousAssessment(int year, RedListEnums.RedListCategories category) {
        this.year = year;
        this.category = category;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public RedListEnums.RedListCategories getCategory() {
        return category;
    }

    public void setCategory(RedListEnums.RedListCategories category) {
        this.category = category;
    }
}
