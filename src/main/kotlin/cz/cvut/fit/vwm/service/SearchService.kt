package cz.cvut.fit.vwm.service

import cz.cvut.fit.vwm.model.WebDocument
import org.koin.java.KoinJavaComponent.inject

class SearchService {
    private val similarity by inject<SimilarityService>(SimilarityService::class.java)
    private val pagerank by inject<PageRankService>(PageRankService::class.java)
    private val pageService by inject<PageService>(PageService::class.java)

    suspend fun getResults(query: String, count: Int = 10, skip: Int = 0): List<WebDocument> {
        return similarity.getResults(query, count, skip).map { pageService.fillDocument(it) }
    }

    fun prepareTextSimilarity(maxPagesToFetch: Int) {
        similarity.instantiate(maxPagesToFetch)
    }

    fun reset() {
        // this will reset pagerank matrix / search index
        // ...
        similarity.clear()
    }
}
