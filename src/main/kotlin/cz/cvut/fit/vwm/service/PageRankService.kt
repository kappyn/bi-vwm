package cz.cvut.fit.vwm.service

import cz.cvut.fit.vwm.persistence.PageRepository
import cz.cvut.fit.vwm.util.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch


class PageRankService(val pageService: PageService, val pageRepository: PageRepository) {

    companion object {
        var THREADS = 8
        var ITERATIONS = 20
    }


    suspend fun compute(count: Long) {

        repeat(ITERATIONS) { pageRankIteration ->
            Logger.info("Computing pagerank iteration $pageRankIteration")
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
        Logger.info("Pagerank done!!")
    }

    suspend fun get(): Map<String, Double> {
        return pageRepository.getPageRank(ITERATIONS)
    }
}
