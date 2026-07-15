---
layout: default
title: Architecture
nav_order: 3
---

# Architecture

Unravel RDF is organized into three complementary layers, separating
application development from RDF query construction and execution.

This architecture provides a unified programming interface for building
interactive applications over heterogeneous RDF sources.

![Unravel RDF architecture]({{ site.baseurl }}/images/Unravel-rdf-architecture.png)

---

## Client application

The client application is responsible for the user interface and the
application-specific logic.

It interacts with RDF resources through the Unravel RDF API without
directly writing SPARQL queries.

Typical technologies include:

- HTML
- JavaScript / TypeScript
- Node.js

The main objects exposed to developers are:

- `UnravelConfig`
- `UnravelSession`
- `UnravelQuery`

The application defines the exploration workflow and consumes the
results returned by RDF query execution.

---

## Unravel RDF API

The Unravel RDF API provides a high-level abstraction layer between
client applications and RDF query engines.

It implements a graph-oriented programming model where RDF exploration
is described through a fluent API and a query DSL.

This layer is implemented in Scala.js and exposes a JavaScript/TypeScript
interface.

Main responsibilities include:

- SPARQL endpoint configuration;
- RDF graph navigation;
- query composition;
- variable scope management;
- interactive session handling;
- pagination and result processing.

The API translates graph traversals into executable SPARQL queries.

---

## RDF query execution layer

The RDF query execution layer is responsible for evaluating generated
queries over RDF data sources.

Unravel RDF relies on RDF JavaScript libraries, mainly Comunica, to
provide a unified access mechanism over heterogeneous RDF resources.

Supported data sources include:

- SPARQL HTTP endpoints;
- local RDF files;
- remote RDF resources.

The application developer does not need to manage the details of query
execution, source access, or federation.

---

## Data flow

The typical execution workflow is:

1. The client application defines an RDF exploration session.
2. The Unravel RDF API builds a query representation from graph
   navigation operations.
3. The query is translated into SPARQL.
4. The RDF query execution layer evaluates the query over the selected
   RDF sources.
5. Results are returned to the application for visualization or further
   processing.

This separation enables the development of interactive RDF applications
while keeping the application logic independent from the underlying RDF
infrastructure.