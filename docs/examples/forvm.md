---
layout: default
title: FORVM
parent: Examples
nav_order: 2
---

## FORVM    

This example illustrates how Unravel RDF can combine graph navigation, cross-endpoint
enrichment, and execution monitoring in a single workflow. It first extracts
molecular information from a KNApSAcK record, then reuses a normalized InChIKey to
identify related ChEBI compounds and associated MeSH terms.

Because this query performs multiple graph traversals and federates information
across several RDF endpoints, its execution may take a few seconds. To better
understand what happens during execution, open your browser's developer tools
(**F12**) and switch to the **Console** tab before running the example. Unravel
emits detailed request events that let you follow each step of the query execution
in real time.

```javascript
const { UnravelConfig, UnravelSession } = window.UnravelRdf
         
         // Step 1: Configure the SPARQL endpoints used in the query.
         // The first endpoint provides the main metabolomics data.
         // The second endpoint is used to enrich the query with related resources.
         const config = UnravelConfig
           .init()
           .sparqlEndpoint("https://rdfportal.org/primary/sparql")
           .sparqlEndpoint("https://forum.semantic-metabolomics.fr/sparql")
         
         // Step 2: Start a new Unravel session and declare the prefixes
         // that will be used in the graph navigation.
         UnravelSession(config)
           .prefix("dc", "http://purl.org/dc/elements/1.1/")
           .prefix("knapsack", "http://purl.jp/knapsack/resource#")
           .prefix("sio", "http://semanticscience.org/resource/")
           .prefix("cheminf", "http://semanticscience.org/resource/CHEMINF_")
           .prefix("chebi", "http://purl.obolibrary.org/obo/chebi/")
           .prefix("skos", "http://www.w3.org/2004/02/skos/core#")
           .prefix("meshv", "http://id.nlm.nih.gov/mesh/vocab#")
         
           // Step 3: Define the starting point of the query.
           // "record" is the root resource explored in the first endpoint.
           .something("record", record =>
             record
         
               // Retrieve the KNApSAcK identifier.
               .out("dc:identifier", "?knapsackId")
         
               // Follow the link to the molecular entity and extract its label.
               .out("sio:SIO_000008", "?molecularEntity", molecularEntity =>
                 molecularEntity
                   .isA("cheminf:000043")
                   .out("sio:SIO_000300", "?molecularEntityNameKnap")
               )
         
               // Follow the link to the standard InChIKey representation.
               .out("cheminf:000200", "?standardInchikey", standardInchikey =>
                 standardInchikey
                   .isA("cheminf:000059")
                   .out("sio:SIO_000300", "?valueInchikeyKnap", valueInchikeyKnap =>
                     // Convert the InChIKey value to a typed xsd:string literal
                     // so that it can be reused consistently across endpoints.
                     valueInchikeyKnap.bind("inchikeyDtFromKS").strdt("xsd:string")
                   )
               )
           )
         
           // Step 4: Reuse the bound InChIKey in another part of the query.
           // This links the KNApSAcK record to a ChEBI compound with the same InChIKey.
           .from("inchikeyDtFromKS", inchikeyDtFromKS =>
             inchikeyDtFromKS.in("chebi:inchikey", "?compoundChebi", compoundChebi =>
               compoundChebi
         
                 // Explore semantic relations from the matched ChEBI compound.
                 .out("skos:related", "?meshTerm", meshTerm =>
                   // Restrict the related resource to MeSH terms.
                   meshTerm.out("rdf:type", "meshv:Term")
                 )
             )
           )
         
           // Optional: display the debug screen directly in the browser.
           // .showDebugScreen()
         
           // Step 5: Select the variables returned in the result set.
           .select(
             "record",
             "molecularEntityNameKnap",
             "valueInchikeyKnap",
             "?compoundChebi",
             "?meshTerm"
           )
         
           // Step 6: Limit the number of results.
           .limit(1)
         
           // Step 7: Execute the query and request the raw SPARQL JSON response.
           .commit()
           .raw()
         
           // Step 8: Track query progression.
           // This callback can be connected to a progress bar in the user interface.
           .progression(value => {
             progressBar.value = value
           })

           // Step 9: Track request-level events during execution.
           // Open the browser Developer Tools (F12) and look at the Console
           // to follow each stage of the query execution in real time.
           .requestEvent(event => {
             console.log(event)
           })
         
           // Step 10: Process the final response.
           .then(response => {
             console.log("KNApSAcK molecular entities")
             console.log(response)
           })
         
           // Step 11: Handle possible execution errors.
           .catch(error => {
             console.error(
               "Unable to query the RDF Portal endpoint",
               error
             )
           })
```