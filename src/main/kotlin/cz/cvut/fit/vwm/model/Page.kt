package cz.cvut.fit.vwm.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Page(
    @Contextual @SerialName("_id") val url: String,
    val inlinks: Int = 0,
    val outlinksCount: Int = 0,
    val outlinks: Set<String> = setOf(),
    val title: String = "",
    val perex: String = "",
    val pageRank: List<Double> = listOf(),
    val diff: Double = 0.0
)
