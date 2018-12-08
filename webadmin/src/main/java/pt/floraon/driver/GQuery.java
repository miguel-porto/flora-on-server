package pt.floraon.driver;

import java.util.*;

import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.IQuery;
import pt.floraon.driver.interfaces.OccurrenceFilter;
import pt.floraon.geometry.Point2D;
import pt.floraon.geometry.Polygon;
import pt.floraon.geometry.PolygonTheme;
import pt.floraon.occurrences.iterators.InventoryIterator;
import pt.floraon.occurrences.iterators.OccurrenceIterator;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.Occurrence;
import pt.floraon.queryparser.Match;
import pt.floraon.driver.results.SimpleTaxonResult;

public abstract class GQuery extends BaseFloraOnDriver implements IQuery {

	public GQuery(IFloraOn driver) {
		super(driver);
	}

	@Override
    public List<SimpleTaxonResult> fetchMatchSpecies(Match match,boolean onlyLeafNodes,boolean onlyCurrent) throws DatabaseException {
		Map<String,SimpleTaxonResult> out;
		SimpleTaxonResult tmp2;
		List<SimpleTaxonResult> tmp1=speciesTextQuerySimple(match.query,match.getMatchType(),onlyLeafNodes,onlyCurrent,new String[]{match.getNodeType().toString()},match.getRank());
		// when there is more than one path to the result node, it will come out repeatedly, one time for each path.
		// so we make this unique here, and choose the shortest path.
		out=new HashMap<String,SimpleTaxonResult>();
		// now remove duplicated taxon matches according to priority.
		for(SimpleTaxonResult str : tmp1) {
			if(out.containsKey(str.getTaxonId())) {		// result already exists, let's see if this one has priority over exiting one
				tmp2=out.get(str.getTaxonId());
				if(str.hasPriorityOver(tmp2)) out.put(str.getTaxonId(), str);
			} else
				out.put(str.getTaxonId(), str);
		}
		return new ArrayList<SimpleTaxonResult>(out.values());
    }

	@Override
	public Iterator<Inventory> findInventoriesContainedIn(String geoJsonPolygon, String filter) throws FloraOnException {
		Iterator<Inventory> out;
		final Map<String, String> parsedFilter;
		final PolygonTheme polyt = new PolygonTheme(geoJsonPolygon);
		final Polygon poly = polyt.iterator().next().getValue();

		OccurrenceFilter geoFilter = new OccurrenceFilter() {
			@Override
			public boolean enter(Inventory inventory) {
				if(inventory._getLongitude() == null || inventory._getLatitude() == null)
					return false;
				else
					return poly.contains(new Point2D(inventory._getLongitude(), inventory._getLatitude()));
			}
		};

		parsedFilter = driver.getOccurrenceDriver().parseFilterExpression(filter);
		out = driver.getOccurrenceDriver().findInventoriesByFilter(parsedFilter, null, null, null);
		return new InventoryIterator(out, new OccurrenceFilter[] {geoFilter});
	}

	@Override
	public Iterator<Occurrence> findOccurrencesContainedIn(final String geoJsonPolygon, final String filterExpression
			, OccurrenceFilter filter) throws FloraOnException {
		Iterator<Occurrence> out;
		final Map<String, String> parsedFilter;
		final PolygonTheme polyt = new PolygonTheme(geoJsonPolygon);
		final Polygon poly = polyt.iterator().next().getValue();

		OccurrenceFilter geoFilter = new OccurrenceFilter() {
			@Override
			public boolean enter(Inventory simpleOccurrence) {
				if(simpleOccurrence._getLongitude() == null || simpleOccurrence._getLatitude() == null)
					return false;
				else
					return poly.contains(new Point2D(simpleOccurrence._getLongitude(), simpleOccurrence._getLatitude()));
			}
		};

		parsedFilter = driver.getOccurrenceDriver().parseFilterExpression(filterExpression);
		out = driver.getOccurrenceDriver().findOccurrencesByFilter(parsedFilter, null, null, null);
		return new OccurrenceIterator(out, new OccurrenceFilter[] {geoFilter, filter});
	}

/*
	@Override
	public Iterator<Occurrence> findOccurrencesContainedIn(final String geoJsonPolygon, final OccurrenceFilter filter) throws FloraOnException {
		Iterator<Occurrence> out;
		ArangoCursor<Occurrence> tmp;
		final PolygonTheme polyt = new PolygonTheme(geoJsonPolygon);
		final Polygon poly = polyt.iterator().next().getValue();

		OccurrenceFilter geoFilter = new OccurrenceFilter() {
			@Override
			public boolean enter(Inventory simpleOccurrence) {
//				return simpleOccurrence.getYear()!=null && simpleOccurrence.getYear()==1998;
				if(simpleOccurrence._getLongitude() == null || simpleOccurrence._getLatitude() == null)
					return false;
				else
					return poly.contains(new Point2D(simpleOccurrence._getLongitude(), simpleOccurrence._getLatitude()));
			}
		};

		Float[] bbox = polyt.getBoundingBox();

		try {
			tmp = database.query(
					AQLQueries.getString("QueryDriver.4")
					, null, null, Occurrence.class);
		} catch (ArangoDBException e) {
			throw new DatabaseException(e.getMessage());
		}
		out = new OccurrenceIterator(tmp, new OccurrenceFilter[] {geoFilter});
		return out;
	}
*/


/*
	@Override
	public Iterator<Inventory> findInventoriesContainedIn(String geoJsonPolygon, OccurrenceFilter filter) throws FloraOnException {
		Iterator<Inventory> out;
		final PolygonTheme polyt = new PolygonTheme(geoJsonPolygon);
		final Polygon poly = polyt.iterator().next().getValue();

		OccurrenceFilter geoFilter = new OccurrenceFilter() {
			@Override
			public boolean enter(Inventory inventory) {
//				return simpleOccurrence.getYear()!=null && simpleOccurrence.getYear()==1998;
				if(inventory._getLongitude() == null || inventory._getLatitude() == null)
					return false;
				else
					return poly.contains(new Point2D(inventory._getLongitude(), inventory._getLatitude()));
			}
		};

		Float[] bbox = polyt.getBoundingBox();


		try {
			out = driver.getOccurrenceDriver().getInventoriesOfMaintainer(null, null, null);
		} catch (DatabaseException e) {
			throw new DatabaseException(e.getMessage());
		}
		out = new InventoryIterator(out, new OccurrenceFilter[] {geoFilter});
		return out;
	}
*/

}
