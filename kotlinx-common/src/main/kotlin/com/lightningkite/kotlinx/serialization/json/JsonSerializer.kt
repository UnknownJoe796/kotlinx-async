package com.lightningkite.kotlinx.serialization.json

import com.lightningkite.kotlinx.locale.*
import com.lightningkite.kotlinx.reflection.AnyReflection
import com.lightningkite.kotlinx.reflection.KxType
import com.lightningkite.kotlinx.reflection.StringReflection
import com.lightningkite.kotlinx.reflection.kxType
import com.lightningkite.kotlinx.serialization.*
import kotlin.reflect.KClass

@Suppress("LeakingThis")
open class JsonSerializer : StandardReader<RawJsonReader>, StandardWriter<RawJsonWriter, Unit> {

    companion object : JsonSerializer()

    override val readerGenerators: MutableList<Pair<Float, AnySubReaderGenerator<RawJsonReader>>> = ArrayList()
    override val readers: MutableMap<KClass<*>, AnySubReader<RawJsonReader>> = HashMap()
    override val writerGenerators: MutableList<Pair<Float, AnySubWriterGenerator<RawJsonWriter, Unit>>> = ArrayList()
    override val writers: MutableMap<KClass<*>, AnySubWriter<RawJsonWriter, Unit>> = HashMap()

    var boxWriter: RawJsonWriter.(typeInfo: KxType, Any?) -> Unit = { typeInfo, value ->
        if (value == null) writeNull()
        else writeArray {
            val useType = when (value) {
                is List<*> -> List::class
                is Map<*, *> -> Map::class
                else -> value::class
            }
            writeEntry {
                writeString(useType.externalName!!)
            }
            writeEntry {
                writer(useType).invoke(this, value, typeInfo)
            }
        }
    }
    var boxReader: RawJsonReader.(typeInfo: KxType) -> Any? = {
        if (lexer.peekIsNull()) {
            null
        } else beginArray {
            val type = ExternalTypeRegistry[nextString()]!!
            reader(type).invoke(this, type.kxType)
        }
    }


    inline fun <T : Any> setNullableReader(typeKClass: KClass<T>, crossinline read: RawJsonReader.(KxType) -> T) {
        setReader(typeKClass) {
            if (lexer.peek().let { it.tokenType == TokenType.VALUE && it.value == null })
                null
            else
                read(it)
        }
    }

    inline fun <T : Any> setNullableWriter(typeKClass: KClass<T>, crossinline write: RawJsonWriter.(T, KxType) -> Unit) {
        setWriter(typeKClass) { it, type ->
            if (it == null) writeNull()
            else write(it, type)
        }
    }

