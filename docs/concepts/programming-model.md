---
layout: default
title: Programming model
parent: Concepts
nav_order: 1
---

# Programming model

Unravel-RDF provides a high-level query API for RDF data.

Instead of writing SPARQL strings directly, applications construct queries
through a composable DSL. The DSL represents graph navigation, constraints,
derived values, and result selection.

A query is evaluated against an RDF source through a session. The session
owns the execution context and delegates query evaluation to the configured
backend.

## Main building blocks

The programming model is based on four concepts:

- A **configuration** defines how RDF data is accessed and how queries are executed.
- A **session** provides an execution context for one or more queries.
- A **query** describes graph patterns and the values to retrieve.
- A **focus** represents the current RDF term or variable in a query chain.

Applications typically create a session, define a query from a starting
focus, then execute that query to obtain results.

## Example shape

```scala
val session = UnravelSession(config)

val query =
  session.query { root =>
    root
      .out(ex.knows)
      .out(schema.name)
      .select()
  }

val results = query.execute()
```

The exact constructors and execution methods are documented in the API
reference. This page introduces the concepts needed to read and compose
queries.

## Query construction

A query is built declaratively. Calling DSL methods does not immediately
retrieve RDF data. Instead, each call adds a new part to the query model.

This separation makes it possible to compose query fragments, inspect the
resulting query, and execute the same query against compatible RDF sources.

## Next steps

- Read [Building queries](building-queries.html) for the core DSL primitives.
- Read [Focus and navigation](focus-and-navigation.html) to understand query
  chaining and variable scope.
- Read [Query context](query-context.html) for session and execution concerns.