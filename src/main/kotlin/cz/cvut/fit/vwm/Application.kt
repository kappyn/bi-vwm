package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.Controller.homePage
import cz.cvut.fit.vwm.Controller.results
import cz.cvut.fit.vwm.Styles.home
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.css.CSSBuilder

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

