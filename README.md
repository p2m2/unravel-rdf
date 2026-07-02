# Unravel RDF

> Lambda Enclosure is the current programming model of Unravel RDF:
> a closure-based, graph-first way to build SPARQL queries from RDF graphs.

Unravel RDF is a JavaScript/TypeScript library for building **interactive RDF exploration sessions** against SPARQL endpoints and RDF data sources.
It is implemented in Scala.js and published as an npm package.

The source code is hosted on [Forge INRAE](https://forge.inrae.fr/p2m2/unravel-rdf).  
Unravel RDF is developed within the [P2M2](https://www6.inrae.fr/p2m2) community and contributes to FAIR knowledge extraction workflows for metabolomics and semantic data integration.

---

## Why Unravel RDF

Writing SPARQL from a web application is painful: prefix management, pagination with OFFSET/LIMIT, result binding traversal, federation across endpoints. Unravel RDF replaces all of this with a **fluent query DSL** that models an RDF exploration session as a navigable, serializable object.

The core idea: an Unravel RDF session is not just a query — it is a **stateful graph of RDF nodes** you can traverse, decorate, filter, paginate, serialize, and restore. This makes it natural to build interfaces where users explore a knowledge graph step by step without writing any SPARQL.

---

## Key concepts

### Lambda Enclosure in practice

Example of a Lambda Enclosure traversal on MetaNetX:

```js
UnravelSession(configMetaNetX)
  .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
  .something(
    "subject",
    subject => subject.out(
      "?rel",
      "CHEBI:106243"
    )
  )
  .select("subject", "rel")
  .limit(3)
  .commit()
  .raw()
  .then(response => {
    for (const row of response.results.bindings) {
      console.log(row["subject"].value, row["rel"].value)
    }
  });
```

Here:

- `something("subject", subject => ...)` creates a **lambda enclosure** around the current focus `subject`.
- Inside the closure, `out("?rel", "CHEBI:106243")` searches for RDF triples where `subject` is connected to `CHEBI:106243` by some relation bound to `?rel`.
- `select("subject", "rel")` turns this traversal into a query projection.
- `limit(3).commit().raw()` executes the resulting query and returns the raw SPARQL JSON response.

### Immutable, serializable sessions

A Unravel RDF session can be serialized to a string and restored exactly:

```js
import { SWDiscovery, SWDiscoveryConfiguration, URI } from '@p2m2/unravel-rdf'

const config = SWDiscoveryConfiguration
    .init()
    .sparqlEndpoint("https://metabolights.semantic-metabolomics.fr/sparql")

const session = SWDiscovery(config)
    .something("study")
    .isA(URI("metabolights:Study"))
    .datatype(URI("rdfs:label"), "label")

// Serialize — store in URL, localStorage, database
const saved = session.getSerializedString()

// Restore later — full session, ready to query
const restored = SWDiscovery().setSerializedString(saved)
```

This enables undo/redo, shareable URLs, and persistent sessions with no extra infrastructure.

### Automatic lazy pagination

No manual OFFSET/LIMIT management:

```js
session
  .selectByPage("study", "label")
  .then(([totalCount, fetchPage]) => {
    console.log(`${totalCount} results`)

    // Fetch page 0 on demand
    fetchPage(0).then(page => renderTable(page))
  })
```

### Query progression and events

Long-running SPARQL queries report progress — useful for updating a loading indicator:

```js
session
  .select("study", "label")
  .commit()
  .progression((percent) => {
    updateProgressBar(percent)
  })
  .requestEvent((event) => {
    console.log("SPARQL event:", event)
  })
  .raw()
  .then(response => render(response))
```

### Node decorations

Attach arbitrary metadata to any node in the query graph:

```js
session
  .something("compound")
    .setDecoration("label", "Chemical compound")
    .setDecoration("attributes", JSON.stringify({ visible: true }))
```

Decorations are preserved through serialization and can be read back with `.getDecoration(key)`.

### Graph traversal

Traverse the internal query graph client-side to build dynamic UI elements:

```js
// Build column definitions from visible node attributes
const columns = session.browse((node, depth) => {
  if (node.decorations?.attributes) {
    return Object.values(JSON.parse(node.decorations.attributes))
      .filter(attr => attr.visible)
      .map(attr => ({ label: attr.label, field: attr.id }))
  }
  return []
}).filter(cols => cols.length > 0).flat()
```

### Session history (undo/redo)

Because sessions are serializable, undo/redo is straightforward:

```js
// Push current state to history
history.push(session.getSerializedString())

// Restore previous state
const previous = SWDiscovery().setSerializedString(history.pop())
```

---

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
<script src="https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/v1.2.3/unravel-rdf.min.js"></script>
```

All exports are available under the global `window.UnravelRdf`:

```html
<script src="https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/latest/unravel-rdf.min.js"></script>
<script>
  const { SWDiscovery, SWDiscoveryConfiguration, URI } = window.UnravelRdf

  const config = SWDiscoveryConfiguration
    .init()
    .sparqlEndpoint("https://metabolights.semantic-metabolomics.fr/sparql")

  SWDiscovery(config)
    .something("study")
      .isA(URI("metabolights:Study"))
      .datatype(URI("rdfs:label"), "label")
    .select("study", "label")
    .commit()
    .raw()
    .then(console.log)
</script>
```

Available versions: [cdn/versions.json](https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/versions.json)

---

## Complete example — query a SPARQL endpoint

```js
import { SWDiscovery, SWDiscoveryConfiguration, URI } from '@p2m2/unravel-rdf'

const config = SWDiscoveryConfiguration
  .init()
  .sparqlEndpoint("https://metabolights.semantic-metabolomics.fr/sparql")

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
      const study = response.results.bindings[i]["study"].value
      const label = response.results.datatypes["label"][study][0].value
      console.log(study + " --> " + label)
    }
  })
  .catch(console.error)
```

---

## Demo application

[unravel-rdf-queryview](https://github.com/p2m2/unravel-rdf-queryview) is a full Vue.js + D3.js application built on top of Unravel RDF.  
It demonstrates graph-based RDF exploration with node navigation, attribute filtering, lazy-paginated result tables, and session history — all driven by the Unravel RDF API.

Live demo: [https://p2m2.github.io/unravel-rdf-queryview](https://p2m2.github.io/unravel-rdf-queryview)

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

### CDN bundle

`cdnPrepare` produces a self-contained UMD bundle suitable for direct `<script>` inclusion:

1. Compiles Scala.js to CommonJS (`fullOptJS`)
2. Assembles `target/npm/` with all npm dependencies declared
3. Runs `npm install` in `target/npm/` to resolve the dependency tree
4. Bundles everything with webpack into `target/cdn/unravel-rdf.min.js`

The bundle exposes all exports under `window.UnravelRdf` in the browser.

To test locally:

```bash
sbt "fullOptJS; cdnPrepare"
cd target/cdn
python3 -m http.server 8080
# open http://localhost:8080/test.html
```

---

## Technical basis

Unravel RDF is built with [Scala.js](https://www.scala-js.org/) and integrates the JavaScript RDF ecosystem:
- [`@comunica/query-sparql`](https://comunica.dev/) — SPARQL query execution
- [`n3`](https://github.com/rdfjs/N3.js) — RDF parsing and in-memory store
- [`rdfxml-streaming-parser`](https://github.com/rubensworks/rdfxml-streaming-parser.js) — RDF/XML support
- [`axios`](https://axios-http.com/) — HTTP transport

---

## License

MIT — [Forge INRAE / p2m2/unravel-rdf](https://forge.inrae.fr/p2m2/unravel-rdf)