---
layout: default
title: Concepts
nav_order: 4
---

# Concepts

This section explains the concepts behind Unravel-RDF's high-level query API.

Use these guides to understand how queries are constructed, how RDF graphs are
navigated, and how query values are constrained or transformed. For exact
method signatures, overloads, and supported expression functions, consult the
API reference.

## Start here

- [Programming model](programming-model.html)  
  Learn the roles of configuration, sessions, queries, and focus values.

- [Building queries](building-queries.html)  
  Learn how graph navigation, constraints, bindings, and result selection
  compose into a query.

## Query DSL

- [Focus and navigation](focus-and-navigation.html)  
  Understand the current query focus, RDF graph traversal, aliases, and
  variable scope.

- [Constraints and filters](constraints-and-filters.html)  
  Restrict query solutions with comparisons, language checks, text matching,
  datatype checks, and other expressions.

- [Derived values and bindings](derived-values-and-bindings.html)  
  Compute, name, and reuse derived query values without discarding solutions.

## RDF and execution

- [RDF terms and prefixes](rdf-terms-and-prefixes.html)  
  Work with named nodes, blank nodes, literals, variables, namespaces, and
  vocabulary constants.

- [Query context](query-context.html)  
  Understand configuration, sessions, RDF sources, query execution, and the
  underlying backend.

## Recommended reading order

1. [Programming model](programming-model.html)
2. [Building queries](building-queries.html)
3. [Focus and navigation](focus-and-navigation.html)
4. [Constraints and filters](constraints-and-filters.html)
5. [Derived values and bindings](derived-values-and-bindings.html)
6. [RDF terms and prefixes](rdf-terms-and-prefixes.html)
7. [Query context](query-context.html)