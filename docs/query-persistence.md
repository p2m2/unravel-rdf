---
layout: default
title: Query persistence
nav_order: 5
---

# Query persistence

Unravel RDF queries can be serialized into a string representation and restored later.

This mechanism preserves the complete query-construction state, including the navigation path, selected variables, and query configuration.

Serialized queries can be used for:

- Storing queries in files or databases
- Transferring query states between applications
- Creating persistent URLs or permalinks
- Restoring previous exploration sessions

## Serializing a query

Use `getSerializedString()` to serialize a query:

```javascript
const serializedQuery =
  query.getSerializedString()
```

The returned string can then be stored or transported by an application.

For example, a web application can include the serialized query in a URL parameter:

```text
https://example.org/explorer?query=<serialized-query>
```

## Restoring a query

Use `setSerializedString()` to reconstruct a query from its serialized representation:

```javascript
const restoredQuery =
  query.setSerializedString(serializedQuery)
```

After restoration, the query can be executed normally:

```javascript
restoredQuery
  .commit()
  .raw()
  .then(response => {
    console.log(response)
  })
```

This makes it possible to preserve and share RDF exploration states while retaining the ability to inspect, modify, and execute the reconstructed query.