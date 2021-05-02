package cz.cvut.fit.vwm.service

import cz.cvut.fit.vwm.SimilarityModule
import cz.cvut.fit.vwm.model.WebDocument
import org.apache.lucene.document.Document

class SimilarityService(private val similarityModule: SimilarityModule) {
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

    fun getResults(query: String) {
        similarityModule.printResults(similarityModule.querySearch(query), query)
    }

    fun clear() {
        similarityModule.deleteIndex()
    }
}