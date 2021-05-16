package cz.cvut.fit.vwm.util

import com.google.common.collect.TreeMultimap

class TopnTreeMultimap<K : Comparable<K>, V>(
    keyComparator: Comparator<in K>,
    valueComparator: Comparator<in V>,
    private val maxSize: Int,
    val underlying: TreeMultimap<K, V> = TreeMultimap.create(keyComparator, valueComparator)
) {

    private val DEBUG = true

    fun put(newKey: K, newValue: V): Boolean {
        var added = false
        if (underlying.size() > maxSize) throw RuntimeException("Code error, should never happen.")
        if (underlying.size() == maxSize) {

            val currentMinKey = underlying.keys().last()
            if (newKey <= currentMinKey) {
                if (DEBUG) {
                    println(
                        "Ignore the put element: " + newKey + " : " + newValue
                    )
                }
            } else {
                added = underlying.put(newKey, newValue)
                if (added) {
                    underlying.remove(underlying.keys().last(), underlying.values().last())
                }
            }
        } else {
            added = underlying.put(newKey, newValue)
        }
        return added
    }

    fun values(): MutableCollection<V> {
        return underlying.values()
    }

    companion object {

        fun <K : Comparable<K>, V> create(
            keyComparator: Comparator<K>,
            valueComparator: Comparator<V>,
            maxSize: Int
        ): TopnTreeMultimap<K, V> {
            return TopnTreeMultimap(keyComparator, valueComparator, maxSize)
        }
    }
}
