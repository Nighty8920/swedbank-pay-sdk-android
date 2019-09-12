package com.payex.mobilesdk.internal.remote.json

import android.content.Context
import android.os.Parcel
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonWriter
import com.payex.mobilesdk.Configuration
import com.payex.mobilesdk.RequestDecorator
import com.payex.mobilesdk.UserHeaders
import com.payex.mobilesdk.internal.remote.Api
import okhttp3.HttpUrl
import okhttp3.Response
import java.io.StringWriter

internal fun Parcel.writeLink(link: Link) {
    writeString(link.href.toString())
}
internal fun <T> Parcel.readLink(constructor: (HttpUrl) -> T): T? {
    return readString()?.let(HttpUrl::get)?.let(constructor)
}

internal sealed class Link(
    val href: HttpUrl
) {
    companion object {
        fun getDeserializer(response: Response): JsonDeserializer<Link?> {
            val requestUrl = response.request().url()
            return JsonDeserializer { element, type, _ ->
                if (element.isJsonNull) return@JsonDeserializer null

                val link = try {
                    element.asString
                } catch (e: Exception) {
                    throw JsonSyntaxException(e)
                }
                val href = requestUrl.resolve(link)
                    ?: throw JsonSyntaxException("Invalid link: $link")
                (type as Class<*>)
                    .asSubclass(Link::class.java)
                    .getConstructor(HttpUrl::class.java)
                    .newInstance(href)
            }
        }
    }

    protected suspend inline fun <reified T : Any> get(
        context: Context,
        configuration: Configuration,
        noinline userHeadersBuilder: RequestDecorator.(UserHeaders) -> Unit
    ) = getCacheable<T>(context, configuration, userHeadersBuilder).value

    protected suspend inline fun <reified T : Any> getCacheable(
        context: Context,
        configuration: Configuration,
        noinline userHeadersBuilder: RequestDecorator.(UserHeaders) -> Unit
    ) = Api.get(context, configuration, href, userHeadersBuilder, T::class.java)

    protected suspend inline fun <reified T : Any> post(
        context: Context,
        configuration: Configuration,
        body: String,
        noinline userHeadersBuilder: RequestDecorator.(UserHeaders) -> Unit
    ) = Api.post(context, configuration, href, body, userHeadersBuilder, T::class.java).value

    class Root(href: HttpUrl) : Link(href) {
        suspend fun get(context: Context, configuration: Configuration) = getCacheable<TopLevelResources>(context, configuration) {
            decorateGetTopLevelResources(it)
        }
    }

    class Consumers(href: HttpUrl) : Link(href) {
        suspend fun post(context: Context, configuration: Configuration, body: String) = post<ConsumerSession>(context, configuration, body) {
            decorateInitiateConsumerSession(it, body)
        }
    }

    class PaymentOrders(href: HttpUrl) : Link(href) {
        suspend fun post(
            context: Context,
            configuration: Configuration,
            consumerProfileRef: String?,
            merchantData: String?
        ): com.payex.mobilesdk.internal.remote.json.PaymentOrder {
            val body = buildCreatePaymentOrderBody(consumerProfileRef, merchantData)
            return post(context, configuration, body) {
                decorateCreatePaymentOrder(it, body, consumerProfileRef, merchantData)
            }
        }

        private fun buildCreatePaymentOrderBody(consumerProfileRef: String?, merchantData: String?): String {
            val writer = StringWriter()
            JsonWriter(writer).use {  it.apply {
                serializeNulls = false
                beginObject()
                name("consumerProfileRef")
                value(consumerProfileRef)
                name("merchantData")
                jsonValue(merchantData)
                endObject()
            } }
            return writer.toString()
        }
    }

    class PaymentOrder(href: HttpUrl) : Link(href) {
        suspend fun get(context: Context, configuration: Configuration) =
            get<com.payex.mobilesdk.internal.remote.json.PaymentOrder>(context, configuration) {
                decorateGetPaymentTransactions(it, href.toString())
            }
    }
}
