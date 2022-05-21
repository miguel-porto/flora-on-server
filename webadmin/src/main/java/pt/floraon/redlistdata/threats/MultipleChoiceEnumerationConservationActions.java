package pt.floraon.redlistdata.threats;

import java.util.*;

public abstract class MultipleChoiceEnumerationConservationActions implements MultipleChoiceEnumeration<ConservationAction, ConservationActionCategory> {
    static final Map<String, ConservationAction> conservationActions = new LinkedHashMap<>();
    static final Map<String, ConservationActionCategory> conservationActionCategories = new HashMap<>();

    @Override
    public ConservationAction[] values() {
        return conservationActions.values().toArray(new ConservationAction[0]);
    }

    public static ConservationAction[] valuesOf(String[] names) {
        List<ConservationAction> out = new ArrayList<>();
        for(String name : names)
            out.add(valueOf(name));
        return out.toArray(new ConservationAction[0]);
    }

    public static ConservationAction valueOf(Object name) {
        return conservationActions.get(name.toString());
    }

    @Override
    public Map<String, ConservationAction> getEnumeration() {
        return conservationActions;
    }

    @Override
    public Map<String, ConservationActionCategory> getEnumerationCategories() {
        return conservationActionCategories;
    }

}
