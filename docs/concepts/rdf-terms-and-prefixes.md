---
layout: default
title: RDF terms and prefixes
parent: Concepts
nav_order: 6
---

# RDF terms and prefixes

RDF data is expressed as triples composed of a subject, predicate, and object.

Unravel-RDF queries use RDF terms to identify resources, properties, literals,
blank nodes, and named graphs.

## RDF term kinds

The main RDF term kinds are:

- **Named nodes**, also called IRIs, identify resources and predicates.
- **Blank nodes** identify local graph resources without a global IRI.
- **Literals** represent values such as text, numbers, dates, and booleans.
- **Variables** represent values matched during query evaluation.

A predicate is normally a named node. An object may be a named node, blank
node, or literal.

## Prefixes

Prefixes make IRI-heavy queries easier to read.

```scala
val schema = namespace("https://schema.org/")
val ex = namespace("https://example.org/")
```

The precise prefix or namespace helper depends on the integration API. Keep
prefix declarations near query definitions or in a shared vocabulary module.

## Literals

Literals may carry a datatype or language tag.

```text
"42"^^xsd:integer
"Bonjour"@fr
```

When filtering or binding values, ensure the operation matches the literal
kind being handled. For example, language operations apply to language-tagged
strings, while numeric comparisons require compatible numeric values.

## Vocabulary modules

Prefer vocabulary constants over raw IRI strings in application code.

```scala
object schema:
  val name = iri("https://schema.org/name")
  val knows = iri("https://schema.org/knows")
```

This keeps predicates discoverable, reduces spelling errors, and centralizes
vocabulary changes.