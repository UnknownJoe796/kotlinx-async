package com.lightningkite.kotlinx.reflection

data class KxValue<Owner, Type>(
        override val name: String,
        override val type: KxType,
        override val get: (Owner) -> Type,
        override val annotations: List<KxAnnotation> = listOf()
) : KxField<Owner, Type> {
    override val set: ((Owner, Type) -> Unit)?
        get() = null
}