---
layout: default
title: Query debugging
nav_order: 6
---

# Query debugging

Unravel RDF provides two built-in ways to inspect query construction and
execution during development.

- `console()` prints a textual debug view to the browser console.
- `showDebugScreen()` displays the same debugging information in the browser as
  an interactive HTML debug screen.

Both views help inspect:

- the query construction tree;
- the current focus position;
- configured RDF sources;
- generated SPARQL requests.

This helps developers understand how incremental graph exploration is
translated into executable SPARQL.

## Inspecting query construction

### Console output

```javascript
const config = UnravelConfig
  .init()
  .sparqlEndpoint("https://rdfportal.org/primary/sparql")

UnravelSession(config)
  .something("record", record =>
     record.out("dc:identifier", "?id"))
  .console()
  .select("record", "id")
  .limit(10)
  .commit()
  .raw()
  .then(
    results => console.log(results)
  )
```

The console displays the query graph before execution:

```text
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

### Browser debug screen

```javascript
const config = UnravelConfig
  .init()
  .sparqlEndpoint("https://rdfportal.org/primary/sparql")

UnravelSession(config)
  .something("record", record =>
     record.out("dc:identifier", "?id"))
  .showDebugScreen()
  .select("record", "id")
  .limit(10)
  .commit()
  .raw()
  .then(
    results => console.log(results)
  )
```

The debug screen displays the same information directly in the browser in a
dedicated visual view.

This is especially useful when inspecting complex query trees or generated
requests during interactive development.

Both debugging modes provide a direct view of the transformation from a
focus-oriented query construction to a SPARQL query.

These features are especially useful when developing interactive RDF
applications, where queries are dynamically assembled from user actions.