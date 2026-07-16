# Unravel RDF

Unravel RDF is a JavaScript/TypeScript library, implemented in Scala.js, for building RDF applications that query SPARQL endpoints. Its Lambda Enclosure programming model offers a fluent API for exploring RDF graphs and building SPARQL queries without writing SPARQL directly.

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
  .sparqlEndpoint("https://rdfportal.org/primary/sparql")

UnravelSession(config)
  .prefix("dc", "http://purl.org/dc/elements/1.1/")
  .prefix("knapsack", "http://purl.jp/knapsack/resource#")
  .prefix("sio", "http://semanticscience.org/resource/")
  .prefix("cheminf", "http://semanticscience.org/resource/")

  .something(
    "record",
    record =>
      record
        .out("dc:identifier", "?knapsackId")
        .out(
          "sio:SIO_000008",
          "?molecularEntity",
          molecularEntity =>
            molecularEntity
              .isA("cheminf:CHEMINF_000043")
              .out("sio:SIO_000300", "?molecularEntityName")
        )
  )

  .select(
    "record",
    "knapsackId",
    "molecularEntityName"
  )
  .limit(20)
  .commit()
  .raw()
  .then(console.log)
```

The query above corresponds to the following RDF traversal:

```
record
 ├── dc:identifier ─────────────► ?knapsackId
 └── sio:SIO_000008 ────────────► ?molecularEntity
       ├── rdf:type ────────────► cheminf:CHEMINF_000043
       └── sio:SIO_000300 ──────► ?molecularEntityName
```

No SPARQL string is written manually. The traversal described in the
Lambda Enclosure is translated into an optimized SPARQL query when
`.commit()` is executed.

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
sbt fastOptJS             # development build with source maps
sbt fullOptJS             # optimized production build
sbt npmPrepareRelease     # assemble target/npm/ for npm publication
sbt npmPrepareDebugRelease # assemble target/npm-debug/ with source maps
sbt "fullOptJS; cdnPrepare" # build browser-ready UMD bundle → target/cdn/unravel-rdf.min.js
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