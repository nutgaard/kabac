package no.utgdev.kabac.utils

import no.utgdev.kabac.KabacException

internal class KeyStack {
    private val stack = LinkedHashSet<Key<*>>()

    fun <T> throwIfCyclic(key: Key<*>, block: () -> T): T {
        if (!stack.add(key)) {
            val cyclePrefix = stack.joinToString(" -> ") { it.name }
            val cycleSuffix = key.name
            throw KabacException.CyclicDependenciesException("Cycle: $cyclePrefix -> $cycleSuffix")
        }

        val result = block()
        check(stack.remove(key)) {
            "Cyclic key error, expected $key to be removed but it was not in stack"
        }

        return result
    }
}
