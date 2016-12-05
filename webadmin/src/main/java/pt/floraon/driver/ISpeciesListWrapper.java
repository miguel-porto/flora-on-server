package pt.floraon.driver;

import pt.floraon.occurrences.entities.Author;

public interface ISpeciesListWrapper {
	/**
	 * Create a new OBSERVED_BY between this species list and an author. If it exists already, nothing happens.
	 * @param idaut The ID of the author
	 * @param isMainAuthor
	 * @return
	 * @throws FloraOnException
	 */
	public int setObservedBy(int idaut,Boolean isMainAuthor) throws FloraOnException;
	/**
	 * Create a new OBSERVED_BY between this species list and an author. If it exists already, nothing happens.
	 * @param aut The {@link Author}
	 * @param isMainAuthor Whether this is the main author (whose name comes first) or a secondary author (order arbitrary)
	 * @return 1 if relation was created, 0 if not
	 * @throws FloraOnException
	 */
	public int setObservedBy(Author aut,Boolean isMainAuthor) throws FloraOnException;

}
