package org.floatingskies.quiet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.floatingskies.quiet.util.PermissionHelper

/**
 * Reinicia os serviços quando o aparelho liga.
 *
 * Em Android 7+ o CallScreeningService é iniciado automaticamente pelo sistema
 * quando uma chamada chega. Em Android 4.4-6, o CallReceiver também é disparado
 * automaticamente. Este BootReceiver existe apenas para garantir que o app
 * permanece na lista de proteção ativa.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i("BootReceiver", "Boot concluído — proteção continua ativa")
        }
    }
}
