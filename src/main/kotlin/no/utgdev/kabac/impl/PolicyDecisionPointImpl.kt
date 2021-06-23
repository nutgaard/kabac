package no.utgdev.kabac.impl

import no.utgdev.kabac.AttributeValue
import no.utgdev.kabac.Kabac
import no.utgdev.kabac.utils.Key

class PolicyDecisionPointImpl : Kabac.PolicyDecisionPoint {
    private val providerRegister = mutableMapOf<Key<*>, Kabac.PolicyInformationPoint<*>>()

    override fun install(informationPoint: Kabac.PolicyInformationPoint<*>): Kabac.PolicyDecisionPoint {
        providerRegister[informationPoint.key] = informationPoint
        return this
    }

    override fun createEvaluationContext(attributes: List<AttributeValue<*>>): Kabac.EvaluationContext {
        return EvaluationContextImpl(providerRegister.values + attributes)
    }
}
