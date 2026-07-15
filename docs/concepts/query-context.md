---
layout: default
title: Query context
parent: Concepts
nav_order: 7
---

# Query context

A query context contains the information required to evaluate an
Unravel-RDF query.

In most applications, the context is provided by an `UnravelSession`, created
from an `UnravelConfig`.

## Configuration

Configuration defines how the application accesses RDF data and which query
backend it uses.

Typical configuration concerns include:

- RDF sources, such as local files, remote files, datasets, or HTTP endpoints
- Backend-specific execution options
- Prefix or vocabulary defaults
- Authentication and HTTP options, when required by remote sources

```scala
val config = UnravelConfig(
  sources = Seq(source)
)
```

Refer to the configuration API for the exact supported options.

## Sessions

A session is the boundary through which queries are created and executed.

```scala
val session = UnravelSession(config)
```

Use one session for related operations that share the same configuration and
data-access settings. Dispose of, close, or otherwise release a session
according to the lifecycle rules of the underlying API.

## Execution

A query definition is separate from its execution:

```scala
val query = session.query { root =>
  root.out(schema.name).select()
}

val result = query.execute()
```

This separation allows applications to compose queries first and evaluate them
only at the point where results are needed.

## Backend abstraction

Unravel-RDF exposes a high-level query abstraction while delegating RDF query
evaluation to the configured backend.

Application code should normally depend on the Unravel-RDF query model rather
than on backend-specific query strings or execution objects. Use backend APIs
directly only when a feature cannot be represented in the high-level DSL.