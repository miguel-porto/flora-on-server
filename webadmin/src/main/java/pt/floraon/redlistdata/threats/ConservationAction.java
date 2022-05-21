package pt.floraon.redlistdata.threats;

import pt.floraon.redlistdata.RedListEnums;

import java.util.Objects;

public class ConservationAction implements RedListEnums.LabelledEnumWithDescription {
    private final String name, label, description;
    private final ConservationActionCategory category;

    ConservationAction(String name, String label, String description, ConservationActionCategory category) {
        this.name = name;
        this.label = label;
        this.description = description;
        this.category = category;
    }

    public ConservationActionCategory getCategory() {
        return this.category;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConservationAction conservationAction = (ConservationAction) o;
        return name.equals(conservationAction.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
