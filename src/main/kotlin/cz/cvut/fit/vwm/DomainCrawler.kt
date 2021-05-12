package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.model.WebDocument
import cz.cvut.fit.vwm.persistence.PageRepository
import cz.cvut.fit.vwm.service.PageRankService
import cz.cvut.fit.vwm.service.PageService
import cz.cvut.fit.vwm.service.SimilarityService
import edu.uci.ics.crawler4j.crawler.WebCrawler
import edu.uci.ics.crawler4j.parser.HtmlParseData
import edu.uci.ics.crawler4j.url.WebURL
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.lucene.document.Document
import org.koin.java.KoinJavaComponent.inject
import java.util.regex.Pattern
import edu.uci.ics.crawler4j.crawler.Page as CrawledPage

class DomainCrawler(private val similarity: SimilarityService) : WebCrawler() {

    val pageService by inject<PageService>(PageService::class.java)
    val pageRankService by inject<PageRankService>(PageRankService::class.java)
    val pageRepository by inject<PageRepository>(PageRepository::class.java)


    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "https://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    override fun shouldVisit(referringPage: CrawledPage?, url: WebURL): Boolean {
        return Companion.shouldVisit(url)
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    override fun visit(page: CrawledPage) {
        val url: String = page.webURL.url
        println("URL: $url")
        if (page.parseData is HtmlParseData) {
            val htmlParseData: HtmlParseData = page.parseData as HtmlParseData
            val text: String = htmlParseData.text
            val html: String = htmlParseData.html
            val outlinks: Set<WebURL> = htmlParseData.outgoingUrls.filter { shouldVisit(it) }.toSet()

            println("Text length: " + text.length)
            println("Html length: " + html.length)
            println("Number of outgoing links: " + outlinks.size)

            val luceneDoc: Document = similarity.createDocument(WebDocument(page.webURL.url, htmlParseData.title, text))

            GlobalScope.launch {
                similarity.addDocument(luceneDoc)
                pageService.updatePage(url, outlinks, htmlParseData.title, text)
                pageService.updateInlinks(outlinks)
            }
        }
    }

    companion object {
        private val FILTERS: Pattern = Pattern.compile(
            ".*(\\.(css|js|gif|jpg|svg"
                    + "|png|mp3|mp4|zip|gz))$"
        )

        private val DOUBLEDOT = Pattern.compile("https://cs\\.wikipedia\\.org/wiki/.*:.*")

        fun shouldVisit(url: WebURL): Boolean {
            val href: String = url.getURL().toLowerCase()

            return (!FILTERS.matcher(href).matches()
                    && href.startsWith("https://cs.wikipedia.org/wiki/")
                    && !DOUBLEDOT.matcher(href).matches()
                    )
        }
    }
}
