package fr.inrae.metabohub.semantic_web.node.pm

case class PrettyNode(
                       label: String,
                       details: Seq[String] = Seq.empty,
                       children: Seq[PrettyNode] = Seq.empty
                     )