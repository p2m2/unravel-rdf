// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.data

import fr.inrae.metabohub.semantic_web.node._
import fr.inrae.metabohub.semantic_web.rdf._

object ApplyAllNode {

  val listNodes : List[Node] = List(
    Root("h1"),
    Something("h1"),
    SubjectOf("h1", URI("http://some"),Var("h1")),
    ObjectOf("h1", URI("http://some"),Var("h1")),
    Value("h1"),
    ListValues(Seq("h1","h2")),
    UnionBlock(),
    NotBlock(),
    isBlank(negation = false,"h1"),
    isLiteral(negation = false,"h1"),
    isURI(negation = false,"h1"),
    Regex("i","i",negation = false,"h1"),
    Contains("s",negation = false,"h1"),
    StrStarts("s",negation = false,"h1"),
    StrEnds("s",negation = false,"h1"),
    Equal("s",negation = false,"h1"),
    NotEqual("s",negation = false,"h1"),
    Inf("s",negation = false,"h1"),
    InfEqual("s",negation = false,"h1"),
    Sup("s",negation = false,"h1"),
    SupEqual("s",negation = false,"h1"),
    DatatypeNode("href",SubjectOf("h1", URI("http://some"),Var("h1")),"h1"),
    SourcesNode("href",Seq(),"h1"),
    OrderByAsc(Seq(),"h1"),
    OrderByDesc(Seq(),"h1"),
    Projection(Seq(),"h1"),
    Distinct("h1"),
    Reduced("h1"),
    Offset(2,"h1"),
    Limit(2,"h1"),
    Bind(SubStr("s","e","h1"),"h1"),
    SparqlDefinitionExpression("s","h1"),
    SubStr("s","e","h1"),
    Replace("a","b","c","h1"),
    Abs("h1"),
    Round("h1"),
    Ceil("h1"),
    Floor("h1"),
    Rand("h1"),
    Datatype("h1"),
    ProjectionExpression(Var("aq"),Count(Seq(Var("q")),distinct = true,"h1"),"h1"),
    Count(Seq(Var("q")),distinct = false,"h1"),
    //CountAll(true,"h1"),
    Str(URI("s"),"h1"),
    Lang(URI("s"),"h1"),
    LangMatches(URI("s"),"h1")
  )
}
