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
This is an example of Flora-On console for the query sentence "shrub rose flowers opposite leaves"
```
flora-on> arbusto flores rosa folhas opostas
[GeoPointParser] Entering with query [arbusto flores rosa folhas opostas]
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
_________________________________________________________________________________________________________________________________________________________________
| Count| Key          | RelTypes              | Name                                | Matches                                                                    |
|================================================================================================================================================================|
|      | 2485528369481| [HAS_QUALITY, PART_OF]| Dianthus cintranus subsp. cintranus | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2485536168265| [HAS_QUALITY, PART_OF]| Dianthus laricifolius subsp. marizii| [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2485955795273| [HAS_QUALITY]         | Calamintha nepeta subsp. nepeta     | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2485967264073| [HAS_QUALITY]         | Micromeria graeca subsp. graeca     | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2485994330441| [HAS_QUALITY]         | +Thymus villosus                    | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2485994920265| [HAS_QUALITY, PART_OF]| Thymus villosus subsp. lusitanicus  | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2485996296521| [HAS_QUALITY, PART_OF]| Thymus villosus subsp. villosus     | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2486772898121| [HAS_QUALITY]         | Disphyma crassifolium               | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2487110867273| [HAS_QUALITY]         | Nerium oleander                     | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2487979088201| [HAS_QUALITY]         | Lonicera etrusca                    | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2488013494601| [HAS_QUALITY]         | Dianthus langeanus                  | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2488015526217| [HAS_QUALITY]         | Dianthus lusitanus                  | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2488143124809| [HAS_QUALITY]         | Cistus albidus                      | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2488144369993| [HAS_QUALITY]         | Cistus crispus                      | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2488505211209| [HAS_QUALITY]         | Calluna vulgaris                    | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489194846537| [HAS_QUALITY]         | Phlomis purpurea                    | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489235282249| [HAS_QUALITY]         | Teucrium chamaedrys                 | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489245243721| [HAS_QUALITY]         | Teucrium salviastrum                | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489250683209| [HAS_QUALITY]         | Thymbra capitata                    | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489253173577| [HAS_QUALITY]         | Thymus caespititius                 | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489254418761| [HAS_QUALITY]         | Thymus camphoratus                  | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489258154313| [HAS_QUALITY]         | Thymus lotocephalus                 | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489654712649| [HAS_QUALITY]         | Antirrhinum cirrhigerum             | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2489657203017| [HAS_QUALITY]         | Antirrhinum linkianum               | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
|      | 2490837375305| [HAS_QUALITY]         | Fagonia cretica                     | [attribute/2490922047817, attribute/2490898913609, attribute/2491022645577]|
25 results.
[1.009 sec]
```

