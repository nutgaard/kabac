package no.utgdev.kabac

sealed class Decision(val type: Type) {
    enum class Type {
        PERMIT, DENY, NOT_APPLICABLE
    }

    fun isApplicable(): Boolean = when (this) {
        is Permit, is Deny -> true
        is NotApplicable -> false
    }

    fun withBias(bias: Type): Decision {
        if (isApplicable()) {
            return this
        }
        return when (bias) {
            Type.PERMIT -> Permit()
            Type.DENY -> Deny("No applicable policy found")
            Type.NOT_APPLICABLE -> throw UnsupportedOperationException("Bias cannot be 'NOT_APPLICABLE'")
        }
    }

    override fun hashCode(): Int = type.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other is Decision) {
            return type == other.type
        }
        return false
    }

    class Permit : Decision(Type.PERMIT) {
        override fun toString() = "Permit"
    }
    class Deny(private val message: String) : Decision(Type.DENY) {
        override fun toString() = "Deny($message)"
    }
    class NotApplicable(private val message: String? = null) : Decision(Type.NOT_APPLICABLE) {
        override fun toString(): String {
            val msg = message?.let { "($it)" } ?: ""
            return "NotApplicable$msg"
        }
    }
}
