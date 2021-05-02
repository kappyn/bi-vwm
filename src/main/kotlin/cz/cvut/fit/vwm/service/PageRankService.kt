package cz.cvut.fit.vwm.service

import cz.cvut.fit.vwm.persistence.PageRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class PageRankService(val pageService: PageService, val pageRepository: PageRepository) {

    val THREADS = 8

    suspend fun compute(count: Long) {

        for (pageRankIteration in 1..20) {
            val jobs: MutableList<Job> = mutableListOf()
            for (i in 0..THREADS) {
                jobs.add(GlobalScope.launch {
                    pageRepository.computePageRank(pageRankIteration, i * (count / THREADS), count / THREADS)
                })
            }

            for (job in jobs) {
                job.join()
            }
            jobs.clear()

            for (i in 0..THREADS) {
                jobs.add(GlobalScope.launch {
                    pageRepository.alterByDamping(pageRankIteration, i * (count / THREADS), count / THREADS)
                })
            }

            for (job in jobs) {
                job.join()
            }
//            pageRepository.computePageRank(i)
        }
    }
}
