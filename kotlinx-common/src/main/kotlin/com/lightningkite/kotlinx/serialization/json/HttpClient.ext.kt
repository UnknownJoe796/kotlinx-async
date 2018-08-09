package com.lightningkite.kotlinx.serialization.json

import com.lightningkite.kotlinx.async.DelayedResultFunction
import com.lightningkite.kotlinx.async.transform
import com.lightningkite.kotlinx.httpclient.*
import com.lightningkite.kotlinx.reflection.KxType
import com.lightningkite.kotlinx.reflection.kxType
import kotlin.reflect.KClass

inline fun <reified T : Any> HttpClient.callJson(
        url: String,
        method: HttpMethod,
        body: HttpBody,
        headers: Map<String, List<String>> = mapOf(),
        serializer: JsonSerializer = JsonSerializer
): DelayedResultFunction<HttpResponse<T>> = callJson(
        url,
        method,
        body,
        headers,
        serializer,
        T::class
)


fun <T : Any> HttpClient.callJson(
        url: String,
        method: HttpMethod,
        body: HttpBody,
        headers: Map<String, List<String>> = mapOf(),
        serializer: JsonSerializer = JsonSerializer,
        typeInfo: KxType
): DelayedResultFunction<HttpResponse<T>> {
    return callString(
            url, method, body, headers
    ).transform {
        it.copy {
            @Suppress("UNCHECKED_CAST")
            serializer.read(typeInfo, it) as T
        }
    }
}


fun <T : Any> HttpClient.callJson(
        url: String,
        method: HttpMethod,
        body: HttpBody,
        headers: Map<String, List<String>> = mapOf(),
        serializer: JsonSerializer = JsonSerializer,
        type: KClass<T>
): DelayedResultFunction<HttpResponse<T>> {
    return callString(
            url, method, body, headers
    ).transform {
        it.copy { serializer.read(type.kxType, it) as T }
    }
}


inline fun <reified T : Any> HttpBody.Companion.BJson(
        value: T,
        serializer: JsonSerializer = JsonSerializer
) = BJson(
        value = value,
        serializer = serializer,
        type = T::class
)


fun <T : Any> HttpBody.Companion.BJson(
        value: T,
        serializer: JsonSerializer = JsonSerializer,
        type: KClass<T>
) = HttpBody.BString("application/json", serializer.write(type.kxType, value).toString())


@Suppress("UNCHECKED_CAST")
fun <T : Any> HttpBody.Companion.BJson(
        value: T,
        serializer: JsonSerializer = JsonSerializer,
        typeInfo: KxType
) = HttpBody.BString("application/json", serializer.write(typeInfo, value).toString())