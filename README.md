# Flora-On server
Java server to manage online biodiversity databases, including a "fluid" taxonomy system, species traits and habitats, and species occurrences.

**This project is currently under "intense" development and is not usable as it stands now**
## What it is (or aims to be)
It is a fully equipped system for building biodiversity-data websites, providing powerful querying services and data upload and management services. It offers tools to query and manage taxonomy, traits, ecology and occurrence data.

All the data is stored in a graph (using [ArangoDB](http://www.arangodb.com/)), which confers it great advantages over classic relational databases. For example, taxonomy needs not follow a strictly hierarchical model, and you can have, for instance, species which are part of other species (when the former is not currently accepted); or you can group certain populations in non-taxonomic nodes, for instance, when some populations of a given species differ in a certain trait but are not formally recognized as an independent taxon on its own.

Stay tuned for updates!
