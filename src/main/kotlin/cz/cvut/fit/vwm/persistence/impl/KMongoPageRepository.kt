package cz.cvut.fit.vwm.persistence.impl

import com.mongodb.client.model.UpdateOptions
import cz.cvut.fit.vwm.model.Page
import cz.cvut.fit.vwm.persistence.MongoConfiguration
import cz.cvut.fit.vwm.persistence.PageRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.projection

class KMongoPageRepository : PageRepository {

    val collection: CoroutineCollection<Page>
    val asyncDatabase: CoroutineDatabase

    init {
        asyncDatabase = MongoConfiguration.newClient().getDatabase("vwm")
        collection = asyncDatabase.getCollection("pages")
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

    override suspend fun getPageRank(iteration: Int): Map<String, Double> {

        return collection.projection(
            Page::url,
            Page::pageRank
        ).toList().associate { it.first as String to (it.second?.get(20) ?: 0.0) }
//        return collection.projection(
//            Page::url,
//            Page::pageRank.colProperty.memberWithAdditionalPath(iteration.toString())
//        ).toList().associate { it.first as String to (it.second ?: 0.0) }
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

            it.outlinks.map { outlink ->
                collection.updateOne(Page::url eq outlink, inc(Page::pageRank.colProperty.memberWithAdditionalPath(pageRankIteration.toString()), pg), UpdateOptions().upsert(true))
            }
        }.collect()
    }

    override suspend fun alterByDamping(pageRankIteration: Int, skip: Long, limit: Long) {
        collection.find().skip(skip.toInt()).limit(limit.toInt()).toFlow().map {
            collection.updateOne(
                Page::url eq it.url, set(SetTo(Page::pageRank.colProperty.memberWithAdditionalPath(pageRankIteration.toString()), 0.15 + 0.85 * it.pageRank[pageRankIteration])),
                UpdateOptions().upsert(true)
            )
        }.collect()
    }

    override suspend fun findOne(url: String): Page {
        return collection.findOne(Page::url eq url) ?: Page(url)
    }
}

