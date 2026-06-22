package org.floatingskies.quiet.ui.payment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.floatingskies.quiet.App
import org.floatingskies.quiet.R
import org.floatingskies.quiet.databinding.ActivityPaymentBinding
import org.floatingskies.quiet.ui.activation.ActivationActivity

/**
 * Tela de doação via PIX.
 *
 * Em vez de gerar QR Code dinamicamente, usa o link oficial de cobrança do Nubank:
 *   https://nubank.com.br/cobrar/dki0p/6a39c86d-cefb-4b6a-b643-b3dca408739b
 *
 * Quando o usuário toca em "Abrir app do banco", o Android abre o link e o app
 * do banco instalado (Nubank, Itaú, BB, Caixa, etc.) intercepta automaticamente,
 * preenchendo valor e favorecido.
 *
 * Como fallback (se o usuário não tem app de banco), mostra as chaves PIX manuais
 * e o código "PIX Copia e Cola" que pode ser copiado.
 */
class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding

    companion object {
        // Link oficial de cobrança do Nubank — abre direto no app do banco
        private const val LINK_NUBANK =
            "https://nubank.com.br/cobrar/dki0p/6a39c86d-cefb-4b6a-b643-b3dca408739b"

        // PIX Copia e Cola (payload BR Code) — pode ser colado em qualquer app de banco
        private const val PIX_COPIA_E_COLA =
            "00020126360014BR.GOV.BCB.PIX0114+556999342713252040000530398654044.995802BR5918Ariel Closs Novais6009SAO PAULO62140510abhrjthoq1630490B3"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Botão principal: abre o link de cobrança do Nubank
        // O Android vai perguntar qual app usar (Nubank, Itaú, BB, etc.) — ou abrir no navegador
        binding.btnAbrirBanco.setOnClickListener {
            abrirAppBanco()
        }

        // Botão secundário: copia o PIX Copia e Cola para a área de transferência
        binding.btnCopiarPix.setOnClickListener {
            copiarPix()
        }

        binding.btnConfirmar.setOnClickListener {
            confirmarPagamento()
        }
    }

    /** Abre o link de cobrança do Nubank. O app do banco instalado intercepta automaticamente. */
    private fun abrirAppBanco() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LINK_NUBANK)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Toast.makeText(
                this,
                "Abrindo app do banco... Confirme o valor de R$ 4,99",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            // Se nenhum app conseguir abrir, mostra o link para o usuário digitar manualmente
            Toast.makeText(
                this,
                "Não foi possível abrir o app do banco. Copie o PIX Copia e Cola.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Copia o código PIX Copia e Cola para a área de transferência. */
    private fun copiarPix() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("PIX Copia e Cola", PIX_COPIA_E_COLA)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            this,
            "PIX Copia e Cola copiado! Cole no seu app de banco.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun confirmarPagamento() {
        val email = binding.edtEmail.text.toString().trim()
        val comprovante = binding.edtComprovante.text.toString().trim()

        if (email.isBlank() || !email.contains("@")) {
            Toast.makeText(this, getString(R.string.pag_erro_email), Toast.LENGTH_SHORT).show()
            return
        }

        // Salva o email para uso posterior
        App.instance.prefs.emailAtivacao = email

        Toast.makeText(this, getString(R.string.pag_sucesso), Toast.LENGTH_LONG).show()

        // Vai para tela de ativação
        val intent = Intent(this, ActivationActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }
}
