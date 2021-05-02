package cz.cvut.fit.vwm.service

import cz.cvut.fit.vwm.model.Page
import cz.cvut.fit.vwm.persistence.PageRepository
import edu.uci.ics.crawler4j.url.WebURL

class PageService(val repository: PageRepository) {
    suspend fun updatePage(url: String, outlinks: Int, title: String?, text: String) {
        repository.updatePage(url, outlinks, title ?: url, text)
    }

    suspend fun updateInlinks(outlinks: Set<WebURL>) {
        repository.incrementInlinks(outlinks.map { webURL -> webURL.url })
    }

    suspend fun search(query: String): List<Page> {
        return repository.findByQuery(query)
    }
}
