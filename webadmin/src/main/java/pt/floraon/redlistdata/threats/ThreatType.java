package pt.floraon.redlistdata.threats;

import pt.floraon.redlistdata.RedListEnums;

public class ThreatType implements RedListEnums.LabelledEnum {
    private final String name, description, letter;

    public ThreatType(String name, String description, String letter) {
        this.name = name;
        this.description = description;
        this.letter = letter;
    }

    @Override
    public String getLabel() {
        return this.description;
    }

    public String getLetter() {
        return this.letter;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
