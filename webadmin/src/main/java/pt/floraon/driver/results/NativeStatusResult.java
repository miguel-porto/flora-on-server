package pt.floraon.driver.results;

import pt.floraon.taxonomy.entities.EXISTS_IN;
import pt.floraon.taxonomy.entities.Territory;

/**
 * Represents one association between a territory and a taxon (NOTE: the taxon is not referred in this class)
 * @author miguel
 *
 */
public class NativeStatusResult {
	protected Territory territory;
	protected EXISTS_IN existsIn;

	public EXISTS_IN getExistsIn() {
		return this.existsIn;
	}
	
	public Territory getTerritory() {
		return this.territory;
	}
}
