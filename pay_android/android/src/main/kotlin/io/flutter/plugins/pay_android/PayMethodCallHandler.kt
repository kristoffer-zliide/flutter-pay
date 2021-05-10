package io.flutter.plugins.pay_android

import android.app.Activity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

private const val METHOD_CHANNEL_NAME = "plugins.flutter.io/pay_channel"

private const val METHOD_USER_CAN_PAY = "userCanPay"
private const val METHOD_SHOW_PAYMENT_SELECTOR = "showPaymentSelector"

class PayMethodCallHandler private constructor(
        messenger: BinaryMessenger,
        activity: Activity,
) : MethodCallHandler {

    private val channel: MethodChannel = MethodChannel(messenger, METHOD_CHANNEL_NAME)
    private val googlePayHandler: GooglePayHandler = GooglePayHandler(activity)

    init {
        channel.setMethodCallHandler(this)
    }

    constructor(registrar: Registrar) : this(registrar.messenger(), registrar.activity()) {
        registrar.addActivityResultListener(googlePayHandler)
    }

    constructor(
            flutterBinding: FlutterPlugin.FlutterPluginBinding,
            activityBinding: ActivityPluginBinding,
    ) : this(flutterBinding.binaryMessenger, activityBinding.activity) {
        activityBinding.addActivityResultListener(googlePayHandler)
    }

    fun stopListening() = channel.setMethodCallHandler(null)

    @Suppress("UNCHECKED_CAST")
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            METHOD_USER_CAN_PAY -> googlePayHandler.isReadyToPay(result, call.arguments())
            METHOD_SHOW_PAYMENT_SELECTOR -> {
                val arguments = call.arguments<Map<String, Any>>()
                googlePayHandler.loadPaymentData(result,
                        arguments.getValue("payment_profile") as String,
                        arguments.getValue("payment_items") as List<Map<String, Any?>>)
            }

            else -> result.notImplemented()
        }
    }
}