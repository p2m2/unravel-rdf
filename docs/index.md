---
layout: default
title: Documentation
nav_order: 1
---

# Unravel RDF Documentation

Unravel RDF is a JavaScript/TypeScript framework for building applications
over RDF knowledge graphs. It provides a focus-oriented programming model
for constructing and integrating SPARQL queries into interactive web
applications.

## Contents

- [Getting started](getting-started)
- [Configuration](configuration)
- [Query model](query-model)
- [Query execution](query-execution)
- [Query persistence](query-persistence)
- [Query debugging](query-debugging)
- [UI integration](ui-integration)
- [Examples](examples)

## Why Unravel RDF?

Unravel RDF provides a graph-oriented programming model for building
SPARQL queries incrementally. Instead of writing query strings,
developers describe graph traversals through a fluent API based on
successive navigation steps.

The library provides:

- **Incremental query construction**, based on a focus-oriented graph
  exploration model. Query patterns are progressively refined by moving
  the focus between variables in the RDF graph, introducing new variables,
  and revisiting previously explored contexts. Following principles similar
  to Query-based Faceted Search (QFS) approaches such as [Sparklis](https://github.com/sebferre/sparklis), Unravel
  RDF turns graph exploration strategies into programmable query workflows.
- **Flexible query configuration**, including prefixes, default and named
  graphs, and multiple RDF data sources. Queries can seamlessly combine
  local RDF files and remote SPARQL endpoints, enabling federated
  querying.
- **Built-in debugging**, exposing both the incremental query
  construction tree and the generated SPARQL query.
- **Lazy execution and pagination**, where query pages are generated on
  demand and executed only when requested, avoiding unnecessary endpoint
  calls.
- **Execution progress notifications**, allowing applications to react
  to each stage of query processing and easily integrate progress indicators into the user interface.
- **Query serialization**, enabling query persistence, later
  reconstruction, and the creation of permanent links to application
  states.

Together, these features allow developers to manipulate query objects
rather than SPARQL strings, while Unravel RDF manages query generation,
execution, and interaction with the user interface.