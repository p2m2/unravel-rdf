---
layout: default
title: Home
nav_order: 1
---

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://forge.inrae.fr/p2m2/unravel-rdf/-/blob/main/LICENSE)
[![Forge INRAE](https://img.shields.io/badge/forge-INRAE-blue)](https://forge.inrae.fr/p2m2/unravel-rdf)


# Unravel RDF

Unravel RDF is a JavaScript/TypeScript library for building
interactive applications over RDF knowledge graphs.

It provides a fluent API for exploring RDF graphs and generating
SPARQL queries without writing SPARQL directly.

The library is implemented in Scala.js and distributed as an npm package
and a browser-ready CDN bundle.

## Quick example

```javascript
const { UnravelConfig, UnravelSession } = window.UnravelRdf

const config = UnravelConfig
              .init()
              .sparqlEndpoint("https://rdfportal.org/primary/sparql")

UnravelSession(config)
  .something(
    "record",record =>
      record.out(
        "dc:identifier",
        "?id"
      )
  ).select("record", "id")
   .commit()
```