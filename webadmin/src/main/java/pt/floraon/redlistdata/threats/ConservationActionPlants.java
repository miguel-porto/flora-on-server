package pt.floraon.redlistdata.threats;

public class ConservationActionPlants extends MultipleChoiceEnumerationConservationActions implements MultipleChoiceEnumeration<ConservationAction, ConservationActionCategory> {
    static {
        conservationActions.clear();
        conservationActionCategories.clear();

        conservationActionCategories.put("NONE", new ConservationActionCategory("CATEGORY0", "ConservationActionCategories.0"));

        conservationActions.put("NO_MEASURES", new ConservationAction("NO_MEASURES", "ProposedConservationActions.0", "ProposedConservationActions.0.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("SITE_PROTECTION", new ConservationAction("SITE_PROTECTION","ProposedConservationActions.1", "ProposedConservationActions.1.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("INVASIVE_CONTROL", new ConservationAction("INVASIVE_CONTROL","ProposedConservationActions.2", "ProposedConservationActions.2.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("HABITAT_RESTORATION", new ConservationAction("HABITAT_RESTORATION","ProposedConservationActions.3", "ProposedConservationActions.3.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("REPOPULATION", new ConservationAction("REPOPULATION","ProposedConservationActions.4", "ProposedConservationActions.4.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("REINTRODUCTION", new ConservationAction("REINTRODUCTION","ProposedConservationActions.5", "ProposedConservationActions.5.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("OTHER", new ConservationAction("OTHER","ProposedConservationActions.6", "ProposedConservationActions.6.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("ARTIFICIAL_PROPAGATION", new ConservationAction("ARTIFICIAL_PROPAGATION","ProposedConservationActions.7", "ProposedConservationActions.7.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("GENOME_BANK", new ConservationAction("GENOME_BANK","ProposedConservationActions.8", "ProposedConservationActions.8.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("AWARENESS", new ConservationAction("AWARENESS","ProposedConservationActions.9", "ProposedConservationActions.9.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("NEW_LEGISLATION", new ConservationAction("NEW_LEGISLATION","ProposedConservationActions.10", "ProposedConservationActions.10.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("LEGISLATION_ENFORCEMENT", new ConservationAction("LEGISLATION_ENFORCEMENT","ProposedConservationActions.11", "ProposedConservationActions.11.desc", conservationActionCategories.get("NONE")));
        conservationActions.put("INCENTIVES", new ConservationAction("INCENTIVES","ProposedConservationActions.12", "ProposedConservationActions.12.desc", conservationActionCategories.get("NONE")));

    }
}
