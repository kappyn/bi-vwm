package cz.cvut.fit.vwm.service

import cz.cvut.fit.vwm.SimilarityModule
import cz.cvut.fit.vwm.model.SearchResult
import cz.cvut.fit.vwm.model.WebDocument
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.apache.lucene.document.Document

class SimilarityService(private val similarityModule: SimilarityModule, private val pageRankService: PageRankService) {

    fun instantiate(maxPagesToFetch: Int) {
        similarityModule.setDocumentCount(maxPagesToFetch)
    }

    fun createDocument(webDocument: WebDocument): Document {
        return similarityModule.createDocumentIndex(webDocument)
    }

    fun addDocument(luceneDoc: Document) {
        similarityModule.addDocumentToIndex(luceneDoc)
    }

    fun updateChanges() {
        similarityModule.finishIndexing()
    }

    suspend fun getResults(query: String, count: Int, skip: Int): SearchResult {

        val pgjob = GlobalScope.async {
            return@async pageRankService.get()
        }
        val smjob = GlobalScope.async {
            return@async similarityModule.querySearch(query)
        }
        val pg = pgjob.await()
        val sm = smjob.await()
        return similarityModule.getResults(sm, pg, count, skip)
    }

    fun clear() {
        similarityModule.deleteIndex()
    }

    fun printResults(query: String) {
        similarityModule.printResults(similarityModule.querySearch(query), query)
    }
}
