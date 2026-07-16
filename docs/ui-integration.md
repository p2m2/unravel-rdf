---
layout: default
title: UI integration
nav_order: 7
---

# UI integration

Unravel RDF is designed for interactive RDF applications, where query construction, execution, and visualization are tightly integrated.

The API provides mechanisms to connect query processing with user interfaces:

- Execution notifications
- Query cancellation
- Query inspection
- Application metadata through decorations

## Progress notifications

The `progression()` method attaches a callback that receives execution progress information.

```javascript
query
  .progression(value => {
    progressBar.value = value
  })
  .commit()
  .raw()
```

Progress notifications can be used to update:

- Progress bars
- Loading indicators
- Interactive exploration components

## Request lifecycle events

The `requestEvent()` method provides notifications throughout query execution.

```javascript
query
  .requestEvent(event => {
    console.log(event)
  })
  .commit()
  .raw()
```

Events allow applications to react to execution stages such as:

- Query preparation
- SPARQL generation
- HTTP request submission
- Endpoint response
- Result processing

## Cancelling execution

Long-running queries can be interrupted:

```javascript
query.abort()
```

This is useful in interactive applications when users modify their exploration context before a previous request has completed.

## Query inspection

Unravel RDF provides a `browse()` method for inspecting the internal query-construction tree.

The visitor receives:

- The serialized query node
- Its position, or depth, in the query tree

```javascript
const nodes = session.browse(
  (node, depth) => ({
    depth: depth,
    node: node
  })
)
```

This mechanism allows applications to build custom views of the query-construction process, including:

- Query editors
- Graph visualizations
- Debugging interfaces

For example, a D3 visualization can associate each query node with a visual element representing a step in RDF exploration.

## Query decorations

Decorations allow applications to attach metadata to a query construction.

They do not affect SPARQL generation. Instead, they store information required by higher-level application layers.

Set a decoration:

```javascript
session.setDecoration("color", "green")
```

Retrieve a decoration:

```javascript
const color = session.getDecoration("color")
```

Typical use cases include:

- Assigning visualization attributes
- Storing UI identifiers
- Associating display information with query nodes
- Maintaining application-specific metadata

For example, a graph visualization can store a D3 node identifier or display color directly in the query model.

## Building interactive RDF applications

By combining:

- Focus-oriented query construction
- Lazy pagination
- Execution notifications
- Query browsing
- Decorations

Unravel RDF enables responsive interfaces in which RDF exploration, query construction, execution, and visualization remain synchronized.