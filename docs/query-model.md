---
layout: default
title: Query model
nav_order: 4
---

# Query model

Unravel RDF provides a focus-oriented programming model for incrementally
constructing SPARQL queries.

Instead of assembling SPARQL strings, applications describe RDF graph
exploration through navigation steps. Each operation extends the query graph
while retaining a current **focus**: the resource currently being explored.

## Basic query structure

A query typically starts by introducing an initial variable with
`something()`.

```javascript
const query = UnravelSession(config)
  .something("entity", entity =>
    entity.out("ex:hasProperty", "?value")
  )
  .select("entity", "value")
```

This produces the graph pattern:

```sparql
?entity ex:hasProperty ?value
```

The `entity` variable is the initial focus. The call to `out()` adds a
triple pattern from that focus and introduces the `?value` variable.

## RDF terms and variables

Navigation methods accept RDF terms and query variables as strings.

- Use a full URI, such as `"http://example.org/property"`, for an RDF term
- Use a configured prefixed name, such as `"ex:property"`, for an RDF term
- Prefix a name with `?`, such as `"?target"`, for a SPARQL variable

For example:

```javascript
something("source", source =>
  source.out("ex:rel", "?target")
)
```

Generates:

```sparql
?source ex:rel ?target
```

## Navigation primitives

The query graph is extended from the current focus with three navigation
operations.

| Operation | Generated pattern | Description |
|---|---|---|
| `out(property, target)` | `?focus property ?target` | Follows an outgoing relationship |
| `in(property, target)` | `?target property ?focus` | Follows an incoming relationship |
| `traverse(property, target)` | Both patterns in a `UNION` | Explores a relationship in either direction |

### Outgoing navigation

Use `out()` to follow a property from the current focus.

```javascript
something("entity", entity =>
  entity.out("ex:hasPart", "?part")
)
```

```sparql
?entity ex:hasPart ?part
```

### Incoming navigation

Use `in()` to find resources that point to the current focus.

```javascript
something("entity", entity =>
  entity.in("ex:isPartOf", "?parent")
)
```

```sparql
?parent ex:isPartOf ?entity
```

### Bidirectional navigation

Use `traverse()` when the direction of a relationship is unknown or should
not constrain the exploration.

```javascript
something("entity", entity =>
  entity.traverse("ex:relatedTo", "?other")
)
```

```sparql
{
  ?entity ex:relatedTo ?other
}
UNION
{
  ?other ex:relatedTo ?entity
}
```

## Nested exploration

A navigation operation can receive a callback to continue exploration from
the newly introduced variable.

```javascript
something("source", source =>
  source.out("ex:rel", "?target", target =>
    target.out("ex:rel2", "?value")
  )
)
```

This generates:

```sparql
?source ex:rel ?target .
?target ex:rel2 ?value .
```

Nested exploration is convenient when the query follows a direct path through
the RDF graph.

## Switching focus

For larger queries, use `from()` to return to a variable introduced earlier
and make it the current focus again.

```javascript
something("source", source =>
  source.out("ex:rel", "?target")
)
.from("target", target =>
  target.out("ex:rel2", "?value")
)
```

This produces the same graph pattern as the nested example:

```sparql
?source ex:rel ?target .
?target ex:rel2 ?value .
```

Use nested callbacks for local traversal paths and `from()` when returning
to an earlier variable improves the readability of a larger query.

## Filtering values

Filters are built incrementally from the current focus.

```javascript
something("entity", entity =>
  entity.out("ex:name", "?name", name =>
    name.filter.regex("^Arab")
  )
)
```

Equivalent SPARQL:

```sparql
?entity ex:name ?name .
FILTER regex(?name, "^Arab")
```

Available filter operations include:

| Operation | Description |
|---|---|
| `filter.not(expression)` | Negates an expression |
| `filter.isLiteral` | Matches literals |
| `filter.isUri` | Matches URIs |
| `filter.isBlank` | Matches blank nodes |
| `filter.regex(pattern, flags)` | Matches a regular expression |
| `filter.contains(value)` | Tests string containment |
| `filter.strStarts(value)` | Tests a string prefix |
| `filter.strEnds(value)` | Tests a string suffix |
| `filter.equal(value)` | Tests equality |
| `filter.notEqual(value)` | Tests inequality |
| `filter.inf(value)` | Tests lower-than comparison |
| `filter.infEqual(value)` | Tests lower-than-or-equal comparison |
| `filter.sup(value)` | Tests greater-than comparison |
| `filter.supEqual(value)` | Tests greater-than-or-equal comparison |

## Transforming values

Use `bind()` to create a new variable from a SPARQL expression.

```javascript
something("entity", entity =>
  entity.out("ex:name", "?name", name =>
    name.bind("shortName").subStr(0, 10)
  )
)
```

Equivalent SPARQL:

```sparql
?entity ex:name ?name .

BIND(
  SUBSTR(?name, 0, 10)
  AS ?shortName
)
```

Supported transformations include:

| Operation | SPARQL function |
|---|---|
| `.bind(variable).subStr(start, length)` | `SUBSTR()` |
| `.bind(variable).replace(pattern, replacement, flags)` | `REPLACE()` |
| `.bind(variable).abs()` | `ABS()` |
| `.bind(variable).round()` | `ROUND()` |
| `.bind(variable).ceil()` | `CEIL()` |
| `.bind(variable).floor()` | `FLOOR()` |
| `.bind(variable).rand()` | `RAND()` |
| `.bind(variable).datatype()` | `DATATYPE()` |
| `.bind(variable).str()` | `STR()` |

## Building the final query

A query model can combine:

- Navigation through RDF relationships
- Introduction of query variables
- Nested exploration and focus switching
- Filters
- Value transformations

The final SPARQL query is generated when the query is committed for
execution.

```javascript
query
  .select("entity", "value")
  .commit()
```

See [Query execution](query-execution.html) for result retrieval, pagination, progress
events, and query cancellation.