---
layout: default
title: Examples
nav_order: 5
---

## KNApSAcK ;

```javascript
const { UnravelConfig, UnravelSession } = window.UnravelRdf

const config = UnravelConfig
.init()
.sparqlEndpoint("https://rdfportal.org/primary/sparql")

UnravelSession(config)
.prefix("dc", "http://purl.org/dc/elements/1.1/")
.prefix("knapsack", "http://purl.jp/knapsack/resource#")
.prefix("sio", "http://semanticscience.org/resource/")
.prefix("cheminf", "http://semanticscience.org/resource/")

.something(
    "record",
    record => record
    .out("dc:identifier", "knapsackId")
    .out(
        "sio:SIO_000008",
        "molecularEntity",
        molecularEntity => molecularEntity
        .isA("cheminf:CHEMINF_000043")
        .datatype(
            "sio:SIO_000300",
            "molecularEntityName"
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
    console.error("Unable to query the RDF Portal endpoint", error)
})
```

# FORVM ;


# Wikidata