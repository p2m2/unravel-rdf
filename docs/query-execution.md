---
layout: default
title: Query execution
nav_order: 5
---

# Query execution

Once a query graph has been constructed, Unravel RDF provides methods to
select variables, execute queries, and retrieve results.

Query execution follows the same workflow for direct queries and lazy
paginated queries:

1. **selection**: define the variables returned by the query;
2. **commit**: prepare the query execution;
3. **result retrieval**: asynchronously retrieve results.

For small result sets, `select()` can be used to directly retrieve query
results. For large RDF graphs and interactive applications,
`selectByPage()` is recommended. It creates lazy page queries that are
executed independently, reducing unnecessary data transfer and memory
usage.

## Selecting results

The `select()` operation defines the variables returned by the query.

Example:

```javascript
UnravelSession(config)
  .something("record",record => record.out("dc:identifier","?id"))
  .select("record", "id")
  .commit()
  .raw()
  .then(response => {
    console.log(response)
  })
```

The result follows the SPARQL JSON result format:

```javascript
response.results.bindings
```

Each binding contains the selected variables:

```javascript
{
  "record": {
    "type": "uri",
    "value": "http://example.org/resource"
  },
  "id": {
    "type": "literal",
    "value": "identifier"
  }
}
```

## Lazy pagination

For interactive applications and large knowledge graphs, Unravel RDF
provides lazy pagination.

The `selectByPage()` operation does not execute all results at once.
Instead, it returns a `PagedResult` object containing the total number of
matching results and a collection of lazy page queries.

The returned object contains:

| Property | Description |
|----------|-------------|
| `totalCount` | Total number of matching results. |
| `pageSize` | Number of results returned per page. |
| `pageQueries` | Array of lazy queries, one for each page. |

Example:

```javascript
UnravelSession(config)
  .something("record",record => record.out("dc:identifier","?id"))
  .selectByPage("record", "id")
  .then(result => {

    console.log(
      "Total results:",
      result.totalCount
    )

    console.log(
      "Page size:",
      result.pageSize
    )

    result.pageQueries[0]
      .commit()
      .raw()
      .then(response => {
        console.log(response)
      })
  })
```

Each page is executed only when its corresponding query is committed.
This lazy execution model avoids retrieving unnecessary results and is
particularly suited for graphical interfaces and exploratory RDF
applications.

The number of results per page can be configured with:

```javascript
config.setPageSize(20)
```

In this example, every page query retrieves at most 20 results.

## Distinct pagination

`selectDistinctByPage()` provides the same lazy pagination mechanism while
generating a `SELECT DISTINCT` query.

Example:

```javascript
UnravelSession(config)
  .something( "record", record => record.out("dc:identifier","?id"))
  .selectDistinctByPage("id")
  .then(result => {

    result.pageQueries[0]
      .commit()
      .raw()
      .then(response => {
        console.log(response)
      })
  })
```

## Query modifiers

Before execution, queries can be refined using common SPARQL modifiers.

### Limit and offset

```javascript
query
  .limit(20)
  .offset(40)
```

### Ordering

Ascending order:

```javascript
query.orderByAsc("?label")
```

Descending order:

```javascript
query.orderByDesc("?label")
```

### Distinct results

```javascript
query.distinct()
```

## Datatype results

Datatype values can be retrieved during query construction.

Example:

```javascript
query
  .datatype("rdfs:label", "?entity")
```

Datatype results are returned in the response metadata:

```javascript
response.results.datatypes
```

This allows applications to retrieve additional literal values associated
with RDF resources.

