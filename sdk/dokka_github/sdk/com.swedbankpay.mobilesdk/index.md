[sdk](../index.md) / [com.swedbankpay.mobilesdk](./index.md)

## Package com.swedbankpay.mobilesdk

### Types

| Name | Summary |
|---|---|
| [Configuration](-configuration/index.md) | The Swedbank Pay configuration for your application.`class Configuration` |
| [Consumer](-consumer/index.md) | A consumer to identify using the [checkin](https://developer.swedbankpay.com/checkout/checkin) flow.`data class Consumer : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html)`, ExtensibleJsonObject` |
| [ConsumerOperation](-consumer-operation/index.md) | Operations that can be performed with a [Consumer](-consumer/index.md).`enum class ConsumerOperation` |
| [DeliveryTimeFrameIndicator](-delivery-time-frame-indicator/index.md) | Product delivery timeframe for a [RiskIndicator](-risk-indicator/index.md).`enum class DeliveryTimeFrameIndicator` |
| [ItemType](-item-type/index.md) | The type of an [OrderItem](-order-item/index.md).`enum class ItemType` |
| [Language](-language/index.md) | Languages supported by checkin and payment menu.`enum class Language` |
| [OrderItem](-order-item/index.md) | An item being paid for, part of a [PaymentOrder](-payment-order/index.md).`data class OrderItem : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [PayeeInfo](-payee-info/index.md) | `data class PayeeInfo : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [PaymentFragment](-payment-fragment/index.md) | A [Fragment](#) that handles a payment process.`open class PaymentFragment : Fragment` |
| [PaymentOrder](-payment-order/index.md) | `data class PaymentOrder : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html)`, ExtensibleJsonObject` |
| [PaymentOrderOperation](-payment-order-operation/index.md) | `enum class PaymentOrderOperation` |
| [PaymentOrderPayer](-payment-order-payer/index.md) | `data class PaymentOrderPayer : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [PaymentOrderUrls](-payment-order-urls/index.md) | `data class PaymentOrderUrls : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [PaymentViewModel](-payment-view-model/index.md) | ViewModel for communicating with a [PaymentFragment](-payment-fragment/index.md).`class PaymentViewModel : AndroidViewModel` |
| [PickUpAddress](-pick-up-address/index.md) | `data class PickUpAddress : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [Problem](-problem/index.md) | Base class for any problems encountered in the payment.`sealed class Problem : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [ProperProblem](-proper-problem/index.md) | A Problem parsed from an application/problem+json object.`interface ProperProblem` |
| [PurchaseIndicator](-purchase-indicator/index.md) | `enum class PurchaseIndicator` |
| [RequestDecorator](-request-decorator/index.md) | Callback for adding custom headers to backend requests.`abstract class RequestDecorator` |
| [RequestDecoratorCompat](-request-decorator-compat/index.md) | Java compatibility wrapper for [RequestDecorator](-request-decorator/index.md).`open class RequestDecoratorCompat : `[`RequestDecorator`](-request-decorator/index.md) |
| [RiskIndicator](-risk-indicator/index.md) | `data class RiskIndicator : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [ShipIndicator](-ship-indicator/index.md) | `class ShipIndicator` |
| [SwedbankPayAction](-swedbank-pay-action.md) | Action to take to correct a problem reported by the Swedbank Pay backend.`typealias SwedbankPayAction = `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [SwedbankPayProblem](-swedbank-pay-problem/index.md) | A Problem defined by the Swedbank Pay backend. [https://developer.payex.com/xwiki/wiki/developer/view/Main/ecommerce/technical-reference/#HProblems](https://developer.payex.com/xwiki/wiki/developer/view/Main/ecommerce/technical-reference/#HProblems)`interface SwedbankPayProblem : `[`ProperProblem`](-proper-problem/index.md) |
| [SwedbankPaySubproblem](-swedbank-pay-subproblem/index.md) | Object detailing the reason for a [SwedbankPayProblem](-swedbank-pay-problem/index.md).`class SwedbankPaySubproblem` |
| [TerminalFailure](-terminal-failure/index.md) | Describes a terminal error condition signaled by an onError callback from Swedbank Pay.`class TerminalFailure : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [UnexpectedContentProblem](-unexpected-content-problem/index.md) | A Pseudo-Problem where the server response was entirely unrecognized.`interface UnexpectedContentProblem` |
| [UnknownProblem](-unknown-problem/index.md) | A Problem whose [type](-unknown-problem/type.md) was not recognized.`interface UnknownProblem : `[`ProperProblem`](-proper-problem/index.md) |
| [UserHeaders](-user-headers/index.md) | Builder for custom headers.`class UserHeaders` |

### Extensions for External Classes

| Name | Summary |
|---|---|
| [androidx.fragment.app.FragmentActivity](androidx.fragment.app.-fragment-activity/index.md) |  |
