package com.lightningkite.kotlinx.reflection


object AnyReflection : EmptyReflection<Any>(Any::class, "kotlin.Any")
object UnitReflection : EmptyReflection<Unit>(Unit::class, "kotlin.Unit")
object BooleanReflection : EmptyReflection<Boolean>(Boolean::class, "kotlin.Boolean"){
    override val enumValues: List<Boolean> get() = listOf(false, true)
}
object ByteReflection : EmptyReflection<Byte>(Byte::class, "kotlin.Byte")
object ShortReflection : EmptyReflection<Short>(Short::class, "kotlin.Short")
object IntReflection : EmptyReflection<Int>(Int::class, "kotlin.Int")
object LongReflection : EmptyReflection<Long>(Long::class, "kotlin.Long")
object FloatReflection : EmptyReflection<Float>(Float::class, "kotlin.Float")
object DoubleReflection : EmptyReflection<Double>(Double::class, "kotlin.Double")
object StringReflection : EmptyReflection<String>(String::class, "kotlin.String")
object ListReflection : EmptyReflection<List<*>>(List::class, "kotlin.List")
object MapReflection : EmptyReflection<Map<*, *>>(Map::class, "kotlin.Map")
