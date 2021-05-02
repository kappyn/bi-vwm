package cz.cvut.fit.vwm.model

import kotlinx.serialization.Serializable

@Serializable
data class Page(val url: String, val inlinks: Int, val outlinks: Int, val title: String, val perex: String)
