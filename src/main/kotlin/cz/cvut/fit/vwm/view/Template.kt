package cz.cvut.fit.vwm.view

import cz.cvut.fit.vwm.model.WebDocument
import io.ktor.http.*
import kotlinx.html.*

object Template {
    fun home(html: HTML): HTML = html.apply {
        head(this, "PageRank")
        body {
            searchBar(this)
        }
    }

    fun results(html: HTML, query: String, results: List<WebDocument>, page: Int, url: URLBuilder): HTML = html.apply {
        head(this, "PageRank: $query")
        body(classes = "results") {
            searchBar(this, query)
            pagination(this, page, url)
            ul {
                for (page in results) {
                    li {
                        div {
                            a(href = page.id) { h2 { +page.title } }
                            p { +page.content }
                        }
                    }
                }
            }
            pagination(this, page, url)
        }
    }

    private fun pagination(body: BODY, page: Int, url: URLBuilder) = body.apply {
        div(classes = "pagination") {
            for (i in 1..10) {
                div(classes = "paginationItem") {
                    when (i) {
                        page -> div { +"$i" }
                        else -> a(href = url.apply { parameters["page"] = i.toString() }.buildString()) {
                            div { +"$i" }
                        }
                    }
                }
            }
        }
    }

    private fun searchBar(body: BODY, query: String = ""): BODY = body.apply {
        h1(classes = "page-title") {
            +"PageRank"
        }
        form(method = FormMethod.get, action = "/") {
            input(type = InputType.search, name = "query") {
                placeholder = "Zadej dotaz"
                value = query
            }
            input {
                type = InputType.submit
                value = "Hledat"
            }
        }
    }

    private fun head(html: HTML, titleText: String): HTML = html.apply {
        head {
            title {
                +titleText
            }
            link(rel = "stylesheet", href = "/styles.css", type = "text/css")
        }
    }
}
