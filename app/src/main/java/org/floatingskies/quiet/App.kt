package org.floatingskies.quiet

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import org.floatingskies.quiet.data.AppDatabase
import org.floatingskies.quiet.util.PrefsManager

/**
 * Classe Application principal.
 * Inicializa o banco de dados Room, preferências e canais de notificação.
 */
class App : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    lateinit var prefs: PrefsManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = PrefsManager(this)
        criarCanaisNotificacao()
    }

    private fun criarCanaisNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal silencioso para avisos de bloqueio
            val canalSilencioso = NotificationChannel(
                CANAL_BLOQUEIO,
                "Chamadas bloqueadas",
                NotificationManager.IMPORTANCE_LOW  // sem som, sem vibração
            ).apply {
                description = "Avisos de chamadas bloqueadas (silencioso)"
                setShowBadge(false)
            }

            // Canal para serviço foreground (mantém o app vivo em segundo plano)
            val canalServico = NotificationChannel(
                CANAL_SERVICO,
                "Proteção ativa",
                NotificationManager.IMPORTANCE_MIN  // totalmente silencioso
            ).apply {
                description = "Mantém a proteção de chamadas ativa em segundo plano"
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canalSilencioso)
            manager.createNotificationChannel(canalServico)
        }
    }

    companion object {
        const val CANAL_BLOQUEIO = "canal_bloqueio"
        const val CANAL_SERVICO = "canal_servico"

        lateinit var instance: App
            private set
    }
}
