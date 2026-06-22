package org.floatingskies.quiet.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import org.floatingskies.quiet.App
import org.floatingskies.quiet.util.PhoneUtils
import org.floatingskies.quiet.util.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * CallScreeningService — Android 7.0 (API 24) ou superior.
 *
 * Esta é a forma OFICIAL e silenciosa de bloquear chamadas no Android moderno:
 *  - A chamada nunca toca, nunca aparece na tela, não gera "chamada perdida".
 *  - O usuário nem sabe que foi ligado.
 *
 * Em Android 10+ (API 29) exige-se que o app seja o app de telefone padrão para
 * que o CallScreeningService seja chamado. Em 7-9 funciona como serviço normal.
 *
 * Comportamento:
 *   - Se proteção desativada → libera tudo
 *   - Se número oculto e "bloquear ocultos" ligado → bloqueia
 *   - Se whitelist vazia → bloqueia tudo (modo paranóia)
 *   - Se número está na whitelist → libera
 *   - Senão → BLOQUEIA silenciosamente + registra no log
 */
@RequiresApi(Build.VERSION_CODES.N)
class CallBlockerService : CallScreeningService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs: PrefsManager by lazy { App.instance.prefs }

    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle
        val telefoneRecebido = handle?.schemeSpecificPart ?: ""

        // Numbers PRIVATE/UNKNOWN chegam com handle vazio
        val ehOculto = PhoneUtils.ehOculto(telefoneRecebido)

        scope.launch {
            val deveBloquear = decidirBloqueio(telefoneRecebido, ehOculto)

            if (deveBloquear) {
                Log.i(TAG, "Bloqueando chamada: $telefoneRecebido (oculto=$ehOculto)")
                registrarBloqueio(telefoneRecebido)
                responderBloqueio(callDetails)
            } else {
                Log.i(TAG, "Permitindo chamada: $telefoneRecebido")
                responderPermitir(callDetails)
            }
        }
    }

    private suspend fun decidirBloqueio(telefone: String, ehOculto: Boolean): Boolean {
        if (!prefs.protecaoAtiva) return false

        // 1. Número oculto/privado
        if (ehOculto && prefs.bloquearOcultos) return true

        // 2. Whitelist vazia → bloqueia tudo
        val whitelist = App.instance.database.whitelistDao().listarTodos()
        if (whitelist.isEmpty()) return true

        // 3. Está na whitelist?
        val permitido = whitelist.any { PhoneUtils.corresponde(telefone, it.telefone) }
        return !permitido
    }

    private suspend fun registrarBloqueio(telefone: String) {
        try {
            val telefoneOriginal = PhoneUtils.formatar(telefone)
            App.instance.database.blockedCallDao().registrarBloqueio(
                telefone = PhoneUtils.normalizar(telefone),
                telefoneOriginal = if (PhoneUtils.ehOculto(telefone)) "Número oculto" else telefoneOriginal
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar bloqueio", e)
        }
    }

    private fun responderBloqueio(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)              // recusa a chamada
            .setRejectCall(true)                // desliga na cara
            .setSkipCallLog(true)               // NÃO aparece no registro de chamadas
            .setSkipNotification(true)          // NÃO aparece notificação
            .build()
        respondToCall(callDetails, response)
    }

    private fun responderPermitir(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)
    }

    companion object {
        private const val TAG = "CallBlockerService"
    }
}
