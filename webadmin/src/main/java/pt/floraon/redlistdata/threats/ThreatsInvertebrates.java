package pt.floraon.redlistdata.threats;

public class ThreatsInvertebrates extends ThreatEnumerationBase implements ThreatEnumeration {
    static {
        threats.clear();
        threatCategories.clear();
        threatTypes.clear();

        System.out.println("Loading threats for invertebrates...");
        threatCategories.put("HUMAN_CONSTRUCTION", new ThreatCategory("CATEGORY1", "ThreatCategories.inv.1"));
        threatCategories.put("AGRICULTURE", new ThreatCategory("CATEGORY2", "ThreatCategories.inv.2"));
        threatCategories.put("ENERGY", new ThreatCategory("CATEGORY3", "ThreatCategories.inv.3"));
        threatCategories.put("TRANSPORTATION", new ThreatCategory("CATEGORY4", "ThreatCategories.inv.4"));
        threatCategories.put("RESOURCES", new ThreatCategory("CATEGORY5", "ThreatCategories.inv.5"));
        threatCategories.put("OTHER_HUMAN", new ThreatCategory("CATEGORY6", "ThreatCategories.inv.6"));
        threatCategories.put("NATURAL_DYNAMICS", new ThreatCategory("CATEGORY7", "ThreatCategories.inv.7"));
        threatCategories.put("BIOLOGICAL", new ThreatCategory("CATEGORY8", "ThreatCategories.inv.8"));
        threatCategories.put("POLLUTION", new ThreatCategory("CATEGORY9", "ThreatCategories.inv.9"));
        threatCategories.put("GEOLOGICAL", new ThreatCategory("CATEGORY10", "ThreatCategories.inv.10"));
        threatCategories.put("CLIMATE", new ThreatCategory("CATEGORY11", "ThreatCategories.inv.11"));
        threatCategories.put("OTHER", new ThreatCategory("OTHER", "ThreatCategories.inv.12"));

        threatTypes.put("UNSPECIFIED", new ThreatType("UNSPECIFIED", "UNSPECIFIED", "U"));

        threats.put("URBAN", new Threat("URBAN", "Threats.inv.01", "Threats.inv.01.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("INDUSTRY", new Threat("INDUSTRY", "Threats.inv.02", "Threats.inv.02.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("TOURISM", new Threat("TOURISM", "Threats.inv.03", "Threats.inv.03.desc", threatCategories.get("HUMAN_CONSTRUCTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("SHIFTAGRIC", new Threat("SHIFTAGRIC", "Threats.inv.04", "Threats.inv.04.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("SMALLFARM", new Threat("SMALLFARM", "Threats.inv.05", "Threats.inv.05.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("INDFARM", new Threat("INDFARM", "Threats.inv.06", "Threats.inv.06.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("OTHERFARM", new Threat("OTHERFARM", "Threats.inv.07", "Threats.inv.07.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("SMALLWOOD", new Threat("SMALLWOOD", "Threats.inv.08", "Threats.inv.08.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("INDWOOD", new Threat("INDWOOD", "Threats.inv.09", "Threats.inv.09.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("OTHERWOOD", new Threat("OTHERWOOD", "Threats.inv.10", "Threats.inv.10.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("NOMADGRAZ", new Threat("NOMADGRAZ", "Threats.inv.11", "Threats.inv.11.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("SMALLGRAZ", new Threat("SMALLGRAZ", "Threats.inv.12", "Threats.inv.12.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("INDGRAZ", new Threat("INDGRAZ", "Threats.inv.13", "Threats.inv.13.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("OTHERGRAZ", new Threat("OTHERGRAZ", "Threats.inv.14", "Threats.inv.14.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("SMALLAQUA", new Threat("SMALLAQUA", "Threats.inv.15", "Threats.inv.15.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("INDAQUA", new Threat("INDAQUA", "Threats.inv.16", "Threats.inv.16.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("OTHERAQUA", new Threat("OTHERAQUA", "Threats.inv.17", "Threats.inv.17.desc", threatCategories.get("AGRICULTURE"), threatTypes.get("UNSPECIFIED")));
        threats.put("OILDRILL", new Threat("OILDRILL", "Threats.inv.18", "Threats.inv.18.desc", threatCategories.get("ENERGY"), threatTypes.get("UNSPECIFIED")));
        threats.put("MINING", new Threat("MINING", "Threats.inv.19", "Threats.inv.19.desc", threatCategories.get("ENERGY"), threatTypes.get("UNSPECIFIED")));
        threats.put("RENEWENER", new Threat("RENEWENER", "Threats.inv.20", "Threats.inv.20.desc", threatCategories.get("ENERGY"), threatTypes.get("UNSPECIFIED")));
        threats.put("ROADS", new Threat("ROADS", "Threats.inv.21", "Threats.inv.21.desc", threatCategories.get("TRANSPORTATION"), threatTypes.get("UNSPECIFIED")));
        threats.put("SERVICELINES", new Threat("SERVICELINES", "Threats.inv.22", "Threats.inv.22.desc", threatCategories.get("TRANSPORTATION"), threatTypes.get("UNSPECIFIED")));
        threats.put("SHIPLANES", new Threat("SHIPLANES", "Threats.inv.23", "Threats.inv.23.desc", threatCategories.get("TRANSPORTATION"), threatTypes.get("UNSPECIFIED")));
        threats.put("FLIGHTPATHS", new Threat("FLIGHTPATHS", "Threats.inv.24", "Threats.inv.24.desc", threatCategories.get("TRANSPORTATION"), threatTypes.get("UNSPECIFIED")));
        threats.put("HUNTINT", new Threat("HUNTINT", "Threats.inv.25", "Threats.inv.25.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("HUNTUNINT", new Threat("HUNTUNINT", "Threats.inv.26", "Threats.inv.26.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("HUNTCONTROL", new Threat("HUNTCONTROL", "Threats.inv.27", "Threats.inv.27.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("HUNTOTHER", new Threat("HUNTOTHER", "Threats.inv.28", "Threats.inv.28.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("GATHERINT", new Threat("GATHERINT", "Threats.inv.29", "Threats.inv.29.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("GATHERUNINT", new Threat("GATHERUNINT", "Threats.inv.30", "Threats.inv.30.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("GATHERCONTROL", new Threat("GATHERCONTROL", "Threats.inv.31", "Threats.inv.31.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("GATHEROTHER", new Threat("GATHEROTHER", "Threats.inv.32", "Threats.inv.32.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("LOGINTSMALL", new Threat("LOGINTSMALL", "Threats.inv.33", "Threats.inv.33.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("LOGINTLARGE", new Threat("LOGINTLARGE", "Threats.inv.34", "Threats.inv.34.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("LOGUNINTSMALL", new Threat("LOGUNINTSMALL", "Threats.inv.35", "Threats.inv.35.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("LOGUNINTLARGE", new Threat("LOGUNINTLARGE", "Threats.inv.36", "Threats.inv.36.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("LOGUNK", new Threat("LOGUNK", "Threats.inv.37", "Threats.inv.37.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("FISHINTSMALL", new Threat("FISHINTSMALL", "Threats.inv.38", "Threats.inv.38.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("FISHINTLARGE", new Threat("FISHINTLARGE", "Threats.inv.39", "Threats.inv.39.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("FISHUNINTSMALL", new Threat("FISHUNINTSMALL", "Threats.inv.40", "Threats.inv.40.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("FISHUNINTLARGE", new Threat("FISHUNINTLARGE", "Threats.inv.41", "Threats.inv.41.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("FISHCONTROL", new Threat("FISHCONTROL", "Threats.inv.42", "Threats.inv.42.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("FISHOTHER", new Threat("FISHOTHER", "Threats.inv.43", "Threats.inv.43.desc", threatCategories.get("RESOURCES"), threatTypes.get("UNSPECIFIED")));
        threats.put("RECREAT", new Threat("RECREAT", "Threats.inv.44", "Threats.inv.44.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("UNSPECIFIED")));
        threats.put("WAR", new Threat("WAR", "Threats.inv.45", "Threats.inv.45.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("UNSPECIFIED")));
        threats.put("WORK", new Threat("WORK", "Threats.inv.46", "Threats.inv.46.desc", threatCategories.get("OTHER_HUMAN"), threatTypes.get("UNSPECIFIED")));
        threats.put("FIREINC", new Threat("FIREINC", "Threats.inv.47", "Threats.inv.47.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("FIREDEC", new Threat("FIREDEC", "Threats.inv.48", "Threats.inv.48.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("FIREUNK", new Threat("FIREUNK", "Threats.inv.49", "Threats.inv.49.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("SRFWATERDOM", new Threat("SRFWATERDOM", "Threats.inv.50", "Threats.inv.50.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("SRFWATERCOMM", new Threat("SRFWATERCOMM", "Threats.inv.51", "Threats.inv.51.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("SRFWATERAGRI", new Threat("SRFWATERAGRI", "Threats.inv.52", "Threats.inv.52.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("SRFWATERUNK", new Threat("SRFWATERUNK", "Threats.inv.53", "Threats.inv.53.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("GRDWATERDOM", new Threat("GRDWATERDOM", "Threats.inv.54", "Threats.inv.54.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("GRDWATERCOMM", new Threat("GRDWATERCOMM", "Threats.inv.55", "Threats.inv.55.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("GRDWATERAGRI", new Threat("GRDWATERAGRI", "Threats.inv.56", "Threats.inv.56.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("GRDWATERUNK", new Threat("GRDWATERUNK", "Threats.inv.57", "Threats.inv.57.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("DAMSMALL", new Threat("DAMSMALL", "Threats.inv.58", "Threats.inv.58.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("DAMLARGE", new Threat("DAMLARGE", "Threats.inv.59", "Threats.inv.59.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("DAMUNK", new Threat("DAMUNK", "Threats.inv.60", "Threats.inv.60.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("ECOMODIF", new Threat("ECOMODIF", "Threats.inv.61", "Threats.inv.61.desc", threatCategories.get("NATURAL_DYNAMICS"), threatTypes.get("UNSPECIFIED")));
        threats.put("INVASUNK", new Threat("INVASUNK", "Threats.inv.62", "Threats.inv.62.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("INVASKN", new Threat("INVASKN", "Threats.inv.63", "Threats.inv.63.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("NATIVUNK", new Threat("NATIVUNK", "Threats.inv.64", "Threats.inv.64.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("NATIKN", new Threat("NATIKN", "Threats.inv.65", "Threats.inv.65.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("NATOTHER", new Threat("NATOTHER", "Threats.inv.66", "Threats.inv.66.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("GENMAT", new Threat("GENMAT", "Threats.inv.67", "Threats.inv.67.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("NATIVUNKORIUNK", new Threat("NATIVUNKORIUNK", "Threats.inv.68", "Threats.inv.68.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("NATUUNKORIKN", new Threat("NATUUNKORIKN", "Threats.inv.69", "Threats.inv.69.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("VIRALUNK", new Threat("VIRALUNK", "Threats.inv.70", "Threats.inv.70.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("VIRALKN", new Threat("VIRALKN", "Threats.inv.71", "Threats.inv.71.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("DISEASEUNK", new Threat("DISEASEUNK", "Threats.inv.72", "Threats.inv.72.desc", threatCategories.get("BIOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("SEWAGE", new Threat("SEWAGE", "Threats.inv.73", "Threats.inv.73.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("RUNOFF", new Threat("RUNOFF", "Threats.inv.74", "Threats.inv.74.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("WASTEUNK", new Threat("WASTEUNK", "Threats.inv.75", "Threats.inv.75.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("OILSPILL", new Threat("OILSPILL", "Threats.inv.76", "Threats.inv.76.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("SEEPAGE", new Threat("SEEPAGE", "Threats.inv.77", "Threats.inv.77.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("EFFLUUNK", new Threat("EFFLUUNK", "Threats.inv.78", "Threats.inv.78.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("NUTRIENT", new Threat("NUTRIENT", "Threats.inv.79", "Threats.inv.79.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("EROSION", new Threat("EROSION", "Threats.inv.80", "Threats.inv.80.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("HERBICIDE", new Threat("HERBICIDE", "Threats.inv.81", "Threats.inv.81.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("AGRICEFFLUNK", new Threat("AGRICEFFLUNK", "Threats.inv.82", "Threats.inv.82.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("GARBAGE", new Threat("GARBAGE", "Threats.inv.83", "Threats.inv.83.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("ACIDRAIN", new Threat("ACIDRAIN", "Threats.inv.84", "Threats.inv.84.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("SMOG", new Threat("SMOG", "Threats.inv.85", "Threats.inv.85.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("OZONE", new Threat("OZONE", "Threats.inv.86", "Threats.inv.86.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("POLLUTUNK", new Threat("POLLUTUNK", "Threats.inv.87", "Threats.inv.87.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("LIGHTPOLLUT", new Threat("LIGHTPOLLUT", "Threats.inv.88", "Threats.inv.88.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("THERMPOLLUT", new Threat("THERMPOLLUT", "Threats.inv.89", "Threats.inv.89.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("NOISEPOLLUT", new Threat("NOISEPOLLUT", "Threats.inv.90", "Threats.inv.90.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("ENERGYUNK", new Threat("ENERGYUNK", "Threats.inv.91", "Threats.inv.91.desc", threatCategories.get("POLLUTION"), threatTypes.get("UNSPECIFIED")));
        threats.put("VOLCANO", new Threat("VOLCANO", "Threats.inv.92", "Threats.inv.92.desc", threatCategories.get("GEOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("EARTHQUAKE", new Threat("EARTHQUAKE", "Threats.inv.93", "Threats.inv.93.desc", threatCategories.get("GEOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("LANDSLIDE", new Threat("LANDSLIDE", "Threats.inv.94", "Threats.inv.94.desc", threatCategories.get("GEOLOGICAL"), threatTypes.get("UNSPECIFIED")));
        threats.put("HABITATALTER", new Threat("HABITATALTER", "Threats.inv.95", "Threats.inv.95.desc", threatCategories.get("CLIMATE"), threatTypes.get("UNSPECIFIED")));
        threats.put("DROUGHT", new Threat("DROUGHT", "Threats.inv.96", "Threats.inv.96.desc", threatCategories.get("CLIMATE"), threatTypes.get("UNSPECIFIED")));
        threats.put("TEMPEXTR", new Threat("TEMPEXTR", "Threats.inv.97", "Threats.inv.97.desc", threatCategories.get("CLIMATE"), threatTypes.get("UNSPECIFIED")));
        threats.put("STORMS", new Threat("STORMS", "Threats.inv.98", "Threats.inv.98.desc", threatCategories.get("CLIMATE"), threatTypes.get("UNSPECIFIED")));
        threats.put("OTHERIMPACT", new Threat("OTHERIMPACT", "Threats.inv.99", "Threats.inv.99.desc", threatCategories.get("CLIMATE"), threatTypes.get("UNSPECIFIED")));
        threats.put("OTHER", new Threat("OTHER", "Threats.inv.100", "Threats.inv.100.desc", threatCategories.get("OTHER"), threatTypes.get("UNSPECIFIED")));
    }
}