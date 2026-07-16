---
layout: default
title: Getting started
nav_order: 2
---

# Getting Started

Unravel RDF applications are built around two main concepts:

- **data sources**, which define where RDF data is retrieved from;
- **query graph exploration**, where SPARQL queries are progressively
  constructed by navigating between variables through a focus-oriented
  model.

The following examples introduce Unravel RDF step by step, from a single
RDF file to federated RDF sources, and demonstrate how graph navigation
operations are translated into SPARQL query patterns.

## 1. Querying an RDF file

The simplest configuration uses a remote RDF file as a data source.

```javascript
const config = UnravelConfig
  .init()
  .prefix("ex", "http://example.org/")
  .urlFile("https://example.org/data.ttl")

const query = UnravelSession(config)
  .something("entity", entity =>
    entity.out("ex:hasProperty", "?value")
  )

query
  .select("entity", "value")
  .commit()
```

The `something()` operation introduces the initial focus of the query.
The focus represents the query variable currently explored.

The graph pattern produced is:

```
?entity ex:hasProperty ?value
```

## 2. Querying a SPARQL endpoint

Unravel RDF can directly query remote SPARQL endpoints.

```javascript
const config = UnravelConfig
  .init()
  .prefix("ex", "http://example.org/")
  .sparqlEndpoint(
    "https://example.org/sparql"
  )

UnravelSession(config)
  .something("entity", entity =>
    entity.out("ex:hasProperty", "?value")
  )
  .select("entity", "value")
  .commit()
```

The query construction remains identical. Only the data source changes.

## 3. Federating RDF sources

A query can combine multiple RDF sources, such as remote RDF files and
SPARQL endpoints.

```javascript
const config = UnravelConfig
  .init()
  .prefix("ex", "http://example.org/")
  .urlFile("https://example.org/data.ttl")
  .sparqlEndpoint(
    "https://example.org/sparql"
  )

UnravelSession(config)
  .something("entity", entity =>
    entity.out("ex:hasProperty", "?value")
  )
  .select("entity", "value")
  .commit()
```

Unravel RDF manages query execution over the configured sources, enabling
federated RDF applications.

# Graph navigation with focus

Once a focus has been defined, RDF graph exploration is expressed as a
sequence of navigation steps.

The focus represents the current position in the query graph. Each
navigation operation extends the query pattern from this position while
preserving the previously constructed query.

## Graph navigation primitives

Unravel RDF provides three basic navigation primitives to extend a query
graph from the current focus. Each operation adds a graph pattern while
updating the exploration context.

| Operation | Query graph extension | Description |
|-----------|-----------------------|-------------|
| `out` | `?focus :property ?target` | Extends the query by following an outgoing relationship from the current focus. |
| `in` | `?target :property ?focus` | Extends the query by following an incoming relationship toward the current focus. |
| `traverse` | `{ ?focus :property ?target } UNION { ?target :property ?focus }` | Extends the query by exploring both directions of a relationship. |

These primitives can be combined to progressively construct complex query
graphs while maintaining an explicit focus on the current exploration
position.

## RDF terms and variable references

Navigation operations such as `out`, `in`, and `traverse` interpret their
arguments according to RDF query construction rules.

Arguments representing RDF resources or properties are provided as strings
and can be expressed either as:

- a full URI;
- a prefixed name using a configured prefix (e.g. `ex:property`).

Arguments starting with `?` are interpreted as query variables. For
example, `"?target"` introduces a variable in the generated SPARQL query.

This distinction allows query construction to remain incremental while
keeping explicit references to variables introduced during exploration.

For example:

```javascript
something("source", source =>
  source.out("ex:rel", "?target")
)
```

generates the triple pattern:

```
?source ex:rel ?target
```

A newly introduced variable can immediately become the focus of a nested
exploration step:

```javascript
something("source", source =>
  source.out(
    "ex:rel",
    "?target",
    target => target.in("ex:rel2", "ex:value")
  )
)
```

This produces the following query graph:

```
?source  ex:rel     ?target .
?target  ex:rel2    ex:value .
```

For more complex queries, previously introduced variables can also be
revisited later during query construction by explicitly changing the
current focus.

```javascript
something("source", source =>
  source.out("ex:rel", "?target")
)
.from("target", target =>
  target.in("ex:rel2", "ex:value")
)
```

Both approaches preserve the same query graph while providing different
styles of navigation: local nested exploration or explicit focus
switching.

## Forward navigation

The `out` operation follows outgoing properties from the current focus:

```
?source ex:rel ?target
```

Example:

```javascript
something("source", source =>
  source.out("ex:rel", "?target")
)
```

The focus remains on the source resource while the target variable is
introduced.

## Backward navigation

The `in` operation follows incoming properties toward the current focus:

```
?target ex:rel ?source
```

Example:

```javascript
something("source", source =>
  source.in("ex:rel", "?target")
)
```

This explores resources pointing to the current focus.

## Bidirectional navigation

The `traverse` operation combines both directions:

```
{
  ?source ex:rel ?target
}
UNION
{
  ?target ex:rel ?source
}
```

Example:

```javascript
something("source", source =>
  source.traverse("ex:rel", "?target")
)
```

This allows applications to explore RDF graphs without requiring prior
knowledge of the direction of the relationship.