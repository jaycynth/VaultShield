package com.techne.vaultshield.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureClipboardManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var clearJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun copyToClipboard(label: String, text: String, timeoutMillis: Long = 30000) {
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        clearJob?.cancel()
        clearJob = scope.launch {
            delay(timeoutMillis)
            clearClipboard()
        }
    }

    private fun clearClipboard() {
        if (clipboard.hasPrimaryClip()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            } else {
                clipboard.setPrimaryClip(ClipData.newPlainText(null, ""))
            }
        }
    }
}
