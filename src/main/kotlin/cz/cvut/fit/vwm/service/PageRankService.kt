package cz.cvut.fit.vwm.service

import cz.cvut.fit.vwm.persistence.PageRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch


class PageRankService(val pageService: PageService, val pageRepository: PageRepository) {

    val THREADS = 8

    suspend fun compute(count: Long) {

        repeat(20) { pageRankIteration ->
            val jobs: MutableList<Job> = mutableListOf()
            repeat(THREADS) { i ->
                jobs.add(GlobalScope.launch {
                    pageRepository.computePageRank(pageRankIteration + 1, i * (count / THREADS), count / THREADS)
                })
            }

            jobs.joinAll()
            jobs.clear()

            repeat(THREADS) { i ->
                jobs.add(GlobalScope.launch {
                    pageRepository.alterByDamping(pageRankIteration + 1, i * (count / THREADS), count / THREADS)
                })
            }

            jobs.joinAll()
        }
    }

    suspend fun get(): Map<String, Double> {
        return pageRepository.getPageRank()
    }
}
