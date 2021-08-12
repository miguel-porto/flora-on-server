package pt.floraon.occurrences.dataproviders;

import pt.floraon.occurrences.entities.Occurrence;

public interface DataProviderTranslator {
    Occurrence translate(Object o);
}
