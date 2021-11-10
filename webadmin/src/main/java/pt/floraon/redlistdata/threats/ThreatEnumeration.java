package pt.floraon.redlistdata.threats;

import java.util.Map;

public interface ThreatEnumeration {
    Map<String, Threat> getThreats();
    Threat[] values();
    Map<String, ThreatCategory> getThreatCategories();
}
