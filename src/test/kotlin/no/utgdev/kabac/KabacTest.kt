package no.utgdev.kabac

import no.utgdev.kabac.CombiningAlgorithm.Companion.denyOverride
import no.utgdev.kabac.CombiningAlgorithm.Companion.permitOverride
import no.utgdev.kabac.impl.PolicyDecisionPointImpl
import no.utgdev.kabac.impl.PolicyEnforcementPointImpl
import no.utgdev.kabac.utils.Key

object CommonAttributes {
    val USER_ID = Key<String>("user-id")
}

object UserRolesInformationPoint : Kabac.PolicyInformationPoint<List<String>> {
    override val key = Key<List<String>>(UserRolesInformationPoint)
    override fun provide(ctx: Kabac.EvaluationContext): List<String> {
        val userId = ctx.getValue(CommonAttributes.USER_ID)
        if (userId == "admin") {
            return listOf("read", "write", "update", "delete")
        }
        return listOf("read")
    }
}
object UserAgeInformationPoint : Kabac.PolicyInformationPoint<Int?> {
    override val key = Key<Int?>(UserAgeInformationPoint)
    override fun provide(ctx: Kabac.EvaluationContext): Int? {
        val userId = ctx.getValue(CommonAttributes.USER_ID)
        if (userId == "admin") {
            return 19
        }
        return null
    }
}

object CanUpdatePolicy : Kabac.Policy {
    override val key = Key<Kabac.Policy>(CanUpdatePolicy)
    override fun evaluate(ctx: Kabac.EvaluationContext): Decision {
        val roles = ctx.getValue(UserRolesInformationPoint)
        if (roles.contains("update")) {
            return Decision.Permit()
        }
        return Decision.Deny("User missing 'update' role")
    }
}

object CanReadPolicy : Kabac.Policy {
    override val key = Key<Kabac.Policy>(CanReadPolicy)
    override fun evaluate(ctx: Kabac.EvaluationContext): Decision {
        val roles = ctx.getValue(UserRolesInformationPoint)
        if (roles.contains("read")) {
            return Decision.Permit()
        }
        return Decision.Deny("User missing 'read' role")
    }
}

object UserIsOldEnoughPolicy : Kabac.Policy {
    override val key = Key<Kabac.Policy>(UserIsOldEnoughPolicy)
    override fun evaluate(ctx: Kabac.EvaluationContext): Decision {
        val age = ctx.getValue(UserAgeInformationPoint) ?: -1
        if (age > 18) {
            return Decision.Permit()
        }
        return Decision.Deny("User not old enough: $age")
    }
}

fun main() {

    val pdp: Kabac.PolicyDecisionPoint = PolicyDecisionPointImpl()
        .install(UserRolesInformationPoint)
        .install(UserAgeInformationPoint)

    val pep: Kabac.PolicyEnforcementPoint = PolicyEnforcementPointImpl(
        bias = Decision.Type.DENY,
        policyDecisionPoint = pdp
    )

    val precombinedPolicy = permitOverride.combine(
        CanUpdatePolicy,
        denyOverride.combine(
            CanReadPolicy,
            UserIsOldEnoughPolicy
        )
    )

    val (decision, report) = pep.evaluatePolicyWithReport(
        attributes = listOf(
            CommonAttributes.USER_ID.withValue("commonuser")
        ),
        policy = precombinedPolicy
    )

//    println(decision)
    println(report)
}
