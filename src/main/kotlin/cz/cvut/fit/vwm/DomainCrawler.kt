package cz.cvut.fit.vwm

import edu.uci.ics.crawler4j.crawler.Page
import edu.uci.ics.crawler4j.crawler.WebCrawler
import edu.uci.ics.crawler4j.parser.HtmlParseData
import edu.uci.ics.crawler4j.url.WebURL
import java.util.regex.Pattern


class DomainCrawler() : WebCrawler() {
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
    override fun shouldVisit(referringPage: Page?, url: WebURL): Boolean {
        val href: String = url.getURL().toLowerCase()
        return (!FILTERS.matcher(href).matches()
                && href.startsWith("https://cs.wikipedia.org/"))
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    override fun visit(page: Page) {
        val url: String = page.getWebURL().getURL()
        println("URL: $url")
        if (page.getParseData() is HtmlParseData) {
            val htmlParseData: HtmlParseData = page.getParseData() as HtmlParseData

            val text: String = htmlParseData.getText()
            val html: String = htmlParseData.getHtml()

            val links: Set<WebURL> = htmlParseData.getOutgoingUrls()

            println("Text length: " + text.length)
            println("Html length: " + html.length)
            println("Number of outgoing links: " + links.size)
        }
    }

    companion object {
        private val FILTERS: Pattern = Pattern.compile(
            ".*(\\.(css|js|gif|jpg"
                    + "|png|mp3|mp4|zip|gz))$"
        )
    }
}
