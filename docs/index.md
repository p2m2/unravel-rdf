[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://forge.inrae.fr/p2m2/unravel-rdf/-/blob/master/LICENSE)
[![Forge INRAE](https://img.shields.io/badge/forge-INRAE-blue)](https://forge.inrae.fr/p2m2/unravel-rdf)

# unravel-rdf

**unravel-rdf** is a JavaScript/TypeScript library for building interactive RDF exploration sessions against SPARQL endpoints and RDF data sources.
It is implemented in Scala.js and published as an npm package.

Developed within the [P2M2](https://www6.inrae.fr/p2m2) community as part of FAIR knowledge extraction workflows for metabolomics and semantic data integration.

---

## CDN — use directly in a browser

No npm, no bundler required:

```html
<!-- Latest stable -->
<script src="https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/latest/unravel-rdf.min.js"></script>
<script>
  const { SWDiscovery, SWDiscoveryConfiguration, URI } = window.UnravelRdf

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
</script>
```

Available CDN versions: [cdn/versions.json](https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/versions.json)

---

## npm

```bash
npm install @p2m2/unravel-rdf
```

```js
import { SWDiscovery, SWDiscoveryConfiguration, URI } from '@p2m2/unravel-rdf'
```

---

## Documentation

- [User documentation](user_docs.md)
- [Configuration](user_docs_configuration.md)
- [Building blocks](user_docs_building_block.md)
- [Transactions](user_docs_transaction.md)
- [Debug](user_docs_debug.md)
- [FORUM example](user_docs_forum_example.md)

---

## Key concepts

### Fluent query DSL

Replace verbose SPARQL with a navigable session object:

```js
SWDiscovery(config)
  .something("study")
    .isA(URI("metabolights:Study"))
    .datatype(URI("rdfs:label"), "label")
  .select("study", "label")
  .commit()
  .raw()
  .then(response => console.log(response))
```

### Serializable sessions

```js
// Save session state
const saved = session.getSerializedString()

// Restore later — full session, ready to query
const restored = SWDiscovery().setSerializedString(saved)
```

### Lazy pagination

```js
session
  .selectByPage("study", "label")
  .then(([totalCount, fetchPage]) => {
    fetchPage(0).then(page => renderTable(page))
  })
```

---

## Technical basis

Built with [Scala.js](https://www.scala-js.org/) on top of the JavaScript RDF ecosystem:

- [`@comunica/query-sparql`](https://comunica.dev/) — SPARQL query execution
- [`n3`](https://github.com/rdfjs/N3.js) — RDF parsing and in-memory store
- [`rdfxml-streaming-parser`](https://github.com/rubensworks/rdfxml-streaming-parser.js) — RDF/XML support
- [`axios`](https://axios-http.com/) — HTTP transport

---

## Source & issues

- Source: [forge.inrae.fr/p2m2/unravel-rdf](https://forge.inrae.fr/p2m2/unravel-rdf)
- Issues: [forge.inrae.fr/p2m2/unravel-rdf/-/issues](https://forge.inrae.fr/p2m2/unravel-rdf/-/issues)

## Authors

- O. Filangi — P2M2, IGEPP, Rennes (INRAE)

## License

MIT