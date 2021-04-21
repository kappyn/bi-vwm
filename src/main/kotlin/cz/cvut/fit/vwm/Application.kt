package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.Controller.homePage
import cz.cvut.fit.vwm.Controller.results
import cz.cvut.fit.vwm.model.WebDocument
import cz.cvut.fit.vwm.persistence.PageRepository
import cz.cvut.fit.vwm.persistence.impl.KMongoPageRepository
import cz.cvut.fit.vwm.service.PageService
import cz.cvut.fit.vwm.view.Styles.home
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
import org.apache.lucene.document.Document
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.logger.SLF4JLogger


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    // TODO: move to appropriate place (needed this instance to be accessible across all endpoints)
    var lucene: SimilarityModule = SimilarityModule("similarity");

    install(Locations) {
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(Koin) {
        SLF4JLogger()
        modules(pageRepositoryModule)
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

            // For each crawl, you need to add some seed urls. These are the first URLs that are fetched and then the crawler starts following links
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

        post("/init") {
            try {
                val aDoc: Document = lucene.createDocumentIndex(
                    WebDocument(
                        "1",
                        "Test 1",
                        "Entropie je veličina s velkým významem, neboť umožňuje formulovat druhou hlavní větu termodynamiky, a vyjádřit kvantitativně nevratnost tepelných pochodů. Tuto skutečnost vyjadřuje princip růstu entropie."
                    )
                )
                val bDoc: Document = lucene.createDocumentIndex(
                    WebDocument(
                        "2",
                        "Test 2",
                        "Celková entropie izolované soustavy dvou těles různých teplot tedy roste. Jsou-li teploty dosti blízké, nebo jsou-li tělesa od sebe dostatečně dobře izolována, může být změna entropie libovolně malá. V takové soustavě prakticky nedochází k výměně tepla a soustava je blízká tepelné rovnováze. Děje, které probíhají v takové soustavě, lze považovat za vratné, a jejich entropie se téměř nemění. Při vratném adiabatickém ději může být tedy entropie stálá, avšak nikdy nemůže klesat."
                    )
                )
                val cDoc: Document = lucene.createDocumentIndex(
                    WebDocument(
                        "3",
                        "Test 3",
                        "Stručně řečeno je entropie střední hodnota informace jednoho kódovaného znaku. Míra entropie souvisí s problematikou generování sekvence náhodných čísel (případně pseudonáhodných čísel), protože sekvence naprosto náhodných čísel by měla mít maximální míru entropie. Shannonova entropie také tvoří limit při bezeztrátové kompresi dat, laicky řečeno komprimovaná data nelze beze ztráty informace „zhustit“ více, než dovoluje jejich entropie."
                    )
                )
                lucene.addDocumentToIndex(aDoc)
                lucene.addDocumentToIndex(bDoc)
                lucene.addDocumentToIndex(cDoc)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        post("/similarity") {
            try {
                val query = context.parameters["q"] ?: "none"
                lucene.printResults(lucene.querySearch(query))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        post("/clear") {
            try {
                lucene.deleteIndex()
            } catch (e: Exception) {
                e.printStackTrace()
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

val pageRepositoryModule = module {
    single { PageService(get()) }
    single<PageRepository> { KMongoPageRepository() }
}