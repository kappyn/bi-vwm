package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.Controller.homePage
import cz.cvut.fit.vwm.Controller.results
import cz.cvut.fit.vwm.Styles.home
import edu.uci.ics.crawler4j.crawler.CrawlConfig
import edu.uci.ics.crawler4j.crawler.CrawlController
import edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.css.CSSBuilder
import kotlinx.html.body


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {
    }

    install(ContentNegotiation) {
        gson {
        }
    }


    routing {
        get("/") {
            call.respondHtml {
                if (context.parameters.contains("query"))
                    results(this, context.parameters["query"] as String)
                else
                    homePage(this)
            }
        }

        post("/start") {

            val crawlStorageFolder = "./crawl"
            val numberOfCrawlers = 7
            val seed = context.parameters["seed"] ?: "https://cs.wikipedia.org/"

            // Instantiate the controller for this crawl.
            val config = CrawlConfig()
            config.crawlStorageFolder = crawlStorageFolder

            config.maxDepthOfCrawling = Integer.parseUnsignedInt(context.parameters["d"] ?: "1")
            config.maxPagesToFetch = Integer.parseUnsignedInt(context.parameters["p"] ?: "10")

            val pageFetcher = PageFetcher(config)
            val robotstxtConfig = RobotstxtConfig()
            val robotstxtServer = RobotstxtServer(robotstxtConfig, pageFetcher)
            val controller = CrawlController(config, pageFetcher, robotstxtServer)

            // For each crawl, you need to add some seed urls. These are the first
            // URLs that are fetched and then the crawler starts following links
            // which are found in these pages
            controller.addSeed(seed)

            // The factory which creates instances of crawlers.
            val factory: WebCrawlerFactory<DomainCrawler> = WebCrawlerFactory { DomainCrawler() }

            // Start the crawl. This is a blocking operation, meaning that your code
            // will reach the line after this only when crawling is finished.
            controller.startNonBlocking(factory, numberOfCrawlers)

            call.respondHtml {
                body {
                    +"Crawling started"
                }
            }
        }
    }
    routing {
        get("/styles.css") {
            call.respondCss {
                home(this)
            }
        }
    }
}


suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}

