package org.floatingskies.quiet.ui.dialer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log

/**
 * Activity "proxy" que faz o app aparecer na lista de "app de telefone padrão"
 * nas configurações do Android 10+.
 *
 * Quando o usuário define ESTE app como discador padrão, o sistema passa a
 * chamar nosso CallScreeningService para toda chamada recebida — isso é
 * obrigatório para o bloqueio 100% silencioso em Android 10+.
 *
 * Esta activity NÃO implementa um discador próprio. Ela apenas:
 *   1. Recebe o intent DIAL (quando o usuário clica num número em qualquer app)
 *   2. Redireciona para o discador nativo do sistema (Google Phone, Samsung Phone, etc.)
 *
 * Assim o usuário continua usando o discador que já conhece, mas nosso
 * CallScreeningService fica ativo para bloquear chamadas recebidas.
 */
class ProxyDialerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentRecebido = intent
        Log.i(TAG, "Intent recebido: ${intentRecebido?.action} data=${intentRecebido?.data}")

        try {
            // Monta um novo intent para o discador nativo do sistema
            val intentNativo = Intent(Intent.ACTION_DIAL).apply {
                data = intentRecebido?.data
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Remove nosso app da lista de resolvedores para não ficar em loop
            val pacoteAtual = packageName
            val escolhas = packageManager.queryIntentActivities(intentNativo, 0)
                .filter { it.activityInfo.packageName != pacoteAtual }

            if (escolhas.isNotEmpty()) {
                // Abre o primeiro discador nativo encontrado
                val alvo = escolhas.first()
                intentNativo.setPackage(alvo.activityInfo.packageName)
                startActivity(intentNativo)
            } else {
                // Fallback: abre o discador padrão sem número (só abre o teclado)
                val fallback = Intent(Intent.ACTION_DIAL)
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(fallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao redirecionar para discador nativo", e)
        }

        // Finaliza imediatamente — esta activity não tem UI
        finish()
    }

    companion object {
        private const val TAG = "ProxyDialerActivity"
    }
}
