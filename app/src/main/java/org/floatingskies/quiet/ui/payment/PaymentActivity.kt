package org.floatingskies.quiet.ui.payment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.floatingskies.quiet.App
import org.floatingskies.quiet.R
import org.floatingskies.quiet.databinding.ActivityPaymentBinding
import org.floatingskies.quiet.ui.activation.ActivationActivity
import org.floatingskies.quiet.util.PhoneUtils
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.UUID

/**
 * Tela de pagamento via PIX.
 *
 * Gera um QR Code PIX estático (com a chave PIX do receptor + valor).
 * Após o usuário confirmar o pagamento com email + comprovante, o pedido é registrado.
 *
 * IMPORTANTE: a geração do código de ativação é feita offline via ActivationValidator.
 * O fluxo real é:
 *   1. Usuário paga o PIX no banco
 *   2. Informa email e comprovante aqui
 *   3. O app registra o pedido (em produção, chamaria backend)
 *   4. Backend valida o comprovante e devolve um código gerado por ActivationValidator.gerarCodigo()
 *   5. Usuário recebe o código por email e cola na tela de Ativação
 *
 * Para esta DEMO: o app gera um código imediatamente e mostra na próxima tela,
 * simulando a chegada do email.
 */
class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        gerarQrCodePix()

        binding.btnConfirmar.setOnClickListener {
            confirmarPagamento()
        }
    }

    private fun gerarQrCodePix() {
        try {
            // Gera um "BR Code" PIX estático com a chave do desenvolvedor
            val chavePix = "arielcloss@gmail.com"  // NUBANK
            val nome = "ARIEL CLOSS"
            val cidade = "SAO PAULO"
            val valor = "1.30"
            val txid = "BRTX" + UUID.randomUUID().toString().take(8).uppercase()

            val payload = montarPayloadPix(chavePix, nome, cidade, valor, txid)
            binding.txtChavePix.text = "Chave PIX (email):\narielcloss@gmail.com\nBanco: NUBANK\n\nPIX Copia e Cola:\n$payload"

            val encoder = BarcodeEncoder()
            val bitmap: Bitmap = encoder.encodeBitmap(payload, BarcodeFormat.QR_CODE, 480, 480)
            binding.imgQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao gerar QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Monta um payload BR Code (PIX) simplificado.
     * Em produção, usar uma biblioteca oficial de emissão de QR Code PIX.
     */
    private fun montarPayloadPix(chave: String, nome: String, cidade: String, valor: String, txid: String): String {
        fun field(id: String, value: String) = "%02d%02d%s".format(id.toInt(), value.length, value)

        val merchantAccount = field("00", "br.gov.bcb.pix") + field("01", chave)
        val additional = field("05", txid)
        val payloadSemCrc = listOf(
            field("00", "01"),
            field("01", "12"),  // BR Code estático
            field("26", merchantAccount + additional),
            field("52", "0000"),
            field("53", "986"),  // BRL
            field("54", valor),
            field("58", "BR"),
            field("59", nome),
            field("60", cidade),
            field("62", field("05", txid))
        ).joinToString("")

        // CRC16/CCITT-FALSE
        val crc = calcularCrc16(payloadSemCrc + "6304")
        return payloadSemCrc + "6304" + "%04X".format(crc)
    }

    private fun calcularCrc16(input: String): Int {
        var crc = 0xFFFF
        for (b in input.toByteArray()) {
            crc = crc xor (b.toInt() and 0xFF shl 8)
            for (i in 0 until 8) {
                crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1
                crc = crc and 0xFFFF
            }
        }
        return crc and 0xFFFF
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
