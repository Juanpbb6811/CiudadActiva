package com.ciudadactiva.app.ui.components

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
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

    /**
     * Crea el Intent oficial del sistema Android para mostrar el panel modal
     * con la lista de todas las cuentas de Google (@gmail.com / institucionales)
     * vinculadas al dispositivo móvil del usuario.
     */
    fun crearIntentSelectorCuentasGoogle(): Intent {
        return AccountManager.newChooseAccountIntent(
            null, // Cuenta preseleccionada
            null, // Cuentas permitidas
            arrayOf("com.google"), // Filtro exclusivo para cuentas de Google
            null, // Descripción personalizada
            null, // Token type
            null, // Required features
            null  // Opciones adicionales
        )
    }

    /**
     * Procesa la cuenta seleccionada por el usuario en el panel nativo de Android.
     */
    fun procesarResultadoSelector(resultCode: Int, data: Intent?): Pair<String, String>? {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val accountEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountEmail.isNullOrBlank()) {
                val inferredName = accountEmail.substringBefore("@")
                    .replace(".", " ")
                    .split(" ")
                    .joinToString(" ") { palabra -> palabra.replaceFirstChar { it.uppercase() } }
                return Pair(accountEmail.trim().lowercase(), inferredName)
            }
        }
        return null
    }

    /**
     * Intenta autenticación federada con Credential Manager si GOOGLE_WEB_CLIENT_ID
     * está configurado, o delega al selector nativo de cuentas.
     */
    fun iniciarGoogleSignIn(
        context: Context,
        coroutineScope: CoroutineScope,
        onSuccess: (email: String, displayName: String) -> Unit,
        onLaunchNativePicker: () -> Unit
    ) {
        val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()

        if (serverClientId.isBlank()) {
            Log.d(TAG, "GOOGLE_WEB_CLIENT_ID no presente en local.properties. Abriendo panel nativo de cuentas de Google.")
            onLaunchNativePicker()
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
                        onSuccess(email.trim().lowercase(), displayName)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onLaunchNativePicker()
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "Inicio con Google cancelado por el usuario.")
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Error en Credential Manager: ${e.message}. Usando selector nativo.", e)
                withContext(Dispatchers.Main) {
                    onLaunchNativePicker()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fallo inesperado: ${e.message}. Usando selector nativo.", e)
                withContext(Dispatchers.Main) {
                    onLaunchNativePicker()
                }
            }
        }
    }
}
