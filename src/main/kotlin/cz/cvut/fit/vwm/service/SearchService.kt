package cz.cvut.fit.vwm.service

import org.koin.java.KoinJavaComponent.inject

class SearchService {
    private val similarity by inject<SimilarityService>(SimilarityService::class.java)
    // TODO: inject pagerank service

    fun getResults(query: String) {
        similarity.getResults(query) // placeholder
    }

    fun prepareTextSimilarity(maxPagesToFetch: Int) {
        similarity.instantiate(maxPagesToFetch)
    }

    fun reset() {
        // this will reset pagerank matrix / search index
        // ...
        similarity.clear( )
    }
}