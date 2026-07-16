---
layout: default
title: Example - KNApSAcK
nav_order: 8
---

# KNApSAcK example

This example demonstrates how Unravel RDF can be used to explore a real
RDF knowledge graph. The query targets the KNApSAcK database available
through the RDF Portal SPARQL endpoint and retrieves molecular entities
described in the knowledge graph.

The exploration follows a multi-step navigation path:

1. start from a KNApSAcK record;
2. retrieve its identifier;
3. navigate to the associated molecular entity;
4. restrict the entity to chemical entities;
5. extract the molecular entity name.

The same exploration is expressed as an incremental construction of the
query graph using the focus-oriented programming model of Unravel RDF.

```javascript
const { UnravelConfig, UnravelSession } = window.UnravelRdf

const config = UnravelConfig
      .init()
      .sparqlEndpoint("https://rdfportal.org/primary/sparql")
      
      UnravelSession(config)
      .prefix("dc", "http://purl.org/dc/elements/1.1/")
      .prefix("knapsack", "http://purl.jp/knapsack/resource#")
      .prefix("sio", "http://semanticscience.org/resource/")
      .prefix("cheminf", "http://semanticscience.org/resource/CHEMINF_")
    
      .something(
        "record",
        record => record
          .out("dc:identifier", "?knapsackId")
          .out(
            "sio:SIO_000008",
            "?molecularEntity",
            molecularEntity => molecularEntity
              .isA("cheminf:000043")
              .out(
                "sio:SIO_000300",
                "?molecularEntityName"
              )
          )
      )
      .select(
        "record",
        "knapsackId",
        "molecularEntityName"
      )
      .limit(20)
      .commit()
      .raw()
    
      .then(response => {
        console.log("KNApSAcK molecular entities")
        console.log(response)
      })
    
      .catch(error => {
        console.error(
          "Unable to query the RDF Portal endpoint",
          error
        )
      })
```

The query above corresponds to the following RDF traversal:

```
record
 ├── dc:identifier ─────────────► ?knapsackId
 └── sio:SIO_000008 ────────────► ?molecularEntity
       ├── rdf:type ────────────► cheminf:CHEMINF_000043
       └── sio:SIO_000300 ──────► ?molecularEntityName
```

The nested callback illustrates the focus mechanism: after navigating from
a record to a molecular entity, the focus moves to the new variable and
additional constraints can be applied locally.

No SPARQL string is written manually. The Lambda Enclosure model
describes the query graph incrementally, and Unravel RDF translates this
construction into a SPARQL query when `.commit()` is executed.

Try this example interactively on
[CodePen](https://codepen.io/ofilangi/full/019f6427-18c7-70e9-a281-1a1bcdd8f93f).