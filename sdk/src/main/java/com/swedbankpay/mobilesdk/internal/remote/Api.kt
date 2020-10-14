package com.swedbankpay.mobilesdk.internal.remote

import android.content.Context
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.swedbankpay.mobilesdk.*
import com.swedbankpay.mobilesdk.internal.parseProblem
import com.swedbankpay.mobilesdk.internal.remote.annotations.Required
import com.swedbankpay.mobilesdk.internal.remote.json.Link
import com.swedbankpay.mobilesdk.merchantbackend.MerchantBackendConfiguration
import com.swedbankpay.mobilesdk.merchantbackend.MerchantBackendProblem
import com.swedbankpay.mobilesdk.merchantbackend.RequestProblemException
import com.swedbankpay.mobilesdk.merchantbackend.UnexpectedResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.annotations.TestOnly
import java.io.IOException

internal object Api {
    private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    private val PROBLEM_MEDIA_TYPE = "application/problem+json".toMediaType()

    private val lazyClient = lazy {
        OkHttpClient.Builder()
            .build()
    }
    private suspend fun getClient(
        context: Context,
        configuration: MerchantBackendConfiguration
    ): OkHttpClient {
        if (!lazyClient.isInitialized()) {
            withContext(Dispatchers.IO) {
                try {
                    ProviderInstaller.installIfNeeded(context)
                } catch (_: GooglePlayServicesNotAvailableException) {
                    // ignored for compatibility with non-Google environments
                } catch (_: GooglePlayServicesRepairableException) {
                    // ditto
                }
            }
        }
        val client = lazyClient.value
        return configuration.certificatePinner?.let {
            client.newBuilder()
                .certificatePinner(it)
                .build()
        } ?: client
    }

    // ProviderInstaller will not work with Robolectric tests
    @TestOnly
    internal fun skipProviderInstallerForTests() {
        lazyClient.value
    }

    suspend fun <T : Any> request(
    context: Context,
    configuration: MerchantBackendConfiguration,
    method: String,
    url: HttpUrl,
    body: String?,
    userHeadersBuilder: suspend RequestDecorator.(UserHeaders) -> Unit,
    entityType: Class<T>
    ): CacheableResult<T> {
        val request = buildRequest(configuration, method, url, body, userHeadersBuilder)
        return executeRequest(context, configuration, request, entityType)
    }

    private suspend fun buildRequest(
        configuration: MerchantBackendConfiguration,
        method: String,
        url: HttpUrl,
        body: String?,
        userHeadersBuilder: suspend RequestDecorator.(UserHeaders) -> Unit
    ): Request {
        val domain = url.host
        if (configuration.domainWhitelist.none { it.matches(domain) }) {
            throw IOException("Non-whitelisted domain: $domain")
        }

        val headers = UserHeaders().also {
            configuration.requestDecorator?.apply {
                decorateAnyRequest(it, method, url.toString(), body)
                userHeadersBuilder(it)
            }
        }.toHeaders()

        val requestBody = body?.toRequestBody(JSON_MEDIA_TYPE)

        return Request.Builder()
            .url(url)
            .headers(headers)
            .method(method, requestBody)
            .build()
    }

    private suspend fun <T : Any> executeRequest(
        context: Context,
        configuration: MerchantBackendConfiguration,
        request: Request,
        entityType: Class<T>
    ): CacheableResult<T> {
        val client = getClient(context, configuration)
        val call = client.newCall(request)
        val result = CompletableDeferred<CacheableResult<T>>()
        call.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        // Buffer the entire body so we can pass it to the error
                        // reporting system if needed.
                        val body = it.body?.string()
                        checkErrorResponse(it, body)
                        val entity = parseResponse(it, body, entityType)
                        result.complete(CacheableResult(entity, it.validUntilMillis))
                    } catch (t: Throwable) {
                        result.completeExceptionally(t)
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                result.completeExceptionally(e)
            }
        })
        try {
            return result.await()
        } catch (c: CancellationException) {
            call.cancel()
            throw c
        }
    }

    private fun MediaType?.checkEquals(expectedContentType: MediaType) {
        if (this == null || type != expectedContentType.type || subtype != expectedContentType.subtype) {
            throw IOException("Invalid Content-Type: $this")
        }
    }

    private fun checkErrorResponse(response: Response, body: String?) {
        val code = response.code
        if (code in 400..599) {
            throw RequestProblemException(getProblem(response, body))
        }
    }

    private fun getProblem(response: Response, body: String?): MerchantBackendProblem {
        val status = response.code
        val contentType = response.body?.contentType()
        return try {
            contentType.checkEquals(PROBLEM_MEDIA_TYPE)
            parseProblem(status, checkNotNull(body))
        } catch (e: Exception) {
            throw UnexpectedResponseException(status, contentType?.toString(), body, e)
        }
    }

    private fun <T : Any> parseResponse(response: Response, body: String?, entityType: Class<T>): T {
        // Note: This does not support responses without a body,
        // such as 204 No Content, because the API does not
        // use them currently.
        val contentType = response.body?.contentType()
        return try {
            contentType.checkEquals(JSON_MEDIA_TYPE)
            GsonBuilder()
                .registerTypeHierarchyAdapter(Link::class.java, Link.getDeserializer(response))
                .create()
                .fromJson(checkNotNull(body), entityType)
                .also(::validateResponse)
        } catch (e: Exception) {
            throw UnexpectedResponseException(response.code, contentType?.toString(), body, e)
        }
    }

    private fun validateResponse(response: Any) {
        for (field in response.javaClass.declaredFields) {
            if (field.isAnnotationPresent(Required::class.java)) {
                field.isAccessible = true
                if (field.get(response) == null) {
                    val fieldName = field.getAnnotation(SerializedName::class.java)?.value ?: field.name
                    throw IOException("Missing required field $fieldName")
                }
            }
        }
    }

    private val Response.validUntilMillis: Long?
        get() = cacheControl.run {
            maxAgeSeconds.takeIf {
                it > 0 && !noStore && !noCache
            }
        }?.times(1000L)?.plus(receivedResponseAtMillis)
}