package no.utgdev.kabac.impl

import no.utgdev.kabac.Kabac
import no.utgdev.kabac.KabacException
import no.utgdev.kabac.utils.Key
import no.utgdev.kabac.utils.KeyStack

class EvaluationContextImpl(
    informationPoints: List<Kabac.PolicyInformationPoint<*>>
) : Kabac.EvaluationContext, Kabac.EvaluationReporter by EvaluationReporterImpl() {
    private val register = informationPoints.associateBy { it.key }
    private val cache = mutableMapOf<Key<*>, Any?>()
    private val keystack = KeyStack()

    override fun <TValue> getValue(attributeKey: Kabac.AttributeKey<TValue>): TValue = getValue(attributeKey.key)

    @Suppress("UNCHECKED_CAST")
    override fun <TValue> getValue(attributeKey: Key<TValue>): TValue {
        return keystack.throwIfCyclic(attributeKey) {
            if (cache.containsKey(attributeKey)) {
                val value = cache[attributeKey] as TValue
                report("Request $attributeKey, cache-hit: $value")
                value
            } else {
                val provider = register[attributeKey]
                if (provider == null) {
                    val msg = "Request $attributeKey, no provider found"
                    report(msg)
                    throw KabacException.MissingPolicyInformationPointException(msg)
                }

                val value = provider.provide(this) as TValue
                report("Request $attributeKey, cache-miss: $value")
                cache[attributeKey] = value
                value
            }
        }
    }
}
