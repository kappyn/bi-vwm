package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.model.WebDocument
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.QueryBuilder
import java.nio.file.Paths
import java.util.*

class SimilarityModule(private val directory: String = "similarity") {
    private var DocCnt: Int
    private val Index: Directory
    private val Analyzer: StandardAnalyzer
    private val QryBuilder: QueryBuilder
    private var IndxWriter: IndexWriter
    private var IndxSearcher: IndexSearcher

    init {
        this.Index = FSDirectory.open(Paths.get(directory))
        this.Analyzer = StandardAnalyzer()
        this.IndxWriter = IndexWriter(Index, IndexWriterConfig(this.Analyzer))
        this.IndxSearcher = IndexSearcher(DirectoryReader.open(this.IndxWriter))
        this.QryBuilder = QueryBuilder(this.Analyzer)
        this.DocCnt = 1000000
        print("Similarity module initialized in \"$directory\".\n")
    }

    @Throws(Exception::class)
    fun createDocumentIndex(doc: WebDocument): Document {
        val retv: Document = Document()
        retv.add(StringField("id", doc.id, Field.Store.YES))
        retv.add(StringField("title", doc.title, Field.Store.YES))
        retv.add(TextField("content", doc.content, Field.Store.NO))
        return retv
    }

    @Throws(Exception::class)
    fun deleteIndex() {
        this.IndxWriter.deleteAll()
        this.IndxWriter.commit()
        print("Lucene index has been deleted.\n")
    }

    @Throws(Exception::class)
    fun addDocumentToIndex(docToAdd: Document) {
        this.IndxWriter.addDocument(docToAdd)
        this.IndxWriter.flush()
        print("Successfully written file " + docToAdd.getField("title").stringValue() + " to the index.\n")
    }

    @Throws(Exception::class)
    fun querySearch(query: String, field: String = "content"): TopDocs {
        // reassign searcher for current "snapshot"
        this.IndxSearcher = IndexSearcher(DirectoryReader.open(this.IndxWriter))

        val q1: Query = this.QryBuilder.createPhraseQuery(field, query)
        val q2: Query = MatchAllDocsQuery() // remove this query if you want listed results above 1.0 score
        val chainQryBldr: BooleanQuery.Builder = BooleanQuery.Builder()
        chainQryBldr.add(q1, BooleanClause.Occur.SHOULD)
        chainQryBldr.add(q2, BooleanClause.Occur.SHOULD)
        return this.IndxSearcher.search(chainQryBldr.build(), this.DocCnt)
    }

    suspend fun getResults(docs: TopDocs, pg: Map<String, Double>, count: Int, skip: Int): List<WebDocument> {
        val list = mutableListOf<WebDocument>()
        val results = TreeMap<Double, WebDocument>(Collections.reverseOrder())
        if (docs.scoreDocs != null && docs.scoreDocs.isNotEmpty()) {
            for (sd: ScoreDoc in docs.scoreDocs) {
                val docRetrieved: Document = this.IndxSearcher.doc(sd.doc);
                print("Score: " + sd.score + " " + docRetrieved.getField("title").stringValue() + " pagerank: " + pg[docRetrieved.get("title")] + " total: " + sd.score * (pg[docRetrieved.get("title")] ?: 0.0) + "\n")
                results[sd.score * (pg[docRetrieved.get("title")] ?: 0.0)] = WebDocument(docRetrieved.get("title"), "", "")
            }
        }
        return results.values.asFlow().drop(skip).take(count).toList(list)
    }

    fun printResults(docs: TopDocs, query: String) {
        print("Query: \"$query\"\nFound results: ${docs.totalHits}\n")
        if (docs.scoreDocs != null && docs.scoreDocs.isNotEmpty()) {
            for (sd: ScoreDoc in docs.scoreDocs) {
                val docRetrieved: Document = this.IndxSearcher.doc(sd.doc);
                print("Score: " + sd.score + " " + docRetrieved.getField("title").stringValue() + "\n")
            }
        } else {
            print("Score docs are empty.\n")
        }
    }

    fun setDocumentCount(pagesToIndex: Int) {
        this.DocCnt = pagesToIndex
    }

    fun finishIndexing() {
        this.IndxWriter.commit()
    }
}

