package fr.inrae.metabohub.semantic_web.node.pm
import org.scalajs.dom.document

case object QueryTreeHtmlStyle {

  def ensureInjected(): Unit = {
    val id = "unravel-tree-style"
    if (document.getElementById(id) != null) return

    val style = document.createElement("style")
    style.id = id
    style.textContent =
      """
      .unravel-tree-panel {
        background: #0b1220;
        color: #e5e7eb;
        border: 1px solid #243041;
        border-radius: 12px;
        padding: 12px;
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 13px;
        line-height: 1.45;
      }

      .unravel-tree-meta {
        margin-bottom: 10px;
        color: #93c5fd;
        font-weight: 700;
      }

      .unravel-tree,
      .unravel-tree ul {
        list-style: none;
        margin: 0;
        padding-left: 18px;
      }

      .unravel-tree-item {
        position: relative;
        margin: 4px 0;
      }

      .unravel-tree-item::before {
        content: "";
        position: absolute;
        left: -10px;
        top: 0;
        bottom: 0;
        width: 1px;
        background: rgba(148, 163, 184, 0.22);
      }

      .unravel-tree-item::after {
        content: "";
        position: absolute;
        left: -10px;
        top: 15px;
        width: 10px;
        height: 1px;
        background: rgba(148, 163, 184, 0.22);
      }

      .unravel-tree > .unravel-tree-item::before {
        top: 18px;
      }

      .unravel-tree-details > summary {
        list-style: none;
      }

      .unravel-tree-details > summary::-webkit-details-marker {
        display: none;
      }

      .unravel-tree-summary,
      .unravel-tree-row {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        padding: 4px 6px;
        border-radius: 8px;
      }

      .unravel-tree-summary:hover,
      .unravel-tree-row:hover {
        background: rgba(255,255,255,0.03);
      }

      .unravel-tree-summary.is-focus,
      .unravel-tree-row.is-focus {
        background: rgba(132, 204, 22, 0.10);
        outline: 1px solid rgba(132, 204, 22, 0.35);
      }

      .unravel-node {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 8px;
      }

      .badge {
        display: inline-block;
        padding: 2px 8px;
        border-radius: 999px;
        font-size: 12px;
        font-weight: 700;
        border: 1px solid transparent;
      }

      .badge.ref {
        background: rgba(96, 165, 250, 0.12);
        color: #93c5fd;
        border-color: rgba(96, 165, 250, 0.25);
      }

      .badge.ref.is-focus-ref {
        background: rgba(132, 204, 22, 0.18);
        color: #bef264;
        border-color: rgba(132, 204, 22, 0.45);
      }

      .kind-root { background: rgba(148,163,184,.14); color: #cbd5e1; border-color: rgba(148,163,184,.28); }
      .kind-something, .kind-somethingvar { background: rgba(34,211,238,.12); color: #67e8f9; border-color: rgba(34,211,238,.28); }
      .kind-subjectof { background: rgba(74,222,128,.12); color: #86efac; border-color: rgba(74,222,128,.28); }
      .kind-objectof { background: rgba(251,146,60,.12); color: #fdba74; border-color: rgba(251,146,60,.28); }
      .kind-bind, .kind-projectionexpression { background: rgba(192,132,252,.12); color: #d8b4fe; border-color: rgba(192,132,252,.28); }
      .kind-count, .kind-projection, .kind-distinct, .kind-reduced { background: rgba(250,204,21,.12); color: #fde047; border-color: rgba(250,204,21,.28); }
      .kind-unionblock, .kind-notblock { background: rgba(244,114,182,.12); color: #f9a8d4; border-color: rgba(244,114,182,.28); }
      .kind-orderbyasc, .kind-orderbydesc, .kind-limit, .kind-offset { background: rgba(129,140,248,.12); color: #a5b4fc; border-color: rgba(129,140,248,.28); }

      .node-detail {
        color: #cbd5e1;
        opacity: 0.95;
        word-break: break-word;
      }
      """
    document.head.appendChild(style)
  }
}