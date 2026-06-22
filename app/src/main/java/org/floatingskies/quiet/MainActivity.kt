package org.floatingskies.quiet

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import org.floatingskies.quiet.databinding.ActivityMainBinding
import org.floatingskies.quiet.ui.activation.ActivationActivity
import org.floatingskies.quiet.ui.blocked.BlockedCallsActivity
import org.floatingskies.quiet.ui.payment.PaymentActivity
import org.floatingskies.quiet.ui.settings.SettingsActivity
import org.floatingskies.quiet.ui.whitelist.WhitelistActivity
import kotlinx.coroutines.launch

/**
 * Tela principal (dashboard).
 *
 * Mostra o status da proteção e atalhos para as 4 principais áreas:
 *  - Whitelist
 *  - Chamadas bloqueadas
 *  - Ativar versão completa
 *  - Configurações
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        atualizarStatus()
        configurarCliques()
    }

    override fun onResume() {
        super.onResume()
        atualizarStatus()
    }

    private fun atualizarStatus() {
        val prefs = App.instance.prefs
        val db = App.instance.database

        // Toggle de proteção
        binding.switchProtecao.setOnCheckedChangeListener(null)
        binding.switchProtecao.isChecked = prefs.protecaoAtiva
        binding.switchProtecao.setOnCheckedChangeListener { _, isChecked ->
            prefs.protecaoAtiva = isChecked
            atualizarCardStatus()
        }
        atualizarCardStatus()

        // Plano
        if (prefs.ativado) {
            binding.txtPlano.text = getString(R.string.dash_premium_msg)
            binding.txtPlano.setTextColor(ContextCompat.getColor(this, R.color.verde))
            binding.txtPlano.setBackgroundColor(ContextCompat.getColor(this, R.color.ativo_verde))
            binding.cardAtivar.visibility = View.GONE
        } else {
            binding.txtPlano.text = getString(R.string.dash_trial_msg)
            binding.txtPlano.setTextColor(ContextCompat.getColor(this, R.color.amarelo))
            binding.cardAtivar.visibility = View.VISIBLE
        }

        // Contadores
        lifecycleScope.launch {
            val whitelist = db.whitelistDao().listarTodos()
            val bloqueadas = db.blockedCallDao().contar()

            val limite = if (prefs.ativado) Int.MAX_VALUE else 30
            binding.txtWhitelistCount.text = if (prefs.ativado) {
                "${whitelist.size} contatos (ilimitado) ❤️"
            } else {
                "${whitelist.size} de $limite contato(s) na lista"
            }

            binding.txtBlockedCount.text = if (bloqueadas == 0) {
                getString(R.string.dash_card_blocked_desc)
            } else {
                "$bloqueadas número(s) bloqueado(s)"
            }
        }
    }

    private fun atualizarCardStatus() {
        val prefs = App.instance.prefs
        if (prefs.protecaoAtiva) {
            binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.ativo_verde))
            binding.cardStatus.strokeColor = ContextCompat.getColor(this, R.color.ativo_verde_borda)
            binding.txtStatus.text = getString(R.string.dash_status_ativo)
            binding.txtStatus.setTextColor(ContextCompat.getColor(this, R.color.verde))
            binding.txtStatusDesc.text = getString(R.string.dash_status_bloqueando)
        } else {
            binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.inativo_vermelho))
            binding.cardStatus.strokeColor = ContextCompat.getColor(this, R.color.inativo_vermelho_borda)
            binding.txtStatus.text = getString(R.string.dash_status_inativo)
            binding.txtStatus.setTextColor(ContextCompat.getColor(this, R.color.vermelho))
            binding.txtStatusDesc.text = getString(R.string.dash_status_livre)
        }
    }

    private fun configurarCliques() {
        binding.cardWhitelist.setOnClickListener {
            startActivity(Intent(this, WhitelistActivity::class.java))
        }
        binding.cardBlocked.setOnClickListener {
            startActivity(Intent(this, BlockedCallsActivity::class.java))
        }
        binding.cardAtivar.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }
        binding.cardConfig.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
