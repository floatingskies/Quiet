package org.floatingskies.quiet.ui.activation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.floatingskies.quiet.App
import org.floatingskies.quiet.MainActivity
import org.floatingskies.quiet.R
import org.floatingskies.quiet.databinding.ActivityActivationBinding
import org.floatingskies.quiet.util.ActivationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tela de ativação com código de 9 linhas.
 *
 * O usuário cola o código recebido por email. O app valida offline via
 * ActivationValidator.validar() — SHA-256 do corpo + assinatura.
 *
 * Após validação bem-sucedida:
 *  - Marca prefs.ativado = true
 *  - Salva o código (para reativar em caso de reinstall)
 *  - Volta para MainActivity
 */
class ActivationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActivationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Se já ativado, mostra mensagem
        if (App.instance.prefs.ativado) {
            binding.txtStatus.visibility = View.VISIBLE
            binding.txtStatus.text = getString(R.string.at_ja_ativado)
            binding.txtStatus.setTextColor(ContextCompat.getColor(this, R.color.verde))
        }

        binding.btnColar.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val texto = clip.getItemAt(0).coerceToText(this).toString()
                binding.edtCodigo.setText(texto)
                Toast.makeText(this, "Código colado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Área de transferência vazia", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAtivar.setOnClickListener {
            ativar()
        }
    }

    private fun ativar() {
        val codigo = binding.edtCodigo.text.toString()

        // Aceita tanto formato multilinhas quanto compactado (com pipes)
        val codigoNormalizado = if (codigo.contains("|")) {
            ActivationValidator.descompactar(codigo)
        } else {
            codigo
        }

        // Validação em background (SHA-256 é rápido mas não queremos travar UI)
        binding.btnAtivar.isEnabled = false
        binding.btnAtivar.text = "Validando…"

        Thread {
            val valido = ActivationValidator.validar(codigoNormalizado)
            runOnUiThread {
                binding.btnAtivar.isEnabled = true
                binding.btnAtivar.text = getString(R.string.at_btn_ativar)

                if (valido) {
                    App.instance.prefs.ativado = true
                    App.instance.prefs.codigoAtivacao = codigoNormalizado
                    App.instance.prefs.dataAtivacao = System.currentTimeMillis()

                    binding.txtStatus.visibility = View.VISIBLE
                    binding.txtStatus.text = getString(R.string.at_sucesso)
                    binding.txtStatus.setTextColor(ContextCompat.getColor(this, R.color.verde))

                    Toast.makeText(this, getString(R.string.at_sucesso), Toast.LENGTH_LONG).show()

                    // Volta para dashboard
                    startActivity(Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
                } else {
                    binding.txtStatus.visibility = View.VISIBLE
                    binding.txtStatus.text = getString(R.string.at_erro_invalido)
                    binding.txtStatus.setTextColor(ContextCompat.getColor(this, R.color.vermelho))
                }
            }
        }.start()
    }
}
