package org.floatingskies.quiet.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Gerencia preferências persistentes (incluindo código de ativação).
 *
 * Usa SharedPreferences simples para manter compatibilidade com Android 4.4 (API 19).
 * O código de ativação não é dado sensível (é uma licença vitalícia, não uma senha),
 * então não há necessidade de criptografia forte.
 *
 * Em versões futuras, pode-se adicionar ofuscação leve (XOR com chave fixa) se
 * desejar dificultar a leitura direta por apps de root.
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bloqueador_prefs", Context.MODE_PRIVATE)

    // ===== Estado de ativação =====
    var codigoAtivacao: String?
        get() = prefs.getString(KEY_CODIGO, null)
        set(value) = prefs.edit().putString(KEY_CODIGO, value).apply()

    var ativado: Boolean
        get() = prefs.getBoolean(KEY_ATIVADO, false)
        set(value) = prefs.edit().putBoolean(KEY_ATIVADO, value).apply()

    var emailAtivacao: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var dataAtivacao: Long
        get() = prefs.getLong(KEY_DATA_ATIVACAO, 0)
        set(value) = prefs.edit().putLong(KEY_DATA_ATIVACAO, value).apply()

    // ===== Configurações =====
    var bloquearOcultos: Boolean
        get() = prefs.getBoolean(KEY_BLOQUEAR_OCULTOS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOQUEAR_OCULTOS, value).apply()

    var protecaoAtiva: Boolean
        get() = prefs.getBoolean(KEY_PROTECAO_ATIVA, true)
        set(value) = prefs.edit().putBoolean(KEY_PROTECAO_ATIVA, value).apply()

    var onboardingConcluido: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()

    var primeiroUso: Boolean
        get() = prefs.getBoolean(KEY_PRIMEIRO_USO, true)
        set(value) = prefs.edit().putBoolean(KEY_PRIMEIRO_USO, value).apply()

    companion object {
        private const val KEY_CODIGO = "codigo_ativacao"
        private const val KEY_ATIVADO = "ativado"
        private const val KEY_EMAIL = "email_ativacao"
        private const val KEY_DATA_ATIVACAO = "data_ativacao"
        private const val KEY_BLOQUEAR_OCULTOS = "bloquear_ocultos"
        private const val KEY_PROTECAO_ATIVA = "protecao_ativa"
        private const val KEY_ONBOARDING = "onboarding_concluido"
        private const val KEY_PRIMEIRO_USO = "primeiro_uso"
    }
}
