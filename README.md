# Discovery

Discovery is a Scala.js library for building and executing SPARQL queries against RDF data sources in JavaScript environments.
The source code is hosted on [Forge INRAE](https://forge.inrae.fr/p2m2/discovery).

Discovery is developed within the P2M2 community and contributes to FAIR knowledge extraction workflows for metabolomics and related semantic data integration use cases.

## Overview

Discovery provides a dedicated query DSL to simplify the development of decision-support tools working on RDF and SPARQL-based data.
The project now targets JavaScript runtimes through Scala.js, including browser and Node.js usage.

## Features

- Immutable query-building API
- Querying RDF content from SPARQL endpoints, RDF files, and RDF content
- Federated query support
- Lazy page result handling
- SPARQL query status event subscription
- Query and configuration serialization for transport and reuse
- Query node decoration with additional metadata

## Project location

The canonical project repository is hosted on Forge INRAE:

- [https://forge.inrae.fr/p2m2/discovery](https://forge.inrae.fr/p2m2/discovery)

## Technical basis

Discovery is implemented with Scala.js and integrates JavaScript RDF tooling.
The build currently relies on Scala.js, Scala.js Bundler, and npm dependencies such as `axios`, `@comunica/query-sparql`, `n3`, and `rdfxml-streaming-parser`.

## Documentation

Additional documentation is available here:

- [https://p2m2.github.io/discovery/](https://p2m2.github.io/discovery/)

## Installation

### npm

```bash
npm install @p2m2/discovery
```

The npm package is the primary distribution channel for JavaScript usage.

## Build from source

```bash
sbt fastOptJS
sbt fullOptJS
```

The project is built as a Scala.js library.
The optimized production artifact is generated through the Scala.js linker with CommonJS module output enabled for `fullOptJS`.

## Browser example

```html
<script type="text/javascript" src="https://cdn.jsdelivr.net/gh/p2m2/discovery@develop/dist/discovery-web.min.js"></script>
<script>
  var config = SWDiscoveryConfiguration
    .init()
    .sparqlEndpoint("https://metabolights.semantic-metabolomics.fr/sparql");

  SWDiscovery(config)
    .prefix("obo", "http://purl.obolibrary.org/obo/")
    .prefix("metabolights", "https://www.ebi.ac.uk/metabolights/property#")
    .prefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
    .something()
      .set(URI("obo:CHEBI_4167"))
      .isObjectOf(URI("metabolights:Xref"), "study")
      .datatype(URI("rdfs:label"), "label")
    .select("study", "label")
    .commit()
    .raw()
    .then((response) => {
      for (let i = 0; i < response.results.bindings.length; i++) {
        let study = response.results.bindings[i]["study"].value;
        let label = response.results.datatypes["label"][study][0].value;
        console.log(study + "-->" + label);
      }
    })
    .catch((error) => {
      console.error(error);
    });
</script>
```

## Docker proxy

A Docker image is also available for the discovery proxy service:

```bash
docker run -d --network host -t inraep2m2/service-discovery-proxy:latest
```

```yaml
version: '3.9'
services:
  service-discovery-proxy:
    image: inraep2m2/service-discovery-proxy:latest
    command: ./mill -w app.runBackground --port 8085 --verbose
    network_mode: "host"
    restart: on-failure
```