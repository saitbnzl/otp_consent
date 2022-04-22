package com.huynn109.otp_consent

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.provider.Telephony.Sms
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.ShareCompat.getCallingActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SMSBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val SMS_CONSENT_REQUEST = 0x1009
    }

    private var listener: Listener? = null
    private var activity: Activity? = null

    fun injectListener(activity: Activity?, listener: Listener?) {
        this.listener = listener
        this.activity = activity
    }

    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (smsRetrieverStatus.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get consent intent
                    val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    try {
                        // Start activity to show consent dialog to user, activity must be started in
                        // 5 minutes, otherwise you'll receive another TIMEOUT intent
                        consentIntent.removeFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        consentIntent.removeFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        val defaultSmsPackageName = Sms.getDefaultSmsPackage(context);
                        if(getCallingActivity(activity).packageName.equals(defaultSmsPackageName)){
                            activity?.startActivityForResult(consentIntent, SMS_CONSENT_REQUEST)
                            listener?.onShowPermissionDialog()
                        }
                    } catch (e: ActivityNotFoundException) {
                        // Handle the exception ...
                    }
                }
                CommonStatusCodes.TIMEOUT -> listener?.onTimeout()
            }
        }
    }

    interface Listener {
        fun onShowPermissionDialog()
        fun onTimeout()
    }
}
