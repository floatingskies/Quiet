package org.floatingskies.quiet.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.floatingskies.quiet.MainActivity
import org.floatingskies.quiet.R
import org.floatingskies.quiet.databinding.ActivityOnboardingBinding
import org.floatingskies.quiet.util.PermissionHelper

/**
 * Tela de boas-vindas.
 *
 * Fluxo:
 *   1. Usuário vê os benefícios
 *   2. Concede todas as permissões (uma a uma)
 *   3. Concede sobreposição (Android 6+)
 *   4. Concede ignorar bateria (Android 6+)
 *   5. Vai para a MainActivity
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    private val pedirMultiplasPermissoes = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        atualizarStatusPermissoes()
        if (PermissionHelper.todasConcedidas(this)) {
            binding.btnProximo.isEnabled = true
            binding.btnProximo.text = getString(R.string.onb_btn_comecar)
            // Auto-passa para overlay e bateria
            checarOverlayEBateria()
        } else {
            Toast.makeText(this, "Conceda todas as permissões para o app funcionar", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        montarListaPermissoes()

        binding.btnConceder.setOnClickListener {
            PermissionHelper.pedirTodas(this)
            // O resultado chega via pedirMultiplasPermissoes — registra via callback
            // mas como o requestPermissions faz request direto, registrar manualmente:
            registrarCallbackPermissoes()
        }

        binding.btnProximo.setOnClickListener {
            if (PermissionHelper.todasConcedidas(this)) {
                org.floatingskies.quiet.App.instance.prefs.onboardingConcluido = true
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Conceda todas as permissões primeiro", Toast.LENGTH_SHORT).show()
            }
        }

        // Já concedidas? Habilita direto
        if (PermissionHelper.todasConcedidas(this)) {
            binding.btnProximo.isEnabled = true
            binding.btnConceder.text = "Permissões OK ✓"
        }
    }

    private fun registrarCallbackPermissoes() {
        // Como o PermissionHelper.pedirTodas() usa requestPermissions() com requestCode RC_PERMISSOES,
        // interceptamos via onRequestPermissionsResult
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.RC_PERMISSOES) {
            atualizarStatusPermissoes()
            if (PermissionHelper.todasConcedidas(this)) {
                binding.btnProximo.isEnabled = true
                binding.btnConceder.text = "Permissões OK ✓"
                checarOverlayEBateria()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PermissionHelper.RC_OVERLAY,
            PermissionHelper.RC_BATERIA,
            PermissionHelper.RC_ROLE_CALL_SCREENING -> atualizarStatusPermissoes()
        }
    }

    private fun montarListaPermissoes() {
        val lista = binding.listaPermissoes
        lista.removeAllViews()

        val itens = listOf(
            ItemPermissao(R.string.onb_perm_phone, R.string.onb_perm_contacts, "phone") {
                PermissionHelper.todasConcedidas(this)
            },
            ItemPermissao(R.string.onb_perm_contacts, 0, "contacts") {
                checkSelfPermission(android.Manifest.permission.READ_CONTACTS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            },
            ItemPermissao(R.string.onb_perm_notifications, 0, "notif") {
                android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            },
            ItemPermissao(R.string.onb_perm_overlay, 0, "overlay") {
                PermissionHelper.temOverlay(this)
            },
            ItemPermissao(R.string.onb_perm_battery, 0, "battery") {
                PermissionHelper.temIgnorarBateria(this)
            },
            ItemPermissao(R.string.onb_perm_padrao, 0, "padrao") {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) true
                else PermissionHelper.temRoleCallScreening(this)
            }
        )

        for (item in itens) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_permissao, lista, false)
            val txtTitulo = view.findViewById<TextView>(R.id.txtTitulo)
            val txtStatus = view.findViewById<TextView>(R.id.txtStatus)
            txtTitulo.setText(item.tituloRes)
            txtStatus.text = if (item.verificador()) "OK ✓" else "Conceder"
            val cor = if (item.verificador())
                ContextCompat.getColor(this, R.color.verde)
            else
                ContextCompat.getColor(this, R.color.ciano)
            txtStatus.setTextColor(cor)
            view.setOnClickListener {
                when (item.id) {
                    "phone", "notif" -> {
                        PermissionHelper.pedirTodas(this)
                    }
                    "contacts" -> {
                        requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 1002)
                    }
                    "overlay" -> PermissionHelper.pedirOverlay(this)
                    "battery" -> PermissionHelper.pedirIgnorarBateria(this)
                    "padrao" -> PermissionHelper.pedirRoleCallScreening(this)
                }
            }
            lista.addView(view)
        }
    }

    private fun atualizarStatusPermissoes() {
        montarListaPermissoes()
    }

    private fun checarOverlayEBateria() {
        if (!PermissionHelper.temOverlay(this)) {
            Toast.makeText(this, "Conceda também a permissão de sobrepor apps", Toast.LENGTH_LONG).show()
            PermissionHelper.pedirOverlay(this)
        } else if (!PermissionHelper.temIgnorarBateria(this)) {
            Toast.makeText(this, "Permita ignorar otimização de bateria para a proteção não ser desligada", Toast.LENGTH_LONG).show()
            PermissionHelper.pedirIgnorarBateria(this)
        }
    }

    private fun ehDialerPadrao(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                val tm = getSystemService(android.telecom.TelecomManager::class.java)
                tm?.defaultDialerPackage == packageName
            } catch (_: Exception) {
                false
            }
        } else true
    }

    private data class ItemPermissao(
        val tituloRes: Int,
        val descRes: Int,
        val id: String,
        val verificador: () -> Boolean
    )
}
