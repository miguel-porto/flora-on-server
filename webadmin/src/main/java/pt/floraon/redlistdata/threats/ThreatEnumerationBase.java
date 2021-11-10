package pt.floraon.redlistdata.threats;

import java.util.*;

public abstract class ThreatEnumerationBase implements ThreatEnumeration {
    static final Map<String, Threat> threats = new LinkedHashMap<>();
    static final Map<String, ThreatCategory> threatCategories = new HashMap<>();
    static final Map<String, ThreatType> threatTypes = new HashMap<>();

    @Override
    public Threat[] values() {
        return threats.values().toArray(new Threat[0]);
    }

    public static Threat[] valuesOf(String[] names) {
        List<Threat> out = new ArrayList<>();
        for(String name : names)
            out.add(valueOf(name));
        return out.toArray(new Threat[0]);
    }

    public static Threat valueOf(Object name) {
        return threats.get(name.toString());
    }

    @Override
    public Map<String, Threat> getThreats() {
        return threats;
    }

    @Override
    public Map<String, ThreatCategory> getThreatCategories() {
        return threatCategories;
    }

}
