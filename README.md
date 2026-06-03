# Discovery

Discovery is a JavaScript/TypeScript library for building **interactive RDF exploration sessions** against SPARQL endpoints and RDF data sources.
It is implemented in Scala.js and published as an npm package.

The source code is hosted on [Forge INRAE](https://forge.inrae.fr/p2m2/discovery).  
Discovery is developed within the [P2M2](https://www6.inrae.fr/p2m2) community and contributes to FAIR knowledge extraction workflows for metabolomics and semantic data integration.

---

## Why Discovery

Writing SPARQL from a web application is painful: prefix management, pagination with OFFSET/LIMIT, result binding traversal, federation across endpoints. Discovery replaces all of this with a **fluent query DSL** that models an RDF exploration session as a navigable, serializable object.

The core idea: a Discovery session is not just a query — it is a **stateful graph of RDF nodes** you can traverse, decorate, filter, paginate, serialize, and restore. This makes it natural to build interfaces where users explore a knowledge graph step by step without writing any SPARQL.

---

## Key concepts

### Immutable, serializable sessions

A Discovery session can be serialized to a string and restored exactly:

```js
import { SWDiscovery, SWDiscoveryConfiguration, URI } from '@p2m2/discovery'

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

```bash
npm install @p2m2/discovery
```

### CDN (browser, no build step)

**Latest stable:**
```html
<script src="https://p2m2.pages.forge.inrae.fr/discovery/cdn/latest/discovery.js"></script>
```

**Pinned version (recommended for production):**
```html
<script src="https://p2m2.pages.forge.inrae.fr/discovery/cdn/v1.2.3/discovery.js"></script>
```

Available versions: [cdn/versions.json](https://p2m2.pages.forge.inrae.fr/discovery/cdn/versions.json)

---

## Complete example — query a SPARQL endpoint

```js
import { SWDiscovery, SWDiscoveryConfiguration, URI } from '@p2m2/discovery'

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

[discovery-queryview](https://github.com/p2m2/discovery-queryview) is a full Vue.js + D3.js application built on top of Discovery.  
It demonstrates graph-based RDF exploration with node navigation, attribute filtering, lazy-paginated result tables, and session history — all driven by the Discovery API.

Live demo: [https://p2m2.github.io/discovery-queryview](https://p2m2.github.io/discovery-queryview)

---

## Documentation

Full API documentation: [https://p2m2.pages.forge.inrae.fr/discovery/](https://p2m2.pages.forge.inrae.fr/discovery/)

---

## Build from source

```bash
sbt fastOptJS           # development build with source maps
sbt fullOptJS           # optimized production build (Closure Compiler)
sbt npmPrepareRelease   # assemble target/npm/ for publication
```

---

## Technical basis

Discovery is built with [Scala.js](https://www.scala-js.org/) and integrates the JavaScript RDF ecosystem:
- [`@comunica/query-sparql`](https://comunica.dev/) — SPARQL query execution
- [`n3`](https://github.com/rdfjs/N3.js) — RDF parsing and in-memory store
- [`rdfxml-streaming-parser`](https://github.com/rubensworks/rdfxml-streaming-parser.js) — RDF/XML support
- [`axios`](https://axios-http.com/) — HTTP transport

---

## Docker proxy

A Docker image is available for the Discovery proxy service:

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

---

## License

MIT — [Forge INRAE / p2m2/discovery](https://forge.inrae.fr/p2m2/discovery)
