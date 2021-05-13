package cz.cvut.fit.vwm.persistence.impl

import com.mongodb.client.model.UpdateOptions
import cz.cvut.fit.vwm.model.Page
import cz.cvut.fit.vwm.persistence.MongoConfiguration
import cz.cvut.fit.vwm.persistence.PageRepository
import cz.cvut.fit.vwm.util.Logger
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.projection

class KMongoPageRepository : PageRepository {

    val collection: CoroutineCollection<Page>
    val asyncDatabase: CoroutineDatabase

    init {
        asyncDatabase = MongoConfiguration.getDatabase()
        collection = asyncDatabase.getCollection("pages")

        Logger.info(mul(Page::pageRank.colProperty.memberWithAdditionalPath(5.toString()), 0.85).json)
    }

    override suspend fun updatePage(url: String, outlinks: List<String>, title: String, perex: String) {
        collection.updateOne(Page::url eq url, set(SetTo(Page::outlinksCount, outlinks.size), SetTo(Page::outlinks, outlinks), SetTo(Page::title, title), SetTo(Page::perex, perex.take(150) + "...")), UpdateOptions().upsert(true))
    }

    override suspend fun incrementInlink(url: String) {
        collection.updateOne(Page::url eq url, inc(Page::inlinks, 1), UpdateOptions().upsert(true))
    }

    override suspend fun incrementInlinks(urls: List<String>) {
        collection.bulkWrite(urls.map { url -> updateOne(Page::url eq url, inc(Page::inlinks, 1), UpdateOptions().upsert(true)) })
    }

    override suspend fun findByQuery(query: String): List<Page> {
        return collection.find(Page::perex regex ".*$query.*").limit(10).toList()
    }

    override suspend fun setPageRank(pageRank: Double) {
        collection.updateMany(Page::url.exists(), setValue(Page::pageRank, (0..20).map { if (it == 0) pageRank else 0.0 }))
    }

    @Serializable
    data class PageRank(@Contextual @SerialName("_id") val url: String, val pageRank: List<Double>)

    override suspend fun getPageRank(iteration: Int): Map<String, Double> {

        return collection.withDocumentClass<PageRank>()
            .find()
            .projection(
                fields(
                    include(
                        PageRank::url
                    ),
                    PageRank::pageRank.slice(iteration, 1)
                )
            ).toList().associate { it.url to it.pageRank.first() }
    }

    override suspend fun getAllUrls(): Set<String> {
        return collection.projection(Page::url).toFlow().toSet()
    }

    override suspend fun getPagesCount(): Long {
        return collection.countDocuments()
    }

    override suspend fun computePageRank(pageRankIteration: Int, skip: Long, limit: Long) {
        collection.find().skip(skip.toInt()).limit(limit.toInt()).toFlow().map { it ->
            val pg = it.pageRank[pageRankIteration - 1] / it.outlinksCount
            collection.updateMany(Page::url `in` it.outlinks, inc(Page::pageRank.colProperty.memberWithAdditionalPath(pageRankIteration.toString()), pg))
        }.collect()
    }

    override suspend fun alterByDamping(pageRankIteration: Int) {
        collection.updateMany(
            Page::pageRank.colProperty.memberWithAdditionalPath(pageRankIteration.toString()).exists(),
            mul(Page::pageRank.colProperty.memberWithAdditionalPath(pageRankIteration.toString()), 0.85)

        )
        collection.updateMany(
            Page::pageRank.colProperty.memberWithAdditionalPath(pageRankIteration.toString()).exists(),
            inc(Page::pageRank.colProperty.memberWithAdditionalPath(pageRankIteration.toString()), 0.15)
        )
    }

    override suspend fun findOne(url: String): String {
        return collection.projection(Page::perex, Page::url eq url).first() ?: ""
    }
}

