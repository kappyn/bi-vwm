package cz.cvut.fit.vwm.persistence.impl

import com.mongodb.client.model.UpdateOptions
import cz.cvut.fit.vwm.model.Page
import cz.cvut.fit.vwm.persistence.MongoConfiguration
import cz.cvut.fit.vwm.persistence.PageRepository
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection

class KMongoPageRepository : PageRepository {

    val collection: CoroutineCollection<Page>
    val dbClient: CoroutineClient

    init {
        dbClient = MongoConfiguration.newClient()
        collection = dbClient.getDatabase("vwm").getCollection("pages")
    }


    override suspend fun updatePage(url: String, outlinks: Int, title: String, perex: String) {
        collection.updateOne(Page::url eq url, set(SetTo(Page::outlinks, outlinks), SetTo(Page::title, title), SetTo(Page::perex, perex.take(150) + "...")), UpdateOptions().upsert(true))
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
}
