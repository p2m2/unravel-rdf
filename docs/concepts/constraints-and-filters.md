---
layout: default
title: Constraints and filters
parent: Concepts
nav_order: 4
---

# Constraints and filters

Filters restrict the solution bindings accepted by a query.

They are used after a graph pattern has produced candidate values and before
those values are returned or used by later query operations.

## When to use filters

Use a filter when the constraint applies to a value already reached by the
query, for example:

- A numeric comparison
- A date boundary
- A language tag
- Text matching
- A datatype check
- Equality or inequality with another value

```scala
val englishLabels =
  session.query { resource =>
    resource
      .out(rdfs.label)
      .filter(_.lang === "en")
      .select()
  }
```

This query only retains label values whose language is English.

## Filters do not navigate

A filter does not follow a predicate and does not introduce a new graph edge.
It only removes solutions that do not satisfy its expression.

```scala
resource
  .out(schema.name)
  .filter(_.contains("RDF"))
```

The query still navigates only through `schema:name`; the filter restricts the
name values produced by that navigation.

## Filter placement

Place a filter as close as possible to the value it constrains. This improves
readability and makes the relationship between a graph pattern and its
condition explicit.

```scala
person
  .out(schema.birthDate)
  .filter(_ >= startDate)
  .filter(_ < endDate)
```

## API reference

This page explains filter semantics. The complete set of expression functions,
operators, overloads, and supported RDF term types belongs in the
[Filters API reference](../reference/query-dsl/filters.html).