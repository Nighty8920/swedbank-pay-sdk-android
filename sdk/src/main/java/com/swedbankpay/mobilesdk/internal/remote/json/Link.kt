package com.swedbankpay.mobilesdk.internal.remote.json

import android.content.Context
import android.os.Parcel
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonWriter
import com.swedbankpay.mobilesdk.Configuration
import com.swedbankpay.mobilesdk.R
import com.swedbankpay.mobilesdk.RequestDecorator
import com.swedbankpay.mobilesdk.UserHeaders
import com.swedbankpay.mobilesdk.internal.remote.Api
import okhttp3.HttpUrl
import okhttp3.Response
import java.io.IOException
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
        noinline userHeadersBuilder: suspend RequestDecorator.(UserHeaders) -> Unit
    ) = getCacheable<T>(context, configuration, userHeadersBuilder).value

    protected suspend inline fun <reified T : Any> getCacheable(
        context: Context,
        configuration: Configuration,
        noinline userHeadersBuilder: suspend RequestDecorator.(UserHeaders) -> Unit
    ) = Api.get(context, configuration, href, userHeadersBuilder, T::class.java)

    protected suspend inline fun <reified T : Any> post(
        context: Context,
        configuration: Configuration,
        body: String,
        noinline userHeadersBuilder: suspend RequestDecorator.(UserHeaders) -> Unit
    ) = Api.post(context, configuration, href, body, userHeadersBuilder, T::class.java).value

    class Root(href: HttpUrl) : Link(href) {
        suspend fun get(context: Context, configuration: Configuration) = getCacheable<TopLevelResources>(context, configuration) {
            decorateGetTopLevelResources(it)
        }
    }

    class Consumers(href: HttpUrl) : Link(href) {
        @Throws(IOException::class) // for testing; see Configuration.getTopLevelResources
        suspend fun post(context: Context, configuration: Configuration, body: String) = post<ConsumerSession>(context, configuration, body) {
            decorateInitiateConsumerSession(it, body)
        }
    }

    class PaymentOrders(href: HttpUrl) : Link(href) {
        @Throws(IOException::class) // for testing
        suspend fun post(
            context: Context,
            configuration: Configuration,
            consumerProfileRef: String?,
            merchantData: String?
        ): com.swedbankpay.mobilesdk.internal.remote.json.PaymentOrder {
            val callbackScheme = context.getString(R.string.swedbankpaysdk_callback_url_scheme)
            val body = buildCreatePaymentOrderBody(callbackScheme, consumerProfileRef, merchantData)
            return post(context, configuration, body) {
                decorateCreatePaymentOrder(it, body, consumerProfileRef, merchantData)
            }
        }

        private fun buildCreatePaymentOrderBody(callbackScheme: String, consumerProfileRef: String?, merchantData: String?): String {
            val writer = StringWriter()
            JsonWriter(writer).use {  it.apply {
                serializeNulls = false
                beginObject()
                if (callbackScheme.isNotEmpty()) {
                    name("callbackScheme")
                    value(callbackScheme)
                }
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
            get<com.swedbankpay.mobilesdk.internal.remote.json.PaymentOrder>(context, configuration) {
                decorateGetPaymentOrder(it, href.toString())
            }
    }
}
