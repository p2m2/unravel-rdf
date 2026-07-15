[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://forge.inrae.fr/p2m2/unravel-rdf/-/blob/master/LICENSE)
[![Forge INRAE](https://img.shields.io/badge/forge-INRAE-blue)](https://forge.inrae.fr/p2m2/unravel-rdf)


# Unravel RDF

> **Lambda Enclosure** is the programming model of Unravel RDF: a
> closure-based, graph-first approach for building SPARQL queries through
> RDF graph navigation.

**Unravel RDF** is a JavaScript/TypeScript library for building interactive
RDF exploration sessions against SPARQL endpoints and RDF data sources.

It provides a fluent API to describe graph traversals, compose SPARQL queries,
execute them, and inspect or serialize exploration sessions. Unravel RDF is
implemented in Scala.js and published as an npm package.

Developed within the [P2M2](https://www6.inrae.fr/p2m2) community, Unravel RDF
supports FAIR knowledge extraction workflows for metabolomics and semantic data
integration.

---

## CDN — use directly in a browser

No npm installation or bundler is required.

```html
<!-- Latest stable version -->
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
          record => record
            .out("dc:identifier", "knapsackId")
            .out(
              "sio:SIO_000008",
              "molecularEntity",
              molecularEntity => molecularEntity
                .isA("cheminf:CHEMINF_000043")
                .datatype(
                  "sio:SIO_000300",
                  "molecularEntityName"
                )
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

        .then(response => {
          console.log("KNApSAcK molecular entities")
          console.log(response)
        })

        .catch(error => {
          console.error("Unable to query the RDF Portal endpoint", error)
        })
</script>
```

Available CDN versions: [cdn/versions.json](https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/versions.json)

---

## npm

```bash
npm install @p2m2/unravel-rdf
```

```js
import {
  UnravelSession,
  UnravelSessionConfiguration
} from "@p2m2/unravel-rdf"
```

---

## Lambda Enclosure

Lambda Enclosure expresses RDF navigation by enclosing traversal instructions in
a JavaScript function. The function receives the current graph focus and
returns the next traversal expression.

```js
UnravelSession(configMetaNetX)
  .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
  .prefix("mtx", "https://rdf.metanetx.org/chem/")
  .something(
    "node",
    node => node.traverse(
      "?rel",
      "mtx:MNXM586757"
    )
  )
  .select("node", "rel")
  .limit(30)
  .commit()
  .raw()
```

This query starts from the MetaNetX chemical `mtx:MNXM586757`, traverses
matching RDF relations, and selects each reached `node` together with the
corresponding relation `rel`.

---

## Documentation

The documentation is organised around concepts and APIs rather than the former
Discovery terminology.

- [Getting started](docs/getting-started.md)
- [Lambda Enclosure](docs/lambda-enclosure.md)
- [Session API](docs/session-api.md)
- [Query API](docs/query-api.md)
- [Configuration](docs/configuration.md)
- [Execution and transactions](docs/execution.md)
- [Pagination and projections](docs/pagination.md)
- [Serialisation](docs/serialization.md)
- [Debugging](docs/debugging.md)
- [FORUM integration example](docs/forum-example.md)

---

## Key concepts

### Graph-first query construction

Unravel RDF lets applications express a query as successive RDF graph
navigation steps rather than assembling raw SPARQL strings manually.

```js
UnravelSession(config)
  .prefix("metabolights", "https://www.ebi.ac.uk/metabolights/property#")
  .prefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
  .something(
    "study",
    study => study
      .isA("metabolights:Study")
      .datatype("rdfs:label", "label")
  )
  .select("study", "label")
  .commit()
  .raw()
  .then(response => console.log(response))
```

### Sessions and queries

An `UnravelSession` holds the exploration context: prefixes, data-source
configuration, graph navigation steps, and query-building state.

The session produces an `UnravelQuery` when a projection is selected and the
query is committed. Queries can then be executed, inspected, or serialised.

### Serializable exploration state

Sessions can be serialised to preserve an exploration state and restore it
later.

```js
const saved = session.getSerializedString()

const restored = UnravelSession()
  .setSerializedString(saved)
```

### Lazy pagination

Paged selections support incremental loading of large result sets.

```js
session
  .selectByPage("study", "label")
  .then(([totalCount, fetchPage]) => {
    console.log(`Found ${totalCount} results`)

    return fetchPage(0)
  })
  .then(page => renderTable(page))
```

---

## Technical basis

Unravel RDF is built with [Scala.js](https://www.scala-js.org/) and integrates
with the JavaScript RDF ecosystem:

- [`@comunica/query-sparql`](https://comunica.dev/) — SPARQL query execution

---

## Source and issues

- Source: [forge.inrae.fr/p2m2/unravel-rdf](https://forge.inrae.fr/p2m2/unravel-rdf)
- Issues: [forge.inrae.fr/p2m2/unravel-rdf/-/issues](https://forge.inrae.fr/p2m2/unravel-rdf/-/issues)

---

## Authors

- O. Filangi — P2M2, IGEPP, Rennes, INRAE

---

## License

This project is distributed under the [GNU General Public License v3.0](LICENSE).