---
layout: default
title: Query debugging
nav_order: 6
---

# Query debugging

Unravel RDF provides a built-in console mode to inspect query construction
and execution.

The `console()` operation exposes the internal representation of the query,
including:

- the query construction tree;
- the current focus position;
- configured RDF sources;
- generated SPARQL requests.

This helps developers understand how incremental graph exploration is
translated into executable SPARQL.

## Inspecting query construction

```javascript
const config = UnravelConfig
  .init()
  .sparqlEndpoint("https://rdfportal.org/primary/sparql")

UnravelSession(config)
  .something("record", record => 
     record.out("dc:identifier","?id"))
  .console()  // Display query construction and generated SPARQL
  .select("record", "id")
  .limit(10)
  .commit()
  .raw()
  .then(
    results => console.log(results)
  )
```

The console displays the query graph before execution:

```
ROOT NODE

CHILDREN
  Something@record
    SubjectOf(
      propertyTerm = dc:identifier,
      objectTerm = ?id
    )

FOCUS NODE
  record

SOURCE
  https://rdfportal.org/primary/sparql
```

It also displays the generated SPARQL request:

```sparql
SELECT *
WHERE {
    ?record dc:identifier ?id .
}
```

The console output provides a direct view of the transformation from a
focus-oriented query construction to a SPARQL query.

This feature is especially useful when developing interactive RDF
applications, where queries are dynamically assembled from user actions.