package pt.floraon.redlistdata.threats;

import pt.floraon.redlistdata.RedListEnums;

import java.util.Map;

public interface MultipleChoiceEnumeration<T extends RedListEnums.LabelledEnumWithDescription, C extends RedListEnums.LabelledEnumWithDescription> {
    Map<String, T> getEnumeration();
    T[] values();
    Map<String, C> getEnumerationCategories();
}
