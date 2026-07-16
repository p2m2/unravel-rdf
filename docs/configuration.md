---
layout: default
title: Configuration
nav_order: 3
---

# Configuration

The `UnravelConfig` object defines how Unravel RDF accesses RDF data
sources and how queries are executed.

A configuration is composed of:

- **RDF sources**, defining where RDF data is retrieved from;
- **general settings**, controlling execution behaviour;
- **optional proxy configuration**, for applications requiring an
  intermediate HTTP layer.

A configuration is created using:

```javascript
const config = UnravelConfig.init()
```

Configuration methods return a new configuration object, allowing a fluent
construction style.

## Contents

- [RDF sources](#rdf-sources)
  - [SPARQL endpoint](#sparql-endpoint)
  - [Remote RDF files](#remote-rdf-files)
  - [Inline RDF content](#inline-rdf-content)
- [Multiple RDF sources and federation](#multiple-rdf-sources-and-federation)
- [Proxy configuration](#proxy-configuration)
- [General settings](#general-settings)
  - [Query result cache](#query-result-cache)
  - [Logging level](#logging-level)
  - [Batch processing size](#batch-processing-size)
  - [Pagination size](#pagination-size)
- [Loading a configuration from JSON](#loading-a-configuration-from-json)

---

# RDF sources

Each RDF source is identified by a unique identifier and describes how RDF
data can be accessed.

Unravel RDF supports three types of RDF sources:

- remote SPARQL endpoints;
- remote RDF files;
- inline RDF content.

Multiple sources can be combined in a single configuration, enabling
federated RDF querying.

---

## SPARQL endpoint

A SPARQL endpoint can be configured using `sparqlEndpoint()`.

```javascript
const config = UnravelConfig
                .init()
                .sparqlEndpoint("https://example.org/sparql")
```

The source is configured with:

| Parameter | Description |
|-----------|-------------|
| URL | SPARQL endpoint URL |
| mimetype | Automatically set to `application/sparql-query` |

Optional parameters:

| Parameter | Description |
|-----------|-------------|
| `method` | HTTP method (`POST`, `POST_ENCODED`, `GET`) |
| `auth` | Authentication mode (`basic`, `digest`, `bearer`, `proxy`) |
| `login` | Authentication login |
| `password` | Authentication password |
| `token` | Authentication token |

Example:

```javascript
const config = UnravelConfig
                .init()
                .sparqlEndpoint("https://dbpedia.org/sparql","POST")
```

---

## Remote RDF files

Remote RDF files can be queried using `urlFile()`.

```javascript
const config = UnravelConfig
                .init()
                .urlFile("https://example.org/data.ttl","text/turtle")
```

Parameters:

| Parameter | Description |
|-----------|-------------|
| `filename` | URL of the RDF resource |
| `mimetype` | RDF serialization format |

Supported RDF formats depend on the RDF parser configuration. Typical
values include:

| Format | MIME type |
|--------|-----------|
| Turtle | `text/turtle` |
| RDF/XML | `application/rdf+xml` |
| JSON-LD | `application/ld+json` |
| N-Triples | `application/n-triples` |

Authentication parameters are identical to SPARQL endpoints.

---

## Inline RDF content

Small RDF datasets can be directly embedded into the configuration using
`rdfContent()`.

```javascript
const rdf = `
@prefix ex: <http://example.org/>.
ex:entity ex:name "Example".
`

const config = UnravelConfig
  .init()
  .rdfContent(rdf,"text/turtle")
```

This mode is useful for:

- examples;
- unit tests;
- small static datasets.

---

# Multiple RDF sources and federation

Several sources can be combined in the same configuration.

```javascript
const config = UnravelConfig
                .init()
                .urlFile("https://example.org/data.ttl")
                .sparqlEndpoint("https://example.org/sparql")
```

During query execution, Unravel RDF can combine these sources to build
federated RDF applications.

---

# Prefixes

Prefixes are not part of `UnravelConfig`. They are attached to the query
builder:

```javascript
UnravelSession(config)
  .prefix("ex","http://example.org/")
```

---

# Proxy configuration

A proxy can be configured when RDF endpoints cannot be accessed directly
from the client application.

```javascript
const config = UnravelConfig
  .proxy("https://example.org/proxy")
```

The HTTP method can optionally be specified:

```javascript
const config = UnravelConfig
  .proxy("https://example.org/proxy","post")
```

---

# General settings

General settings control query execution behaviour.

## Query result cache

Enable or disable query result caching:

```javascript
config.setCache(true)
```

| Value | Description |
|-------|-------------|
| `true` | Enable cache |
| `false` | Disable cache |

Default:

```
true
```

---

## Logging level

The logging verbosity can be configured with:

```javascript
config.setLogLevel("debug")
```

Available levels:

```
trace
debug
info
warn
error
all
off
```

Default:

```
warn
```

---

## Batch processing size

The batch size used during datatype processing operations can be customized:

```javascript
config.setSizeBatchProcessing(150)
```

Default:

```
150
```

---

## Pagination size

The number of results returned per page can be configured with:

```javascript
config.setPageSize(20)
```

This value is used by paginated queries such as `selectByPage()`.

Default:

```
10
```

---

# Loading a configuration from JSON

A configuration can also be reconstructed from a JSON representation:

```javascript
const config =
  UnravelConfig.setConfigString(jsonConfiguration)
```

This enables configuration persistence and reuse between application
sessions.