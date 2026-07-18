---
layout: default
title: Getting started
nav_order: 2
---

# Getting started

## Installation

### npm

Add the `@p2m2` registry to your `.npmrc`:

```ini
@p2m2:registry=https://forge.inrae.fr/api/v4/packages/npm/
```

Then install the package:

```bash
npm install @p2m2/unravel-rdf
```
Import the library in your application:

```javascript
import { UnravelConfig, UnravelSession } from "@p2m2/unravel-rdf";
```

### CDN (browser, no build step)

Load the library directly in any HTML page — no npm, no bundler:

```html
<!-- Latest stable -->
<script src="https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/latest/unravel-rdf.min.js"></script>

<!-- Pinned version (recommended for production) -->
<script src="https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/0.5.4/unravel-rdf.min.js"></script>
```

Available versions: [cdn/versions.json](https://unravel-rdf-5df20c.pages-forge.inrae.fr/cdn/versions.json)

After loading the script, the library is available as `window.UnravelRdf`:

```html
<script>
const { UnravelConfig, UnravelSession } = window.UnravelRdf
</script>
```


## Minimal workflow

This guide shows the minimal workflow for querying RDF with Unravel RDF:

1. Configure an RDF data source
2. Create a query session
3. Describe a graph pattern
4. Select variables and execute the query

```javascript

const config = UnravelConfig
  .init()
  .urlFile("https://example.org/data.ttl")

const query = UnravelSession(config)
  .prefix("ex", "http://example.org/")
  .something("entity", entity =>
    entity.out("ex:hasProperty", "?value")
  )
  .select("entity", "value")
  .commit()
  .raw()
  .then(
    results => console.log(results)
  )

```

This query selects RDF resources connected to a value through
`ex:hasProperty`.

```sparql
?entity ex:hasProperty ?value
```

`something()` introduces the initial query variable and establishes the
first exploration focus. `out()` adds an RDF graph pattern, while
`select()` defines the variables returned by the query.


