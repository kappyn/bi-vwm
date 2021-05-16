package cz.cvut.fit.vwm.routes

import cz.cvut.fit.vwm.Controller
import cz.cvut.fit.vwm.view.Styles
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.css.CSSBuilder

fun Application.frontendRoutes() {
    routing {
        homeRoute()
        stylesRoute()
    }
}

fun Route.homeRoute() {
    get("/") {
        call.respondHtml {
            val page = context.parameters["page"]?.toIntOrNull() ?: 1
            if (context.parameters.contains("query"))
                Controller.results(this, context.parameters["query"] as String, page, URLBuilder.createFromCall(call))
            else
                Controller.homePage(this)
        }
    }
}


fun Route.stylesRoute() {
    get("/styles.css") {
        call.respondCss {
            Styles.homeCss(this)
        }
    }
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}



