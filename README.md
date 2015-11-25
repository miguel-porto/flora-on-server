# Flora-On server
Java server to manage and query online biodiversity databases, including a "fluid" taxonomy system, species traits and habitats, and species occurrences.

**This project is currently under "intense" development and is not usable as it stands now**
## What it is (or aims to be)
It is a fully equipped system for building biodiversity-data websites, providing powerful querying services and data upload and management services.
It offers tools to query and manage taxonomy, traits, ecology and occurrence data.

All the data is stored in a graph (using [ArangoDB](http://www.arangodb.com/)), which confers it great advantages over classic relational databases.
For example, taxonomy needs not follow a strictly hierarchical model, and you can have, for instance, species which are part of other species (when the former is not currently accepted);
or you can group certain populations in non-taxonomic nodes, for instance, when some populations of a given species differ in a certain trait but are not formally recognized as an independent taxon on its own.

## Installation
1. Download and install ArangoDB
2. Clone Flora-On into a local folder
3. In the console, compile with Maven: `mvn package appassembler:assemble`
4. And type `./run` to launch Flora-On shell
5. In Flora-On shell, you can type `\sampledata` to load some sample data on the portuguese plant species, to get it working.
6. Directly type any query in the Flora-On shell to fetch matching species.

A web-based admin is underway.

Stay tuned for updates!

## Example output of a query
This is an example of Flora-On console for the compound query sentence "shrub rose flowers opposite leaves near:38.56 -8.5"
```
flora-on> arbusto flores rosa folhas opostas perto:38.56 -8.5
[GeoPointParser] Entering with query [arbusto flores rosa folhas opostas perto:38.56 -8.5]
[GeoPointParser] Parsing perto:38.56 -8.5
[GeoPointParser] Found 1358 results here
[GeoPointParser] Left unparsed: [arbusto flores rosa folhas opostas]
[Matcher] Query: "arbusto flores rosa folhas opostas"
[Matcher] No matches
[Matcher] Query: "arbusto flores rosa folhas"
[Matcher] No matches
[Matcher] Query: "flores rosa folhas opostas"
[Matcher] No matches
[Matcher] Query: "arbusto flores rosa"
[Matcher] No matches
[Matcher] Query: "flores rosa folhas"
[Matcher] No matches
[Matcher] Query: "rosa folhas opostas"
[Matcher] No matches
[Matcher] Query: "arbusto flores"
[Matcher] No matches
[Matcher] Query: "flores rosa"
[Matcher] Found 1 matches
        * Match: attribute; rank: null; matches: Flores rosa
[Matcher] Query: "folhas opostas"
[Matcher] Found 1 matches
        * Match: attribute; rank: null; matches: Folhas opostas
[Matcher] Query: "arbusto"
[Matcher] Found 1 matches
        * Match: attribute; rank: null; matches: Arbusto ou subarbusto
[Fetcher] Fetch: "flores rosa"
[Fetcher] Found 430 results.
[Fetcher] Fetch: "folhas opostas"
[Fetcher] Found 623 results.
[Fetcher] Fetch: "arbusto"
[Fetcher] Found 311 results.
___________________________________________________________________________________________________________________________________________________________________________________________
| Count| Key          | RelTypes                  | Name                           | Matches                                                                                               |
|==========================================================================================================================================================================================|
| 1    | 2485955795273| [OBSERVED_IN, HAS_QUALITY]| Calamintha nepeta subsp. nepeta| [attribute/2490922047817, attribute/2490898913609, specieslist/2543013009737, attribute/2491022645577]|
| 1    | 2487979088201| [OBSERVED_IN, HAS_QUALITY]| Lonicera etrusca               | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577, specieslist/2587369019721]|
| 1    | 2488013494601| [OBSERVED_IN, HAS_QUALITY]| Dianthus langeanus             | [attribute/2490922047817, attribute/2490898913609, specieslist/2545645132105, attribute/2491022645577]|
| 1    | 2488505211209| [OBSERVED_IN, HAS_QUALITY]| Calluna vulgaris               | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577, specieslist/2579778902345]|
| 1    | 2489194846537| [OBSERVED_IN, HAS_QUALITY]| Phlomis purpurea               | [attribute/2490922047817, attribute/2490898913609, specieslist/2574484183369, attribute/2491022645577]|
5 results.
[0.912 sec]
```

