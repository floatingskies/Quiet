package org.floatingskies.quiet.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.floatingskies.quiet.App
import org.floatingskies.quiet.R
import org.floatingskies.quiet.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val prefs = App.instance.prefs

        // Carrega estado atual
        binding.switchProtecao.isChecked = prefs.protecaoAtiva
        binding.switchOcultos.isChecked = prefs.bloquearOcultos

        // Status do plano
        binding.txtStatusPlano.text = if (prefs.ativado) {
            "✓ Versão vitalícia ativada"
        } else {
            "Versão gratuita (até 5 contatos)"
        }
        binding.txtStatusPlano.setTextColor(
            if (prefs.ativado) ContextCompat.getColor(this, R.color.verde)
            else ContextCompat.getColor(this, R.color.amarelo)
        )

        // Listeners
        binding.switchProtecao.setOnCheckedChangeListener { _, checked ->
            prefs.protecaoAtiva = checked
        }
        binding.switchOcultos.setOnCheckedChangeListener { _, checked ->
            prefs.bloquearOcultos = checked
        }
    }
}
