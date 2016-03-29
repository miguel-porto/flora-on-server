# Flora-On server
Database server to manage and query biodiversity databases, including modules for managing taxonomy, species traits, habitats and species occurrences.
The main highlight is the graph-based approach to taxonomy, traits and habitats, which is backed by the power of [ArangoDB](http://www.arangodb.com/) and which confers it great advantages over classic relational databases.

## The new taxonomic concept in Flora-On
The main highlight of Flora-On server is its new approach to taxonomy. All the taxonomic data is stored in a graph, i.e., a collection of "taxonomic" nodes and their relationships. However, here the graph nodes do not represent the formal taxonomic entities, but represent populations (or groups of populations) that may, or may not, be labelled with a formal taxonomic name, i.e., that may, or may not, correspond to formal taxonomic entities. And by *populations* I mean a group of individuals that share something that justifies that they can be grouped - usually traits (but not necessarily).
Under this setting, there can be nodes without a name - for example, to represent certain populations of a species which differ consistently in a given trait, but no one has assigned a name to them yet. Conversely, there can also be nodes with exactly the same name but representing different things, when the interpretation of the name differs between authors.
In this sense, the hierarchy of the "taxonomic" nodes does not need to follow the taxonomic rules, and you can have, for example, species which are "part of" other species, or even genera that are part of species, and so on - it just depends on the interpretation given to those names, i.e., what do those names *actually* represent. You can basically do whatever graph structure you need to represent reality, as long as you stick into the idea that nodes represent *groups of populations*, not formal taxonomic entities.

The purpose of this approach is to greatly simplify the problems faced by biodiversity data managers when matching occurrence data to names, in a constantly changing taxonomic world. You don't need to change anything here when there are taxonomic revisions - better said, you *should not* change anything. Whenever a new combination appears, all you have to do is add it to the graph as a new node, and create the appropriate relationships with existing nodes, so that the Flora-On query algorithms can find their way through the graph - from the occurrence data, traits and habitat data, to the formally accepted taxonomic entities. As long as all the relationships between nodes are correctly defined, Flora-On will know what to show you when you ask for some formal taxonomic name - it just follows all the paths that lead to the queried node.

Basically there are two types of realtionships between nodes: `PART_OF` and `SYNONYM`. If node `A` is `PART_OF` node `B`, it means that the populations represented by node `A` are included in those represented by node `B`. If node `A` is `SYNONYM` of node `B`, it means that they represent exactly the same populations, so they are totally interchangeable - this is used when you need to provide different taxonomic names for the same thing, as long as you are sure that they do represent *exactly* the same thing, otherwise you should use a `PART_OF` relationship appropriately.

As you might expect, all the types of data are also stored in the same graph, i.e., occurrence data, traits, habitats, images, etc., and nodes of these other types of data are connected to the "taxonomic" nodes. Further, both the habitat sub-graph and the traits sub-graph are subjected to their own taxonomy, so you can have habitat `A` which is `PART_OF` habitat `B`, and "taxon" `C` which `EXISTS_IN` habitat `A` and `HAS_ATTRIBUTE` `D`.

This is where all the magic lives - the full integration of all kinds of data in the same graph and all its relationships, so that Flora-On quickly finds and shows the data you are looking for, circunventing all the usual problems associated with the constant taxonomic changes.

Finally, all features are exposed in an API that can easily be used to implement your own biodiversity web site backed by Flora-On server.

**This project is currently under "intense" development and is not usable as it stands now**

## Installation
1. Download and install ArangoDB
2. Install Tomcat
3. Clone Flora-On into a local folder
4. In the console, compile and deploy with Maven: `mvn clean package tomcat:deploy`

## Using

### Flora-On console
In the local folder type `./run` to launch Flora-On shell
In Flora-On shell, you can start by typing `\sampledata` to load some sample data on the portuguese plant species, to get it working.
Then, directly type any query in the Flora-On shell to fetch matching species.

### Flora-On server
Navigate to `http://localhost:8080/floraon/admin` for the management interface.

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

