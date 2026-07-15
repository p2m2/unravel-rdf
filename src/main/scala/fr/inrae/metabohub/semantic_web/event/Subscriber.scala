// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.event

trait Subscriber[-Event,-Publisher] {
  def notify (pub: Publisher, event: Event): Unit
}
