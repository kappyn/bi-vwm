package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.persistence.MongoConfiguration
import cz.cvut.fit.vwm.persistence.PageRepository
import cz.cvut.fit.vwm.persistence.impl.KMongoPageRepository
import cz.cvut.fit.vwm.routes.apiRoutes
import cz.cvut.fit.vwm.routes.frontendRoutes
import cz.cvut.fit.vwm.service.*
import cz.cvut.fit.vwm.util.Logger
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
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

    applyApplicationConfig(environment.config)
    Logger.LOGGER = environment.log

    install(Locations) {
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(Koin) {
        SLF4JLogger()
        listOf(
            modules(pageRepositoryModule),
            modules(similarityModule)
        )
    }

    frontendRoutes()
    apiRoutes()

}

fun applyApplicationConfig(config: ApplicationConfig) {
    MongoConfiguration.setConfig(config.config("ktor.mongo"))
    CrawlingService.CRAWLERS = Integer.parseInt(config.property("ktor.pagerank.threads").getString())
    PageRankService.THREADS = Integer.parseInt(config.property("ktor.pagerank.threads").getString())
    PageRankService.ITERATIONS = Integer.parseInt(config.property("ktor.pagerank.iterations").getString())
}

val similarityModule = module {
    single { SimilarityService(get(), get()) }
    single { SimilarityModule("similarity") }
}

val pageRepositoryModule = module {
    single { SearchService() }
    single { PageService(get()) }
    single { PageRankService(get(), get()) }
    single<PageRepository> { KMongoPageRepository() }
    single { CrawlingService() }
}
