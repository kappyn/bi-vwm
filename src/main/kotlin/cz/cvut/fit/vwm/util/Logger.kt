package cz.cvut.fit.vwm.util

import org.slf4j.Logger as Slf4jLogger

object Logger {
    var LOGGER: Slf4jLogger? = null

    fun info(mess: String) {
        LOGGER?.info(mess)
    }

    fun debug(mess: String) {
        LOGGER?.debug(mess)
    }
}
