---
layout: default
title: Building queries
parent: Concepts
nav_order: 2
---

# Building queries

Unravel-RDF queries are built by composing graph-navigation operations with
constraints, derived values, and result-selection operations.

The DSL describes *what* data should be matched. Query execution is performed
only after the query has been submitted to a session.

## Core primitives

The core query primitives navigate RDF triples:

- `out(predicate)` follows triples where the current focus is the subject.
- `in(predicate)` follows triples where the current focus is the object.
- `has(predicate, value)` restricts the current focus to resources having a
  matching property value.
- `select()` declares the values returned by the query.

The exact method names and overloads belong in the API reference; this guide
focuses on how the operations compose.

## Navigation example

```scala
val names =
  session.query { person =>
    person
      .out(ex.knows)
      .out(schema.name)
      .select()
  }
```

Conceptually, this query starts from `person`, follows `ex:knows`, then follows
`schema:name` and returns the resulting names.

## Constraints and values

Graph traversal can be refined with two additional operation families:

- **Filters** restrict the solutions accepted by a query.
- **Bindings** compute a value and associate it with a query variable.

Neither operation changes the RDF graph being traversed. They operate on the
variable bindings produced while matching graph patterns.

```scala
val labels =
  session.query { resource =>
    resource
      .out(rdfs.label)
      .filter(_.lang === "en")
      .bind(_.str.toLowerCase, "normalizedLabel")
      .select()
  }
```

See [Constraints and filters](constraints-and-filters.html) and
[Derived values and bindings](derived-values-and-bindings.html) for their
respective semantics.

## Composition

Prefer small, reusable query fragments when a graph pattern is used in several
places. A fragment should describe one semantic concern, such as selecting a
label, following a parent relation, or applying a language constraint.

Keep query execution at the application boundary. This makes it easier to test
and reuse query definitions independently from their data source.