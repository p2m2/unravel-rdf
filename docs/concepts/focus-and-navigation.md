---
layout: default
title: Focus and navigation
parent: Concepts
nav_order: 3
---

# Focus and navigation

A focus is the current RDF term or query variable in a DSL chain.

Navigation operations transform one focus into another. When a query follows a
predicate, the resulting focus represents the RDF terms reached by that graph
pattern.

## Moving through the graph

Use outgoing navigation when the current focus is the subject of a triple:

```scala
person.out(schema.knows)
```

Use incoming navigation when the current focus is the object of a triple:

```scala
organization.in(schema.memberOf)
```

These operations correspond to traversing RDF statements in opposite
directions.

## Focus chains

Each operation is evaluated relative to the focus returned by the preceding
operation.

```scala
person
  .out(schema.knows)
  .out(schema.name)
```

In this example, the second navigation starts from each resource matched by
`person.out(schema.knows)`, not from the original `person` focus.

## Scope

A query may contain more than one focus. Use named variables, aliases, or the
DSL's scoping operations when a later condition must refer to an earlier value.

For example, a query may retain a person focus while navigating to that
person's name:

```scala
person
  .as("person")
  .out(schema.name)
  .as("name")
```

The API reference defines the available variable and aliasing methods. The
important rule is that a focus is local to the query scope in which it was
introduced.

## Choosing a focus

Use the current focus for operations that naturally apply to the current RDF
term. Introduce or retain another focus only when the query needs to compare,
return, or constrain multiple terms at once.