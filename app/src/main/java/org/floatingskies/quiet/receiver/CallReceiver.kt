package org.floatingskies.quiet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import org.floatingskies.quiet.App
import org.floatingskies.quiet.util.PhoneUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Method

/**
 * Receiver para Android 4.4 (API 19) até Android 6 (API 23).
 *
 * Nestas versões antigas, NÃO existe CallScreeningService (introduzido no 7.0).
 * O bloqueio é feito monitorando PHONE_STATE e usando reflexão para chamar
 * endCall() no ITelephony (API oculta).
 *
 * LIMITAÇÕES IMPORTANTES (documentadas no README):
 *   - Pode aparecer como "chamada perdida" em alguns fabricantes
 *   - Em Android 5+ o endCall() funciona com permissão MODIFY_PHONE_STATE
 *     (apenas apps de sistema têm). Em 4.4 funciona via reflexão.
 *   - Em Android 6 às vezes ainda toca 1 ring antes de desligar
 *
 * Para Android 7+ o bloqueio é 100% silencioso via CallBlockerService.
 */
class CallReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_NEW_OUTGOING_CALL) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val telefone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                Log.i(TAG, "RINGING: telefone=$telefone")
                scope.launch {
                    processarChamada(context, telefone)
                }
            }
        }
    }

    private suspend fun processarChamada(context: Context, telefone: String?) {
        val prefs = App.instance.prefs
        if (!prefs.protecaoAtiva) return

        val ehOculto = PhoneUtils.ehOculto(telefone)

        // Decide bloqueio
        val deveBloquear = when {
            ehOculto && prefs.bloquearOcultos -> true
            else -> {
                val whitelist = App.instance.database.whitelistDao().listarTodos()
                if (whitelist.isEmpty()) true
                else !whitelist.any { PhoneUtils.corresponde(telefone, it.telefone) }
            }
        }

        if (deveBloquear) {
            Log.i(TAG, "BLOQUEANDO chamada de: $telefone (oculto=$ehOculto)")
            bloquearChamada(context)
            registrarBloqueio(telefone, ehOculto)
        }
    }

    private fun bloquearChamada(context: Context) {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val m = Class.forName(tm.javaClass.name)
                .getDeclaredMethod("getITelephony")
            m.isAccessible = true
            val telephonyService = m.invoke(tm)

            val telephonyClass = Class.forName(telephonyService.javaClass.name)
            val endCall: Method = telephonyClass.getDeclaredMethod("endCall")
            endCall.invoke(telephonyService)

            Log.i(TAG, "Chamada encerrada via ITelephony.endCall()")
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao bloquear chamada (pode ser limitação da versão Android)", e)
        }
    }

    private suspend fun registrarBloqueio(telefone: String?, ehOculto: Boolean) {
        try {
            App.instance.database.blockedCallDao().registrarBloqueio(
                telefone = PhoneUtils.normalizar(telefone),
                telefoneOriginal = if (ehOculto) "Número oculto" else PhoneUtils.formatar(telefone)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar bloqueio", e)
        }
    }

    companion object {
        private const val TAG = "CallReceiver"
    }
}
