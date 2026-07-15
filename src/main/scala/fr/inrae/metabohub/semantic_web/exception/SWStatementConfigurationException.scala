// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

package fr.inrae.metabohub.semantic_web.exception

final case class SWStatementConfigurationException(private val message: String = "",
                                                 private val cause: Throwable = None.orNull) extends Exception(message,cause)
