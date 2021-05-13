package cz.cvut.fit.vwm.service

import cz.cvut.fit.vwm.DomainCrawler
import cz.cvut.fit.vwm.persistence.PageRepository
import edu.uci.ics.crawler4j.crawler.CrawlConfig
import edu.uci.ics.crawler4j.crawler.CrawlController
import edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer
import io.ktor.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class CrawlingService {

    val pageRankService by inject<PageRankService>(PageRankService::class.java)
    val pageRepository by inject<PageRepository>(PageRepository::class.java)
    val similarity: SimilarityService by inject(SimilarityService::class.java)

    companion object {
        var CRAWLERS = 8
    }

    fun start(context: ApplicationCall) {
        val controller = configure(context)

        val factory: WebCrawlerFactory<DomainCrawler> = WebCrawlerFactory { DomainCrawler(similarity) }

        // Creates a new instance of IndexWriter for each crawling session
        similarity.instantiate(controller.config.maxPagesToFetch)

        controller.startNonBlocking(factory, CRAWLERS)

        GlobalScope.launch {
            controller.waitUntilFinish()
            similarity.updateChanges()
            val pages = pageRepository.getPagesCount()
            pageRepository.setPageRank(1.0 / pages)
            pageRankService.compute(pages)
        }
    }

    private fun configure(context: ApplicationCall): CrawlController {
        val crawlStorageFolder = "./crawl"
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
        controller.addSeed(seed)
        return controller
    }
}
