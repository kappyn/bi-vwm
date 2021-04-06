package cz.cvut.fit.vwm

import kotlinx.html.*

object Template {
    fun home(html: HTML): HTML = html.apply {
        head(this, "PageRank")
        body {
            searchBar(this)
        }
    }

    fun results(html: HTML, query: String): HTML = html.apply {
        head(this, "PageRank: $query")
        body {
            searchBar(this, query)
            ul {
                for (n in 1..10) {
                    li {
                        div {
                            a(href = "/?query=$query") { h2 { +"$query $n" } }
                            p { +"Some description" }
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
