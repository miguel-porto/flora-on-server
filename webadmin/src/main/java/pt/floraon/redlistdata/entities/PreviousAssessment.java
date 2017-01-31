package pt.floraon.redlistdata.entities;

import pt.floraon.driver.DiffableBean;
import pt.floraon.redlistdata.RedListEnums;

/**
 * Created by miguel on 06-01-2017.
 */
public class PreviousAssessment implements DiffableBean {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreviousAssessment that = (PreviousAssessment) o;

        if (year != null ? !year.equals(that.year) : that.year != null) return false;
        return category == that.category;
    }

    @Override
    public int hashCode() {
        int result = year != null ? year.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }
}
