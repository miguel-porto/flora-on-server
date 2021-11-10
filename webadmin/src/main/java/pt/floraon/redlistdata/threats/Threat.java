package pt.floraon.redlistdata.threats;

import pt.floraon.redlistdata.RedListEnums;

import java.util.Objects;

public class Threat implements RedListEnums.LabelledEnumWithDescription {
    private final String name, label, description;
    private final ThreatCategory category;
    private final ThreatType type;

    Threat(String name, String label, String description, ThreatCategory category, ThreatType type) {
        this.name = name;
        this.label = label;
        this.description = description;
        this.category = category;
        this.type = type;
    }

    public ThreatCategory getCategory() {
        return this.category;
    }

    public ThreatType getType() {
        return this.type;
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
        Threat threat = (Threat) o;
        return name.equals(threat.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
