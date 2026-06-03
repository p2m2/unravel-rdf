---
title: Unravel RDF
---

# Unravel RDF

Unravel RDF is a JavaScript/TypeScript library for building **interactive RDF exploration sessions** against SPARQL endpoints and RDF data sources. It is implemented in Scala.js and published as an npm package via [Forge INRAE](https://forge.inrae.fr/p2m2/unravel-rdf).

Unravel RDF is developed within the [P2M2](https://www6.inrae.fr/p2m2) community and contributes to FAIR knowledge extraction workflows for metabolomics and semantic data integration, as part of the [MetaboHUB](https://www.metabohub.fr/) national infrastructure.

---

## Why Unravel RDF

Standard SPARQL libraries execute queries. Unravel RDF manages the **state of an RDF exploration session**.

A session in Unravel RDF is a navigable, serializable object: you traverse a graph of RDF nodes, attach metadata to each node, apply filters, paginate results lazily, and at any point serialize the entire session to a string — to store it, share it as a URL, or restore it later for undo/redo.

This makes it natural to build interfaces where users navigate a knowledge graph step by step without writing any SPARQL.

---

## Installation

### npm

```bash
npm install @p2m2/unravel-rdf
```

### CDN

```html
<!-- Latest stable -->
<script src="https://p2m2.pages.forge.inrae.fr/unravel-rdf/cdn/latest/unravel-rdf.js"></script>

<!-- Pinned version (recommended for production) -->
<script src="https://p2m2.pages.forge.inrae.fr/unravel-rdf/cdn/v1.2.3/unravel-rdf.js"></script>
```

Available versions: [cdn/versions.json](https://p2m2.pages.forge.inrae.fr/unravel-rdf/cdn/versions.json)

---

## Core concepts

### Session as a serializable object

Every Unravel RDF session can be serialized to a string and restored exactly. This is the foundation for undo/redo, shareable URLs, and persistent sessions.

```js
import { SWDiscovery, SWDiscoveryConfiguration, URI } from '@p2m2/unravel-rdf'

const config = SWDiscoveryConfiguration
  .init()
  .sparqlEndpoint("https://endpoint.example.org/sparql")

const session = SWDiscovery(config)
  .something("study")
    .isA(URI("ex:Study"))
    .datatype(URI("rdfs:label"), "label")

// Serialize
const saved = session.getSerializedString()

// Restore — full session, ready to query
const restored = SWDiscovery().setSerializedString(saved)
```

### Automatic lazy pagination

Unravel RDF handles OFFSET/LIMIT automatically. You get a total count and an on-demand page fetcher:

```js
session
  .selectByPage("study", "label")
  .then(([totalCount, fetchPage]) => {
    console.log(`${totalCount} results`)
    fetchPage(0).then(page => renderTable(page))
  })
```

### Query progression

Long-running queries emit progress events — connect them directly to a loading indicator:

```js
session
  .select("study", "label")
  .commit()
  .progression((percent) => updateProgressBar(percent))
  .requestEvent((event) => console.log("event:", event))
  .raw()
  .then(response => render(response))
```

### Node decorations

Attach arbitrary metadata to any node in the query graph. Decorations are preserved through serialization:

```js
session
  .something("compound")
    .setDecoration("label", "Chemical compound")
    .setDecoration("attributes", JSON.stringify({ visible: true }))

// Read back
const label = session.getDecoration("label")
```

### Graph traversal

Traverse the internal query graph client-side to build dynamic UI elements such as column definitions or filter panels:

```js
const columns = session
  .browse((node, depth) => {
    if (!node.decorations?.attributes) return []
    return Object.values(JSON.parse(node.decorations.attributes))
      .filter(attr => attr.visible)
      .map(attr => ({ label: attr.label, field: attr.id }))
  })
  .filter(cols => cols.length > 0)
  .flat()
```

### Fluent query DSL

Build SPARQL patterns with a readable API — no string concatenation, no prefix boilerplate:

```js
SWDiscovery(config)
  .prefix("obo",          "http://purl.obolibrary.org/obo/")
  .prefix("metabolights", "https://www.ebi.ac.uk/metabolights/property#")
  .prefix("rdfs",         "http://www.w3.org/2000/01/rdf-schema#")
  .something()
    .set(URI("obo:CHEBI_4167"))
    .isObjectOf(URI("metabolights:Xref"), "study")
    .datatype(URI("rdfs:label"), "label")
  .select("study", "label")
  .commit()
  .raw()
  .then(response => console.log(response.results.bindings))
  .catch(console.error)
```

---

## Session history (undo/redo)

Because sessions are serializable, undo/redo requires no external store:

```js
const history = []

// Push current state
history.push(session.getSerializedString())

// Undo: restore previous state
const previous = SWDiscovery().setSerializedString(history.pop())
```

---

## Supported data sources

Unravel RDF queries RDF data from multiple source types:

- **SPARQL endpoints** — remote HTTP endpoints
- **RDF files** — Turtle, N-Triples, JSON-LD, RDF/XML
- **Inline RDF content** — string-embedded triples
- **Federated sources** — multiple endpoints in a single session

---

## Demo application

[unravel-rdf-queryview](https://github.com/p2m2/unravel-rdf-queryview) is a complete Vue.js + D3.js application built on Unravel RDF. It demonstrates:

- Graph-based RDF exploration with forward/backward property navigation
- Attribute filtering with operators (contains, equals, range comparisons)
- Lazy-paginated result tables driven by `selectByPage`
- Full session history (undo/redo) via serialization
- Strategy pattern for AskOmics and data-driven query modes

Live demo: [https://p2m2.github.io/unravel-rdf-queryview](https://p2m2.github.io/unravel-rdf-queryview)

---

## Further documentation

- [Building blocks](user_docs_building_block) — DSL primitives reference
- [Configuration](user_docs_configuration) — endpoint and source configuration
- [Transactions](user_docs_transaction) — commit, select, pagination
- [Debug](user_docs_debug) — console, logging, source maps
- [Forum example](user_docs_forum_example) — community usage example

---

## License

MIT — [forge.inrae.fr/p2m2/unravel-rdf](https://forge.inrae.fr/p2m2/unravel-rdf)