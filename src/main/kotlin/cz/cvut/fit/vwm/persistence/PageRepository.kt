package cz.cvut.fit.vwm.persistence

import cz.cvut.fit.vwm.model.Page

interface PageRepository {
    suspend fun updatePage(url: String, outlinks: List<String>, title: String, perex: String)
    suspend fun incrementInlink(url: String)
    suspend fun incrementInlinks(urls: List<String>)
    suspend fun findByQuery(query: String): List<Page>

    suspend fun setPageRank(pageRank: Double)
    suspend fun getAllUrls(): Set<String>
    suspend fun computePageRank(pageRankIteration: Int, skip: Long, limit: Long)
    suspend fun getPagesCount(): Long
    suspend fun alterByDamping(pageRankIteration: Int, skip: Long, limit: Long)
}
