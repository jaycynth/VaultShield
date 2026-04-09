package com.techne.vaultshield.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techne.vaultshield.domain.model.OtpAccount
import com.techne.vaultshield.security.TotpGenerator
import com.techne.vaultshield.ui.audit.AuditLogScreen
import com.techne.vaultshield.ui.audit.AuditViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onAddClick: () -> Unit,
    auditViewModel: AuditViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VaultShield") },
                actions = {
                    if (state.isAuthenticated) {
                        IconButton(onClick = { onIntent(HomeIntent.ToggleAudit) }) {
                            Icon(
                                if (state.isAuditVisible) Icons.AutoMirrored.Filled.ListAlt else Icons.Default.History,
                                contentDescription = "Security Audit"
                            )
                        }
                        IconButton(onClick = { onIntent(HomeIntent.LockVault) }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isAuthenticated && !state.isAuditVisible) {
                FloatingActionButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Account")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!state.isDeviceSecure) {
                SecurityWarningBanner(state.error ?: "Security Risk Detected")
            }

            if (!state.isAuthenticated) {
                AuthRequiredScreen(onAuthenticate = { onIntent(HomeIntent.Authenticate) })
            } else {
                if (state.isAuditVisible) {
                    val auditState by auditViewModel.state.collectAsState()
                    AuditLogScreen(auditState.auditLogs)
                } else {
                    AccountList(state.accounts, onIntent)
                }
            }
        }
    }
}

@Composable
fun SecurityWarningBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AuthRequiredScreen(onAuthenticate: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Vault is Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Authenticate to view your secure codes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onAuthenticate,
                modifier = Modifier.height(56.dp).fillMaxWidth(0.6f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Unlock with Biometrics")
            }
        }
    }
}

@Composable
fun AccountList(accounts: List<OtpAccount>, onIntent: (HomeIntent) -> Unit) {
    if (accounts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No accounts yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts, key = { it.id }) { account ->
                OtpAccountItem(
                    account = account,
                    onDelete = { onIntent(HomeIntent.DeleteAccount(account.id)) },
                    onCopy = { onIntent(HomeIntent.CopyOtp(account)) },
                    onReveal = { onIntent(HomeIntent.RevealAccount(account.id)) }
                )
            }
        }
    }
}

@Composable
fun OtpAccountItem(
    account: OtpAccount,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onReveal: () -> Unit
) {
    var otpCode by remember { mutableStateOf(TotpGenerator.generateTotp(account.secret)) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            val currentTime = System.currentTimeMillis() / 1000
            val remainingSeconds = 30 - (currentTime % 30)
            progress = remainingSeconds / 30f
            otpCode = TotpGenerator.generateTotp(account.secret)
            delay(1000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.issuer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = account.accountName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                if (account.isRevealed) {
                    Text(
                        text = otpCode.chunked(3).joinToString(" "),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                } else {
                    Button(onClick = onReveal, modifier = Modifier.height(48.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reveal Code")
                    }
                }
            }
            if (account.isRevealed) {
                Column {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
