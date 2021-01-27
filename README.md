# wikidata-subgraph-builder
This tool pulls configurable subsets of Wikidata and writes the results to file in JSON-LD format. To do so, it constructs SPARQL and requests data from the Wikidata Query Service API.

It is a work in progress so feel free to contact me with suggestions or requests.

## Setup
Requirements: Scala 2.11 and SBT

All commands run via SBT console

## Commands
- Build subgraph with all nodes connected by a single edge type

`run singleEdge edgeId {-p or --properties} outputFile.jsonld`

`-p` flag optionally includes all node properties (i.e. literal values) and vastly increases runtime

`edgeId` should be formatted such as `P1542` for "[has effect](https://www.wikidata.org/wiki/Property:P1542)", as per Wikidata

- Build subgraph with Wikipedia URLs and Wikidata relationships

`run buildFromWikipediaURLs listOfURLs.txt outputFile.jsonld`

`listOfURLs.txt` should be a list of English Wikipedia page URLs to include, one per line. Mapping from Wikipedia to Wikidata is done via the English Wikipedia API. Mapping errors will be printed to the console.

Only Wikidata relationships between the URLs in this list will be included in the generated subgraph.

Example `listOfURLs.txt`:

```
https://en.wikipedia.org/wiki/Climate_change
https://en.wikipedia.org/wiki/Society
https://en.wikipedia.org/wiki/Social_science
```

# TODO
- Server timeouts for large queries not handled nicely
