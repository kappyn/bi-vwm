package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.service.SearchService
import cz.cvut.fit.vwm.view.Template
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.HTML
import org.koin.java.KoinJavaComponent.inject


object Controller {

    val ITEMS_PER_PAGE = 10

    val service by inject<SearchService>(SearchService::class.java)

    fun homePage(html: HTML): HTML = Template.home(html)
    fun results(html: HTML, query: String, page: Int = 1, url: URLBuilder): HTML {
        return runBlocking {
            val result = service.getResults(query, ITEMS_PER_PAGE, ITEMS_PER_PAGE * (page - 1))
            return@runBlocking Template.results(html, query, result, page, url)
        }
    }
}
