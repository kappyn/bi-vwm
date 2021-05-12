package com.google.common.collect

import java.lang.RuntimeException
import java.util.Comparator

class TopnTreeMultimap<K : Comparable<K>, V>(
    keyComparator: Comparator<in K>,
    valueComparator: Comparator<in V>,
    private val maxSize: Int
) : TreeMultimap<K, V>(keyComparator, valueComparator) {

    private val DEBUG = true

    fun put(newKey: K, newValue: V): Boolean {
        var added = false
        if (size() > maxSize) throw RuntimeException("Code error, should never happen.")
        if (size() == maxSize) {
            val keyIterator: Iterator<K> = keySet().iterator()
            val currentMinKey = keyIterator.next()
            if (newKey <= currentMinKey) {
                if (DEBUG) {
                    println(
                        "Ignore the put element: " + newKey + " : " + newValue
                    )
                }
            } else {
                added = super.put(newKey, newValue)
                if (added) {
                    // remove the first element - the min
                    val entryiIterator: MutableIterator<*> = entryIterator()
                    entryiIterator.next()
                    entryiIterator.remove()
                }
            }
        } else {
            added = super.put(newKey, newValue)
        }
        return added
    }

    companion object {
        private const val serialVersionUID = 1L

        fun <K : Comparable<K>, V> create(
            keyComparator: Comparator<K>,
            valueComparator: Comparator<V>,
            maxSize: Int
        ): TopnTreeMultimap<K, V> {
            return TopnTreeMultimap(keyComparator, valueComparator, maxSize)
        }
    }
}