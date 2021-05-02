package cz.cvut.fit.vwm.persistence

import cz.cvut.fit.vwm.model.Page

interface PageRepository {
    suspend fun updatePage(url: String, outlinks: Int, title: String, perex: String)
    suspend fun incrementInlink(url: String)
    suspend fun incrementInlinks(url: List<String>)
    suspend fun findByQuery(query: String): List<Page>
}
