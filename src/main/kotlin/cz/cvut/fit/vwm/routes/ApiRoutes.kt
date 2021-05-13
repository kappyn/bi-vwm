package cz.cvut.fit.vwm.routes

import cz.cvut.fit.vwm.persistence.PageRepository
import cz.cvut.fit.vwm.service.CrawlingService
import cz.cvut.fit.vwm.service.PageRankService
import cz.cvut.fit.vwm.service.SimilarityService
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.body
import org.koin.ktor.ext.inject


fun Application.apiRoutes() {
    routing {
        startCrawling()
        startPageRank()
        printSimilarityResults()
        clearSimilarityIndex()
    }
}

fun Route.startCrawling() {
    val crawlingService: CrawlingService by inject()
    post("/start") {

        crawlingService.start(context)

        call.respondHtml {
            body {
                +"Crawling started"
            }
        }
    }
}

fun Route.startPageRank() {
    val pageRankService by inject<PageRankService>()
    val pageRepository by inject<PageRepository>()
    post("/pagerank") {

        GlobalScope.launch {
            pageRankService.compute(pageRepository.getPagesCount())
        }
        call.respondHtml {
            body {
                +"Computing started"
            }
        }

    }
}

fun Route.printSimilarityResults() {
    val similarity: SimilarityService by inject()
    post("/similarity") {
        try {
            val query = context.parameters["q"] ?: "none"
            similarity.printResults(query)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun Route.clearSimilarityIndex() {
    val similarity: SimilarityService by inject()
    post("/clear") {
        try {
            similarity.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
