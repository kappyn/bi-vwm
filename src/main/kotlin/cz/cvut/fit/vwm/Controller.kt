package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.service.SearchService
import cz.cvut.fit.vwm.view.Template
import kotlinx.coroutines.runBlocking
import kotlinx.html.HTML
import org.koin.java.KoinJavaComponent.inject


object Controller {

    val service by inject<SearchService>(SearchService::class.java)

    fun homePage(html: HTML): HTML = Template.home(html)
    fun results(html: HTML, query: String): HTML {
        return runBlocking {
            val pages = service.getResults(query)
            return@runBlocking Template.results(html, query, pages)
        }
    }
}
