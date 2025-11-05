package com.revanth.biometric

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revanth.biometric.ui.theme.BiometricTheme

class MainActivity : AppCompatActivity() {

    private val bioMetricPromptManager by lazy {
        BioMetricPromptManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiometricTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val biometricResult by bioMetricPromptManager.promptResults.collectAsStateWithLifecycle(null)

                    val enrollLauncher= rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult(), onResult = {
                        println(it.resultCode)
                    })
                    LaunchedEffect(biometricResult) {
                        if(biometricResult is BioMetricPromptManager.BioMetricResult.AuthenticationNotSet){
                            if(Build.VERSION.SDK_INT>=30){
                                val enrollIntent= Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                    putExtra(
                                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                    )
                                }
                                enrollLauncher.launch(enrollIntent)
                            }
                        }
                    }
                    Column(
                        Modifier.padding(innerPadding).fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                bioMetricPromptManager.showBiometricPrompt(
                                    title = "Biometric Prompt",
                                    description = "Biometric Prompt Description",
                                    negativeButtonText = "Cancel"
                                )
                            }
                        ) {
                            Text("Show Prompt")
                        }

                        biometricResult?.let { result->
                            Text(
                                text=when(result){
                                    is BioMetricPromptManager.BioMetricResult.AuthenticationSuccess->"Success"
                                    is BioMetricPromptManager.BioMetricResult.AuthenticationError->{
                                        result.msg
                                    }
                                    is BioMetricPromptManager.BioMetricResult.AuthenticationCancelled->"Cancelled"
                                    is BioMetricPromptManager.BioMetricResult.HardWareUnavailable->"HardWareUnavailable"
                                    is BioMetricPromptManager.BioMetricResult.AuthenticationNotSet->"AuthenticationNotSet"
                                    is BioMetricPromptManager.BioMetricResult.FeatureUnAvailable->"FeatureUnAvailable"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

