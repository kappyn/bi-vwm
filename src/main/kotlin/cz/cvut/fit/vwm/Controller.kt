package cz.cvut.fit.vwm

import kotlinx.html.HTML


object Controller {
    fun homePage(html: HTML): HTML = Template.home(html)
    fun results(html: HTML, query: String): HTML {
        
        return Template.results(html, query)
    }
}
