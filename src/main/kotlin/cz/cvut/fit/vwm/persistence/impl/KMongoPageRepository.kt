package cz.cvut.fit.vwm.persistence.impl

import com.mongodb.client.model.MapReduceAction
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
        collection.updateMany(Page::url.exists(), setValue(Page::pageRank, pageRank))
    }

    override suspend fun getAllUrls(): Set<String> {
        return collection.projection(Page::url).toFlow().toSet()
    }

    override suspend fun getPagesCount(): Long {
        return collection.countDocuments()
    }

    override suspend fun computePageRankMapReduce(iteration: Int): Page? {

        collection.find()
        return collection.mapReduce<Page>(mapFunction(), reduceFunction())
            .action(MapReduceAction.REPLACE)
            .collectionName("pageRank$iteration")
            .first()
    }

    override suspend fun computePageRank(pageRankIteration: Int, skip: Long, limit: Long) {
        val inputCollection = asyncDatabase.getCollection<Page>(if (pageRankIteration == 1) "pages" else "pageRank_" + (pageRankIteration - 1))
        val outputCollection = asyncDatabase.getCollection<Page>("pageRank_$pageRankIteration")
        inputCollection.find().skip(skip.toInt()).limit(limit.toInt()).toFlow().map {
            val pg = it.pageRank / it.outlinksCount

            outputCollection.updateMany(Page::url `in` it.outlinks, inc(Page::pageRank, pg), UpdateOptions().upsert(true))
            outputCollection.updateOne(Page::url eq it.url, set(SetTo(Page::outlinks, it.outlinks), SetTo(Page::outlinksCount, it.outlinksCount), SetTo(Page::previousPageRank, it.pageRank)), UpdateOptions().upsert(true))
        }.collect()
    }

    override suspend fun alterByDamping(pageRankIteration: Int, skip: Long, limit: Long) {
        val pageRankCollection = asyncDatabase.getCollection<Page>("pageRank_$pageRankIteration")
        pageRankCollection.find().skip(skip.toInt()).limit(limit.toInt()).toFlow().map {
            pageRankCollection.updateOne(Page::url eq it.url, set(SetTo(Page::pageRank, 0.15 + 0.85 * it.pageRank)), UpdateOptions().upsert(true))
        }.collect()
    }

    fun mapFunction(): String {
        return """function() {
            var page;
            if (this.value === undefined) {
            page=this;
            } else {
            page=this.value;
            }
    // For each node that is reachable from this node, give it the 
    // appropriate portion of my pagerank
    for (var toNode in page.outlinks) {
        emit(page.outlinks[toNode], {outlinksCount : 0.0
                  , pageRank : 1/page.outlinksCount * page.pageRank
                  , outlinks : {}
                  , diff : 0.0
                  , previousPageRank : 0.0});
    }
    // Pass the previous pagerank and the probability matrix to myself
emit(page._id, {outlinksCount: page.outlinksCount
               , pageRank: 0.0
               , outlinks: page.outlinks
               , outlinksCount: page.outlinksCount
               , diff: 0.0
               , previousPageRank: page.pageRank});

// Reduce won't be called on a key unless there's more than one value for that key
// So this is just an extra emit to make sure that reduce is called
emit(page._id, {outlinksCount: 0.0
               , pageRank : 0.0
               , outlinks : {} 
               , diff : 0.0
               , previousPageRank : 0.0});
}"""
    }

    fun reduceFunction(): String {
        return """function(url, values) {
    var pageRank = 0
    , diff = 0
    , outlinks = [] 
    , previousPageRank = 0 
    , beta = 0.9
    , outlinksCount = 0;
for (var i in values) {
// Retrieve the previous pagerank and the probability matrix
  for (var key in values[i].outlinks) {
    outlinks.push(values[i].outlinks[key]);
    }
    previousPageRank += values[i].previousPageRank;
     // Summation of the pagerank
      pageRank += values[i].pageRank; 
      outlinksCount += values[i].outlinksCount;
       }

if (previousPageRank != 0) {
            diff = Math.abs(previousPageRank - pageRank) / previousPageRank;
        } 
return {"outlinksCount" : outlinksCount
    , "pageRank" : pageRank 
    , "outlinks" : outlinks
    , "diff" : diff
    , "previousPageRank" : previousPageRank};
}"""
    }
}

