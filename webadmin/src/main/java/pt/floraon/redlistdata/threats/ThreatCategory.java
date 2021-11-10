package pt.floraon.redlistdata.threats;

import pt.floraon.redlistdata.RedListEnums;

public class ThreatCategory implements RedListEnums.LabelledEnumWithDescription {
    private final String name, description;

    public ThreatCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getLabel() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
