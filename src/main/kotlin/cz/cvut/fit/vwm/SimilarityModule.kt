package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.model.WebDocument
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.*
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.QueryBuilder
import java.nio.file.Paths

class SimilarityModule(private val directory: String) {
    private val IndexedDocs: Int = 1000000000 // TODO: replace with number of crawled webpages
    private val Index: Directory
    private val Analyzer: StandardAnalyzer
    private var IndxWriter: IndexWriter
    private val IndxSearcher: IndexSearcher
    private val QryBuilder: QueryBuilder

    init {
        this.Index = FSDirectory.open(Paths.get(directory))
        this.Analyzer = StandardAnalyzer()

        // make sure to close every time after using
        this.IndxWriter = IndexWriter(Index, IndexWriterConfig(this.Analyzer))
        this.IndxSearcher = IndexSearcher(DirectoryReader.open(this.IndxWriter))
        this.IndxWriter.close()

        this.QryBuilder = QueryBuilder(this.Analyzer)
        print("Similarity module initialized in \"$directory\".\n")
    }

    @Throws(Exception::class)
    fun createDocumentIndex(doc: WebDocument): Document {
        val retv: Document = Document()
        retv.add(StringField("id", doc.id, Field.Store.YES))
        retv.add(StringField("title", doc.title, Field.Store.YES))
        retv.add(TextField("content", doc.content, Field.Store.NO))
        return retv;
    }

    @Throws(Exception::class)
    fun removeDocumentFromIndex(docID: String) {
        val termToDelete: Term = Term("id", docID)
        this.IndxWriter = IndexWriter(Index, IndexWriterConfig(this.Analyzer))
        this.IndxWriter.deleteDocuments(termToDelete);
        this.IndxWriter.flush();
        this.IndxWriter.commit();
        this.IndxWriter.close()
        print("Document $docID has been removed from the index.\n")
    }

    @Throws(Exception::class)
    fun deleteIndex() {
        this.IndxWriter = IndexWriter(Index, IndexWriterConfig(this.Analyzer))
        this.IndxWriter.deleteAll();
        this.IndxWriter.flush();
        this.IndxWriter.commit();
        this.IndxWriter.close()
        print("Lucene index has been deleted.\n")
    }

    @Throws(Exception::class)
    fun addDocumentToIndex(docToAdd: Document) {
        this.IndxWriter = IndexWriter(Index, IndexWriterConfig(this.Analyzer))
        this.IndxWriter.addDocument(docToAdd)
        this.IndxWriter.flush()
        this.IndxWriter.commit()
        this.IndxWriter.close()
        print("Successfully written file " + docToAdd.getField("title").stringValue() + " to the index.\n")
    }

    @Throws(Exception::class)
    fun querySearch(query: String, field: String = "content"): TopDocs {
        val q1: Query = this.QryBuilder.createPhraseQuery(field, query);

        // remove this query from the chainBuilder if you want only documents that scored > 1.0
        val q2: Query = MatchAllDocsQuery()
        val chainQryBldr: BooleanQuery.Builder = BooleanQuery.Builder();
        chainQryBldr.add(q1, BooleanClause.Occur.SHOULD);
        chainQryBldr.add(q2, BooleanClause.Occur.SHOULD)

        return this.IndxSearcher.search(chainQryBldr.build(), this.IndexedDocs)
    }

    fun printResults(docs: TopDocs) {
        print("Found results: " + docs.totalHits + "\n")
        if (docs.scoreDocs != null && docs.scoreDocs.isNotEmpty()) {
            for (sd: ScoreDoc in docs.scoreDocs) {
                val docRetrieved: Document = this.IndxSearcher.doc(sd.doc);
                print("Score: " + sd.score + " " + docRetrieved.getField("title").stringValue() + "\n")
            }
        } else {
            print("Score docs are empty.\n")
        }
    }
}

