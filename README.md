# Unravel RDF

Unravel RDF is a JavaScript/TypeScript library, implemented in Scala.js, for building web applications on top of RDF knowledge graphs. It provides a fluent API based on the **Lambda Enclosure** programming model, allowing developers to explore RDF graphs and generate SPARQL queries without writing SPARQL manually.

The source code is hosted on [Forge INRAE](https://forge.inrae.fr/p2m2/unravel-rdf), with a mirror available on [GitHub](https://github.com/p2m2/unravel-rdf). 

---

## Quick example

The following example retrieves KNApSAcK Core records together with the
associated molecular entity names from the RDF Portal.

```html
<script src="https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/latest/unravel-rdf.min.js"></script>

<script>
const { UnravelConfig, UnravelSession } = window.UnravelRdf

const config = UnravelConfig
  .init()
  .sparqlEndpoint(
    "https://rdfportal.org/primary/sparql"
  )

UnravelSession(config)
  .something("record", 
     record => record.out("dc:identifier","?id"))
  .select("record", "id")
  .limit(10)
  .commit()
  .raw()
  .then(
    results => console.log(results)
  )
```
## Try It Online

Explore and modify this example directly in [CodePen](https://codepen.io/ofilangi/full/019f69a9-7ccb-74fb-9d29-a0365fe4a3a6).

## Why Unravel RDF?

Unravel RDF provides a graph-oriented programming model for building
SPARQL queries incrementally. Instead of writing query strings,
developers describe graph traversals through a fluent API based on
successive navigation steps.

The library provides:

- **Incremental query construction**, based on a focus-oriented graph
  exploration model. Query patterns are progressively refined by moving
  the focus between variables in the RDF graph, introducing new variables,
  and revisiting previously explored contexts. Following principles similar
  to Query-based Faceted Search (QFS) approaches such as [Sparklis](https://github.com/sebferre/sparklis), Unravel
  RDF turns graph exploration strategies into programmable query workflows.
- **Flexible query configuration**, including prefixes, default and named
  graphs, and multiple RDF data sources. Queries can seamlessly combine
  local RDF files and remote SPARQL endpoints, enabling federated
  querying.
- **Built-in debugging**, exposing both the incremental query
  construction tree and the generated SPARQL query.
- **Lazy execution and pagination**, where query pages are generated on
  demand and executed only when requested, avoiding unnecessary endpoint
  calls.
- **Execution progress notifications**, allowing applications to react
  to each stage of query processing—from query construction to result
  retrieval—and easily integrate progress indicators into the user
  interface.
- **Query serialization**, enabling query persistence, later
  reconstruction, and the creation of permanent links to application
  states.

Together, these features allow developers to manipulate query objects
rather than SPARQL strings, while Unravel RDF manages query generation,
execution, and interaction with the user interface.

## Installation

### npm
Add the `@p2m2` scope to your `.npmrc`:

```ini
@p2m2:registry=https://forge.inrae.fr/api/v4/packages/npm/
```

Then install the package:
    
```bash
npm install @p2m2/unravel-rdf
```

### CDN (browser, no build step)

Load the library directly in any HTML page — no npm, no bundler:

```html
<!-- Latest stable -->
<script src="https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/latest/unravel-rdf.min.js"></script>

<!-- Pinned version (recommended for production) -->
<script src="https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/0.5.4/unravel-rdf.min.js"></script>
```
Available versions: [cdn/versions.json](https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/versions.json)

---

## Documentation

Full API documentation: [https://unravel-rdf-5df20c.pages-forge.inrae.fr](https://unravel-rdf-5df20c.pages-forge.inrae.fr/)

---

## Build from source

```bash
sbt fastOptJS               # development build with source maps
sbt fullOptJS               # optimized production build
sbt npmPrepareRelease       # assemble target/npm/ for npm publication
sbt npmPrepareDebugRelease  # assemble target/npm-debug/ with source maps
sbt "fastOptJS;cdnDebugPrepare" # build browser-ready UMD bundle -> target/cdn-debug/unravel-rdf.min.js
sbt "fullOptJS; cdnPrepare"     # build browser-ready UMD bundle -> target/cdn/unravel-rdf.min.js
```

---

## Implementation

Unravel RDF is built with [Scala.js](https://www.scala-js.org/) and integrates the RDFJS query engine Comunica [`@comunica/query-sparql`](https://comunica.dev/)

---

## Licence

Copyright (C) 2026 INRAE

Unravel-RDF is distributed under the terms of the GNU General Public
License, version 3 or any later version (GPL-3.0-or-later).

See the [LICENSE](LICENSE) file for the full license text.
