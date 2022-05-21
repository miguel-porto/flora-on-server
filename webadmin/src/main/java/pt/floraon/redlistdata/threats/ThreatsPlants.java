package pt.floraon.redlistdata.threats;

public class ThreatsPlants extends MultipleChoiceEnumerationThreats implements MultipleChoiceEnumeration<Threat, ThreatCategory> {
    static {
        threats.clear();
        threatCategories.clear();
        threatTypes.clear();

        System.out.println("Loading threats for plants...");
        threatCategories.put("HUMAN_CONSTRUCTION", new ThreatCategory("CATEGORY1", "ThreatCategories.1"));
        threatCategories.put("VEGETATION_MANAGEMENT", new ThreatCategory("CATEGORY2", "ThreatCategories.2"));
        threatCategories.put("RESOURCES", new ThreatCategory("CATEGORY3", "ThreatCategories.3"));
        threatCategories.put("OTHER_HUMAN", new ThreatCategory("CATEGORY4", "ThreatCategories.4"));
        threatCategories.put("NATURAL_DYNAMICS", new ThreatCategory("CATEGORY5", "ThreatCategories.5"));
        threatCategories.put("STOCHASTIC", new ThreatCategory("CATEGORY6", "ThreatCategories.6"));
        threatCategories.put("UNKNOWN", new ThreatCategory("CATEGORY7", "ThreatCategories.7"));
        threatCategories.put("OTHER", new ThreatCategory("OTHER", "ThreatCategories.8"));

        threatTypes.put("CLIMATIC", new ThreatType("CLIMATIC", "ThreatTypes.1", "A"));
        threatTypes.put("CATTLE", new ThreatType("CATTLE", "ThreatTypes.2", "B"));
        threatTypes.put("DYNAMICS", new ThreatType("DYNAMICS", "ThreatTypes.3", "C"));
        threatTypes.put("BIOLOGICAL", new ThreatType("BIOLOGICAL", "ThreatTypes.4", "D"));
        threatTypes.put("EROSION", new ThreatType("EROSION", "ThreatTypes.5", "E"));
        threatTypes.put("RESOURCES", new ThreatType("RESOURCES", "ThreatTypes.6", "F"));
        threatTypes.put("HYDRIC_RESOURCES", new ThreatType("HYDRIC_RESOURCES", "ThreatTypes.7", "G"));
        threatTypes.put("EXOTIC_SPECIES", new ThreatType("EXOTIC_SPECIES", "ThreatTypes.8", "H"));
        threatTypes.put("FIRE", new ThreatType("FIRE", "ThreatTypes.9", "I"));
        threatTypes.put("AGRICULTURE", new ThreatType("AGRICULTURE", "ThreatTypes.10", "J"));
        threatTypes.put("FORESTRY", new ThreatType("FORESTRY", "ThreatTypes.11", "K"));
        threatTypes.put("MANAGEMENT", new ThreatType("MANAGEMENT", "ThreatTypes.12", "L"));
        threatTypes.put("OTHER", new ThreatType("OTHER", "ThreatTypes.13", "M"));
        threatTypes.put("OTHER_HUMAN", new ThreatType("OTHER_HUMAN", "ThreatTypes.14", "N"));
        threatTypes.put("RESIDUALS", new ThreatType("RESIDUALS", "ThreatTypes.15", "O"));
        threatTypes.put("CONSTRUCTION", new ThreatType("CONSTRUCTION", "ThreatTypes.16", "P"));
        threatTypes.put("STOCHASTIC_UNKNOWN", new ThreatType("STOCHASTIC_UNKNOWN", "ThreatTypes.17", "Q"));
        threatTypes.put("NONSIGNIFICANT", new ThreatType("NONSIGNIFICANT", "ThreatTypes.18", "R"));

        threats.put("CURBI", new Threat("CURBI", "Threats.1", "Threats.1.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("CONSTRUCTION")));
        threats.put("CTRAN", new Threat("CTRAN", "Threats.2", "Threats.2.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("CONSTRUCTION")));
        threats.put("CCOST", new Threat("CCOST", "Threats.3", "Threats.3.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("OTHER")));
        threats.put("CBARR", new Threat("CBARR", "Threats.4", "Threats.4.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("HYDRIC_RESOURCES")));
        threats.put("CEOLS", new Threat("CEOLS", "Threats.5", "Threats.5.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("CONSTRUCTION")));
        threats.put("CLINE", new Threat("CLINE", "Threats.6", "Threats.6.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("CONSTRUCTION")));
        threats.put("COUTR", new Threat("COUTR", "Threats.7", "Threats.7.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("CONSTRUCTION")));
        threats.put("AAGRO", new Threat("AAGRO", "Threats.8", "Threats.8.desc", threatCategories.get("VEGETATION_MANAGEMENT"), threatTypes.get("AGRICULTURE")));
        threats.put("AFLOR", new Threat("AFLOR", "Threats.9", "Threats.9.desc", threatCategories.get("VEGETATION_MANAGEMENT"), threatTypes.get("FORESTRY")));
        threats.put("APECU", new Threat("APECU", "Threats.10", "Threats.10.desc", threatCategories.get("VEGETATION_MANAGEMENT"), threatTypes.get("CATTLE")));
        threats.put("ACULT", new Threat("ACULT", "Threats.11", "Threats.11.desc", threatCategories.get("VEGETATION_MANAGEMENT"), threatTypes.get("MANAGEMENT")));
        threats.put("ADESF", new Threat("ADESF", "Threats.12", "Threats.12.desc", threatCategories.get("VEGETATION_MANAGEMENT"), threatTypes.get("MANAGEMENT")));
        threats.put("EXGEO", new Threat("EXGEO", "Threats.13", "Threats.13.desc", threatCategories.get("RESOURCES"), threatTypes.get("RESOURCES")));
        threats.put("EXHID", new Threat("EXHID", "Threats.14", "Threats.14.desc", threatCategories.get("RESOURCES"), threatTypes.get("HYDRIC_RESOURCES")));
        threats.put("EXSAL", new Threat("EXSAL", "Threats.15", "Threats.15.desc", threatCategories.get("RESOURCES"), threatTypes.get("OTHER")));
        threats.put("EXAQU", new Threat("EXAQU", "Threats.16", "Threats.16.desc", threatCategories.get("RESOURCES"), threatTypes.get("OTHER")));
        threats.put("EXREC", new Threat("EXREC", "Threats.17", "Threats.17.desc", threatCategories.get("RESOURCES"), threatTypes.get("OTHER")));
        threats.put("HLAZE", new Threat("HLAZE", "Threats.18", "Threats.18.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("OTHER_HUMAN")));
        threats.put("HPOLU", new Threat("HPOLU", "Threats.19", "Threats.19.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("RESIDUALS")));
        threats.put("HRESI", new Threat("HRESI", "Threats.20", "Threats.20.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("RESIDUALS")));
        threats.put("HLITO", new Threat("HLITO", "Threats.21", "Threats.21.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("OTHER_HUMAN")));
        threats.put("HFOGO", new Threat("HFOGO", "Threats.22", "Threats.22.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("FIRE")));
        threats.put("HDREN", new Threat("HDREN", "Threats.23", "Threats.23.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("HYDRIC_RESOURCES")));
        threats.put("BNATU", new Threat("BNATU", "Threats.24", "Threats.24.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("DYNAMICS")));
        threats.put("BEROS", new Threat("BEROS", "Threats.25", "Threats.25.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("EROSION")));
        threats.put("BNEVE", new Threat("BNEVE", "Threats.26", "Threats.26.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("CLIMATIC")));
        threats.put("BHIDR", new Threat("BHIDR", "Threats.27", "Threats.27.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("CLIMATIC")));
        threats.put("BEXOT", new Threat("BEXOT", "Threats.28", "Threats.28.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("EXOTIC_SPECIES")));
        threats.put("BHERB", new Threat("BHERB", "Threats.29", "Threats.29.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("OTHER")));
        threats.put("BREPR", new Threat("BREPR", "Threats.30", "Threats.30.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("BIOLOGICAL")));
        threats.put("BDOEN", new Threat("BDOEN", "Threats.31", "Threats.31.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("BIOLOGICAL")));
        threats.put("BPOLG", new Threat("BPOLG", "Threats.32", "Threats.32.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("BIOLOGICAL")));
        threats.put("STOCA", new Threat("STOCA", "Threats.33", "Threats.33.desc", threatCategories.get("STOCHASTIC"), threatTypes.get("STOCHASTIC_UNKNOWN")));
        threats.put("UNKN", new Threat("UNKN", "Threats.34", "Threats.34.desc", threatCategories.get("UNKNOWN"), threatTypes.get("STOCHASTIC_UNKNOWN")));
        threats.put("INEXT", new Threat("INEXT", "Threats.35", "Threats.35.desc", threatCategories.get("UNKNOWN"), threatTypes.get("NONSIGNIFICANT")));
        threats.put("OTHER", new Threat("OTHER", "Threats.36", "Threats.36.desc", threatCategories.get("OTHER"), threatTypes.get("OTHER")));
    }

}
