package com.swedbankpay.mobilesdk

import android.content.Context

/**
 * The Swedbank Pay configuration for your application.
 *
 * You need a Configuration to use [PaymentFragment].
 * If you want to use a custom way of communicating with your services,
 * you can create a subclass of Configuration.
 * If you wish to use the specified Merchant Backend API,
 * create a [MerchantBackendConfiguration] using [MerchantBackendConfiguration.Builder].
 *
 * In most cases, it is enough to set a
 * [default Configuration][PaymentFragment.defaultConfiguration].
 * However, for more advanced situations, you may override [PaymentFragment.getConfiguration]
 * to provide a Configuration dynamically.
 *
 * N.B! Configuration is specified as `suspend` functions, i.e. Kotlin coroutines.
 * As Java does not support these, a [compatibility class][ConfigurationCompat]
 * is provided.
 */
abstract class Configuration {
    /**
     * Called by [PaymentFragment] when it needs to start a consumer identification
     * session. Your implementation must ultimately make the call to Swedbank Pay API
     * and return a [ViewConsumerIdentificationInfo] describing the result.
     *
     * @param context an application context
     * @param consumer the [Consumer] object set as the PaymentFragment argument
     * @param userData the user data object set as the PaymentFragment argument
     * @return ViewConsumerIdentificationInfo describing the consumer identification session
     */
    abstract suspend fun postConsumers(
        context: Context,
        consumer: Consumer?,
        userData: Any?
    ): ViewConsumerIdentificationInfo

    /**
     * Called by [PaymentFragment] when it needs to create a payment order.
     * Your implementation must ultimately make the call to Swedbank Pay API
     * and return a [ViewPaymentOrderInfo] describing the result.
     *
     * @param context an application context
     * @param paymentOrder the [PaymentOrder] object set as the PaymentFragment argument
     * @param userData the user data object set as the PaymentFragment argument
     * @param consumerProfileRef if a checkin was performed first, the `consumerProfileRef` from checkin
     * @return ViewPaymentOrderInfo describing the payment order
     */
    abstract suspend fun postPaymentorders(
        context: Context,
        paymentOrder: PaymentOrder?,
        userData: Any?,
        consumerProfileRef: String?
    ): ViewPaymentOrderInfo
}