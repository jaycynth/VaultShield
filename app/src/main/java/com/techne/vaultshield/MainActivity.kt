package com.techne.vaultshield

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.techne.vaultshield.ui.home.HomeIntent
import com.techne.vaultshield.ui.home.HomeScreen
import com.techne.vaultshield.ui.home.HomeViewModel
import com.techne.vaultshield.ui.theme.VaultShieldTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setHideOverlayWindows(true)
        }

        enableEdgeToEdge()
        
        executor = ContextCompat.getMainExecutor(this)
        
        setContent {
            VaultShieldTheme {
                val viewModel: HomeViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                
                HomeScreen(
                    state = state,
                    onIntent = { intent ->
                        when (intent) {
                            is HomeIntent.Authenticate -> showBiometricPrompt {
                                viewModel.handleIntent(HomeIntent.Authenticate)
                            }
                            else -> viewModel.handleIntent(intent)
                        }
                    },
                    onAddClick = {
                        viewModel.handleIntent(HomeIntent.AddAccount)
                    }
                )
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                           BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        val canAuthenticate = biometricManager.canAuthenticate(authenticators)
        
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(applicationContext, "Auth error: $errString", Toast.LENGTH_SHORT).show()
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("VaultShield Secure Access")
                .setSubtitle("Authenticate to continue")
                .setAllowedAuthenticators(authenticators)
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            // In a real high-security app, you might block access if no lock is set.
            // For this, we allow bypass but log it.
            onSuccess()
            Toast.makeText(this, "Security Warning: No biometric/PIN setup", Toast.LENGTH_SHORT).show()
        }
    }
}
