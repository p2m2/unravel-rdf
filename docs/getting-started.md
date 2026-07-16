---
layout: default
title: Getting started
nav_order: 2
---

# Getting started

This guide shows the minimal workflow for querying RDF with Unravel RDF:

1. Configure an RDF data source
2. Create a query session
3. Describe a graph pattern
4. Select variables and execute the query

```javascript
const config = UnravelConfig
  .init()
  .prefix("ex", "http://example.org/")
  .urlFile("https://example.org/data.ttl")

const query = UnravelSession(config)
  .something("entity", entity =>
    entity.out("ex:hasProperty", "?value")
  )
  .select("entity", "value")
  .commit()
```

This query selects RDF resources connected to a value through
`ex:hasProperty`.

```sparql
?entity ex:hasProperty ?value
```

`something()` introduces the initial query variable and establishes the
first exploration focus. `out()` adds an RDF graph pattern, while
`select()` defines the variables returned by the query.

## Next steps

- See [Query model](query-model.html) to learn graph navigation, focus
  switching, filters, and variable transformations.
- See [Configuration](configuration.html) to configure prefixes, RDF files,
  SPARQL endpoints, default graphs, and named graphs.
- See [Execution](execution.html) to retrieve and process query results.