    init {
        setReader(Unit::class) { nextAny(); Unit }
        setWriter(Unit::class) { it, _ -> writeNull() }

        setNullableReader(Int::class) { nextInt() }
        setNullableWriter(Int::class) { it, _ -> writeNumber(it) }

        setNullableReader(Short::class) { nextInt().toShort() }
        setNullableWriter(Short::class) { it, _ -> writeNumber(it) }

        setNullableReader(Byte::class) { nextInt().toByte() }
        setNullableWriter(Byte::class) { it, _ -> writeNumber(it) }

        setNullableReader(Long::class) { nextLong() }
        setNullableWriter(Long::class) { it, _ -> writeNumber(it) }

        setNullableReader(Float::class) { nextDouble().toFloat() }
        setNullableWriter(Float::class) { it, _ -> writeNumber(it) }

        setNullableReader(Double::class) { nextDouble() }
        setNullableWriter(Double::class) { it, _ -> writeNumber(it) }

        setNullableReader(Number::class) { nextDouble() }
        setNullableWriter(Number::class) { it, _ -> writeNumber(it) }

        setNullableReader(Boolean::class) { nextBoolean() }
        setNullableWriter(Boolean::class) { it, _ -> writeBoolean(it) }

        setNullableReader(String::class) { nextString() }
        setNullableWriter(String::class) { it, _ -> writeString(it) }

        setNullableReader(Date::class) { Date.iso8601(nextString()) }
        setNullableWriter(Date::class) { it, _ -> writeString(it.iso8601()) }

        setNullableReader(Time::class) { Time.iso8601(nextString()) }
        setNullableWriter(Time::class) { it, _ -> writeString(it.iso8601()) }

        setNullableReader(DateTime::class) { DateTime.iso8601(nextString()) }
        setNullableWriter(DateTime::class) { it, _ -> writeString(it.iso8601()) }

        setNullableReader(TimeStamp::class) { TimeStamp.iso8601(nextString()) }
        setNullableWriter(TimeStamp::class) { it, _ -> writeString(it.iso8601()) }

        setNullableReader(List::class) { typeInfo ->
            val valueSubtype = typeInfo.typeParameters.getOrNull(0)?.takeUnless { it.isStar }?.type
                    ?: KxType(AnyReflection, true)
            val output = ArrayList<Any?>()
            val valueSubtypeReader = reader(valueSubtype.base.kclass)
            beginArray {
                while (hasNext()) {
                    output.add(valueSubtypeReader.invoke(this, valueSubtype))
                }
            }
            output
        }
        setNullableWriter(List::class) { value, typeInfo ->
            val valueSubtype = typeInfo.typeParameters.getOrNull(0)?.takeUnless { it.isStar }?.type
                    ?: KxType(AnyReflection, true)
            val valueSubtypeWriter = writer(valueSubtype.base.kclass)
            writeArray {
                @Suppress("UNCHECKED_CAST")
                for (subvalue in value as List<Any?>) {
                    writeEntry {
                        valueSubtypeWriter.invoke(this, subvalue, valueSubtype)
                    }
                }
            }
        }

        setNullableReader(Map::class) { typeInfo ->
            val keySubtype = typeInfo.typeParameters.getOrNull(0)?.takeUnless { it.isStar }?.type
                    ?: KxType(AnyReflection, true)
            val valueSubtype = typeInfo.typeParameters.getOrNull(1)?.takeUnless { it.isStar }?.type
                    ?: KxType(AnyReflection, true)
            val valueSubtypeReader = reader(valueSubtype.base.kclass)

            val map = LinkedHashMap<Any?, Any?>()
            if (keySubtype.base == StringReflection) {
                beginObject {
                    while (hasNext()) {
                        map[nextName()] = valueSubtypeReader.invoke(this, valueSubtype)
                    }
                }
            } else {
                val keySubReader = reader(keySubtype.base.kclass)
                beginObject {
                    while (hasNext()) {
                        val key = nextName().let {
                            keySubReader.invoke(RawJsonReader(it.iterator()), keySubtype)
                        }
                        map[key] = valueSubtypeReader.invoke(this, valueSubtype)
                    }
                }
            }

            map
        }
        setNullableWriter(Map::class) { value, typeInfo ->
            val keySubtype = typeInfo.typeParameters.getOrNull(0)?.takeUnless { it.isStar }?.type
                    ?: KxType(AnyReflection, true)
            val valueSubtype = typeInfo.typeParameters.getOrNull(1)?.takeUnless { it.isStar }?.type
                    ?: KxType(AnyReflection, true)
            val valueSubtypeWriter = writer(valueSubtype.base.kclass)

            if (keySubtype.base == StringReflection) {
                writeObject {
                    for ((key, subvalue) in value) {
                        writeEntry(key as String) {
                            valueSubtypeWriter.invoke(this, subvalue, valueSubtype)
                        }
                    }
                }
            } else {
                val keySubWriter = writer(keySubtype.base.kclass)
                writeObject {
                    for ((key, subvalue) in value) {
                        val stringifiedKey = StringBuilder().also {
                            keySubWriter.invoke(RawJsonWriter(it), key, keySubtype)
                        }.toString()
                        writeEntry(stringifiedKey) {
                            valueSubtypeWriter.invoke(this, subvalue, valueSubtype)
                        }
                    }
                }
            }
        }

        val polyboxWriter: AnySubWriter<RawJsonWriter, Unit> = { value, t -> boxWriter.invoke(this, t, value) }
        val polyboxReader: AnySubReader<RawJsonReader> = {
            @Suppress("UNCHECKED_CAST")
            boxReader.invoke(this, it)
        }

        setReader(Any::class, polyboxReader)
        setWriter(Any::class, polyboxWriter)

        addReaderGenerator(1f, EnumGenerators.readerGenerator(this))
        addWriterGenerator(1f, EnumGenerators.writerGenerator(this))

        //Any non-final polyboxing
        addReaderGenerator(.5f) { type ->
            if (type.serializePolymorphic) {
                polyboxReader
            } else null
        }
        addWriterGenerator(.5f) { type ->
            if (type.serializePolymorphic) {
                polyboxWriter
            } else null
        }

        addReaderGenerator(0f) { type ->
            val helper = ReflectiveReaderHelper.tryInit(type, this)
                    ?: return@addReaderGenerator null
            return@addReaderGenerator { typeInfo ->
                if (lexer.peekIsNull()) null
                else {
                    val builder = helper.instanceBuilder()
                    beginObject {
                        while (hasNext()) {
                            builder.place(nextName(), this) { nextAny() }
                        }
                    }
                    builder.build()
                }
            }
        }
        addWriterGenerator(0f) { type ->
            val vars = type.reflectiveWriterData(this) ?: return@addWriterGenerator null

            return@addWriterGenerator { value, typeInfo ->
                if (value == null) writeNull()
                else writeObject {
                    vars.forEach {
                        writeEntry(it.key) {
                            it.writeValue(this, value)
                        }
                    }
                }
            }
        }
    }

    fun read(type: KxType, from: Iterator<Char>) = read(type, RawJsonReader(from))
    fun read(type: KxType, from: String) = read(type, RawJsonReader(from.iterator()))
    fun write(type: KxType, value: Any?, to: Appendable) = to.also { write(type, value, RawJsonWriter(it)) }
    fun write(type: KxType, value: Any?): String = StringBuilder().apply { write(type, value, this) }.toString()

    fun <T : Any> read(type: KClass<T>, from: Iterator<Char>) = read(type.kxType, from)
    fun <T : Any> read(type: KClass<T>, from: String) = read(type.kxType, from)
    fun <T : Any> write(type: KClass<T>, value: Any?, to: Appendable) = write(type.kxType, value, to)
    fun <T : Any> write(type: KClass<T>, value: Any?): String = write(type.kxType, value)
}