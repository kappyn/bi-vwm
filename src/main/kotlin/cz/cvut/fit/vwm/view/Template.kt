package cz.cvut.fit.vwm.view

import cz.cvut.fit.vwm.model.WebDocument
import kotlinx.html.*

object Template {
    fun home(html: HTML): HTML = html.apply {
        head(this, "PageRank")
        body {
            searchBar(this)
        }
    }

    fun results(html: HTML, query: String, pages: List<WebDocument>): HTML = html.apply {
        head(this, "PageRank: $query")
        body {
            searchBar(this, query)
            ul {
                for (page in pages) {
                    li {
                        div {
                            a(href = page.id) { h2 { +page.title } }
                            p { +page.content }
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
