package com.swedbankpay.mobilesdk.test.fragment

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.matcher.DomMatchers
import androidx.test.espresso.web.model.SimpleAtom
import androidx.test.espresso.web.sugar.Web
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swedbankpay.mobilesdk.internal.WebViewFragment
import com.swedbankpay.mobilesdk.test.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for WebViewFragment
 */
@RunWith(AndroidJUnit4::class)
class WebViewFragmentTest {

    private lateinit var scenario: FragmentScenario<WebViewParentFragment>

    /**
     * Start scenario for WebViewFragment and load empty page with element for storing test results
     */
    @Before
    fun setup() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_AppCompat)

        scenario.onWebViewFragment {
            it.load("", """
                <html>
                <head><title></title></head>
                <body>
                <p id="testResult"></p>
                </body>
                </html>
            """.trimIndent())
        }
    }

    /**
     * Destroy scenario
     */
    @After
    fun teardown() {
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    /**
     * Check that WebViewFragment shows a dialog when alert() is called in javascript,
     * and after the button on the dialog is pressed, alert() returns
     * and the dialog is dismissed.
     */
    @Test
    fun itShouldShowAlertAndReturnOnClick() {
        testJsDialog(
            "alert('$alertText')",
            ""
        ) {
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(alertText)))
            onView(withId(android.R.id.button2))
                .check(matches(Matchers.anyOf(
                    withEffectiveVisibility(Visibility.INVISIBLE),
                    withEffectiveVisibility(Visibility.GONE)
                )))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that WebViewFragment shows a dialog when alert() is called in javascript,
     * and after the the dialog is dismissed by the user, alert() returns.
     */
    @Test
    fun itShouldShowAlertAndReturnOnDismiss() {
        testJsDialog(
            "alert('$alertText')",
            ""
        ) {
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(alertText)))
                .perform(pressBack())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that WebViewFragment shows a dialog when confirm() is called in javascript,
     * and that after the "yes" button on the dialog is pressed, confirm() returns true
     * and the dialog is dismissed.
     */
    @Test
    fun itShouldShowConfirmAndReturnTrueForOk() {
        testJsDialog(
            "confirm('$confirmText')",
            "true"
        ) {
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(confirmText)))
            onView(withId(android.R.id.button2))
                .check(matches(isDisplayed()))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that WebViewFragment shows a dialog when confirm() is called in javascript,
     * and that after the "no" button on the dialog is pressed, confirm() returns false
     * and the dialog is dismissed.
     */
    @Test
    fun itShouldShowConfirmAndReturnFalseForCancel() {

        testJsDialog(
            "confirm('$confirmText')",
            "false"
        ) {
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(confirmText)))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
            onView(withId(android.R.id.button2))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that WebViewFragment shows a dialog when confirm() is called in javascript,
     * and after the the dialog is dismissed by the user, confirm() returns false.
     */
    @Test
    fun itShouldShowConfirmAndReturnFalseOnDismiss() {
        testJsDialog(
            "confirm('$confirmText')",
            "false"
        ) {
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(confirmText)))
                .perform(pressBack())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that WebViewFragment shows a dialog when prompt() is called in javascript,
     * and that after the "yes" button on the dialog is pressed,
     * prompt() returns the value entered and the dialog is dismissed.
     */
    @Test
    fun itShouldShowPromptAndReturnValueForOk() {
        testJsDialog(
            "prompt('$promptText', '$promptHint')",
            promptValue
        ) {
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_message))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptText)))
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_value))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptHint)))
                .perform(replaceText(promptValue))
            onView(withId(android.R.id.button2))
                .check(matches(isDisplayed()))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that WebViewFragment shows a dialog when prompt() is called in javascript,
     * and that after the "no" button on the dialog is pressed, prompt() returns null
     * and the dialog is dismissed.
     */
    @Test
    fun itShouldShowPromptAndReturnNullForCancel() {
        testJsDialog(
            "prompt('$promptText', '$promptHint') === null ? 'null' : 'non-null'",
            "null"
        ) {
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_message))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptText)))
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_value))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptHint)))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
            onView(withId(android.R.id.button2))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that WebViewFragment shows a dialog when prompt() is called in javascript,
     * and that after the "yes" button on the dialog is pressed, prompt() returns non-null
     * and the dialog is dismissed.
     */
    @Test
    fun itShouldShowPromptAndReturnNonnullForOk() {
        testJsDialog(
            "prompt('$promptText', '$promptHint') === null ? 'null' : 'non-null'",
            "non-null"
        ) {
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_message))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptText)))
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_value))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptHint)))
                .perform(replaceText(""))
            onView(withId(android.R.id.button2))
                .check(matches(isDisplayed()))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that WebViewFragment shows a dialog when prompt() is called in javascript,
     * and after the the dialog is dismissed by the user, prompt() returns null.
     */
    @Test
    fun itShouldShowPromptAndReturnNullOnDismiss() {
        testJsDialog(
            "prompt('$promptText', '$promptHint') === null ? 'null' : 'non-null'",
            "null"
        ) {
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_message))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptText)))
                .perform(pressBack())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that if WebViewFragment is recreated while showing an alert, the dialog will be visible after the fragment is recreated,
     * and that after the button on the dialog is pressed, alert() returns
     * and the dialog is dismissed.
     */
    @Test
    fun itShouldRetainAlertOverRecreate() {
        testJsDialog(
            "alert('$alertText')",
            ""
        ) {
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(alertText)))
            scenario.apply {
                recreate()
                runBlocking { waitForDialogFragment() }
            }
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(alertText)))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that if WebViewFragment is recreated while showing a confirm, the dialog will be visible after the fragment is recreated,
     * and that after the "yes" button on the dialog is pressed, confirm() returns true
     * and the dialog is dismissed.
     */
    fun itShouldRetainPromptOverRecreateAndReturnTrueForOk() {
        testJsDialog(
            "confirm('$confirmText')",
            "true"
        ) {
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(confirmText)))
            scenario.apply {
                recreate()
                runBlocking { waitForDialogFragment() }
            }
            onView(withId(android.R.id.message))
                .check(matches(isDisplayed()))
                .check(matches(withText(confirmText)))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    /**
     * Check that if WebViewFragment is recreated while showing a prompt, the dialog will be visible after the fragment is recreated,
     * and that after the "yes" button on the dialog is pressed,
     * prompt() returns the value entered and the dialog is dismissed.
     */
    @Test
    fun itShouldRetainPromptOverRecreateAndReturnValueForOk() {
        testJsDialog(
            "prompt('$promptText', '$promptHint')",
            promptValue
        ) {
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_message))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptText)))
            scenario.apply {
                recreate()
                runBlocking { waitForDialogFragment() }
            }
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_message))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptText)))
            onView(withId(com.swedbankpay.mobilesdk.R.id.swedbankpaysdk_prompt_value))
                .check(matches(isDisplayed()))
                .check(matches(withText(promptHint)))
                .perform(replaceText(promptValue))
            onView(withId(android.R.id.button1))
                .check(matches(isDisplayed()))
                .perform(click())
        }
        onView(withId(android.R.id.message))
            .check(doesNotExist())
    }

    private fun testJsDialog(dialogScript: String, expectedResult: String, dialogInteraction: () -> Unit) {
        val script = "document.getElementById('testResult').textContent = $dialogScript"
        GlobalScope.launch {
            Web.onWebView().perform(SimpleAtom(script))
        }
        runBlocking { scenario.waitForDialogFragment() }
        dialogInteraction()
        Web.onWebView().check(
            WebViewAssertions.webContent(
                DomMatchers.elementByXPath(
                    "//*[@id='testResult']",
                    DomMatchers.withTextContent(expectedResult)
                )
            )
        )
    }

    private companion object {
        private const val timeout = 5000L

        private const val delayStep = 100L
        private const val maxDelayStepCount = (timeout / delayStep).toInt()

        private const val alertText = "Alert"
        private const val confirmText = "Confirm"
        private const val promptText = "Prompt"
        private const val promptHint = "hint"
        private const val promptValue = "value"

        private inline fun FragmentScenario<WebViewParentFragment>.onWebViewFragment(
            crossinline f: (WebViewFragment) -> Unit) {
            onFragment { f(it.webViewFragment) }
        }

        private suspend fun FragmentScenario<WebViewParentFragment>.waitForDialogFragment() {
            var found = false
            var i = 0
            while (!found && i < maxDelayStepCount) {
                i++
                onWebViewFragment { webViewFragment ->
                    found = webViewFragment.childFragmentManager
                        .fragments
                        .firstOrNull { it is DialogFragment } != null
                }
                if (!found) delay(delayStep)
            }
            Assert.assertTrue("Timeout waiting for DialogFragment", found)
        }
    }

    /**
     * Parent fragment for WebViewFragment tests.
     *
     * WebViewFragment expects to have a parent fragment
     * with an InternalPaymentViewModel, so we provide one.
     */
    class WebViewParentFragment : Fragment(R.layout.web_view_parent_fragment) {
        internal val webViewFragment get() = childFragmentManager.findFragmentById(R.id.web_view_fragment_container) as WebViewFragment
    }
}