package com.ciudadactiva.app.ui.components

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.ciudadactiva.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object GoogleAuthHelper {

    private const val TAG = "GoogleAuthHelper"

    fun iniciarGoogleSignIn(
        context: Context,
        coroutineScope: CoroutineScope,
        onSuccess: (email: String, displayName: String) -> Unit,
        onError: (String) -> Unit,
        onFallbackPrompt: () -> Unit
    ) {
        val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()

        // Si todavía no han pegado la clave en local.properties, abrir el diálogo seguro de Google
        if (serverClientId.isBlank()) {
            Log.d(TAG, "GOOGLE_WEB_CLIENT_ID no configurado en local.properties. Abriendo selector de cuenta.")
            onFallbackPrompt()
            return
        }

        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(serverClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleIdToken.id
                    val displayName = googleIdToken.displayName ?: email.substringBefore("@")
                    withContext(Dispatchers.Main) {
                        onSuccess(email, displayName)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError("Tipo de credencial no soportado")
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "Inicio de sesión con Google cancelado por el usuario.")
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Error en Credential Manager: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    // Fallback amigable al prompt si falta configurar SHA1 o Client ID en Google Cloud
                    onFallbackPrompt()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción inesperada en Google Sign-In: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onFallbackPrompt()
                }
            }
        }
    }
}
