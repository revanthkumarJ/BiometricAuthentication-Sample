package com.revanth.biometric

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BioMetricPromptManager(
    private val activity: AppCompatActivity
) {

    private val resultChannel = Channel<BioMetricResult>()
    val promptResults=resultChannel.receiveAsFlow()

    fun showBiometricPrompt(
        title: String,
        subtitle: String ="",
        description: String,
        negativeButtonText: String = "Cancel",
    ){
        val manager = BiometricManager.from(activity)
        val authenticators= if(Build.VERSION.SDK_INT >= 30){
            BIOMETRIC_STRONG  or DEVICE_CREDENTIAL
        }else{
            BIOMETRIC_STRONG
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)

        if(Build.VERSION.SDK_INT<30){
            promptInfo.setNegativeButtonText(negativeButtonText)
        }

        when(manager.canAuthenticate(authenticators)){
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE->{
                resultChannel.trySend(BioMetricResult.HardWareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED->{
                resultChannel.trySend(BioMetricResult.AuthenticationNotSet)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE->{
                resultChannel.trySend(BioMetricResult.FeatureUnAvailable)
                return
            }
            else-> Unit
        }

        val prompt = BiometricPrompt(activity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                resultChannel.trySend(BioMetricResult.AuthenticationError(errString.toString()))
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                resultChannel.trySend(BioMetricResult.AuthenticationSuccess)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                resultChannel.trySend(BioMetricResult.AuthenticationCancelled)
            }
        })

        prompt.authenticate(promptInfo.build())
    }

    sealed interface BioMetricResult{
        data object HardWareUnavailable:BioMetricResult
        data object FeatureUnAvailable:BioMetricResult
        data class AuthenticationError(val msg: String): BioMetricResult
        data object AuthenticationSuccess:BioMetricResult
        data object AuthenticationCancelled:BioMetricResult
        data object AuthenticationNotSet:BioMetricResult
    }
}