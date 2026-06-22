package org.floatingskies.quiet.ui.whitelist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import org.floatingskies.quiet.App
import org.floatingskies.quiet.R
import org.floatingskies.quiet.data.WhitelistEntity
import org.floatingskies.quiet.databinding.ActivityWhitelistBinding
import org.floatingskies.quiet.databinding.DialogAddWhitelistBinding
import org.floatingskies.quiet.databinding.DialogDigitarNumeroBinding
import org.floatingskies.quiet.databinding.ItemWhitelistBinding
import org.floatingskies.quiet.util.PhoneUtils
import kotlinx.coroutines.launch

class WhitelistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhitelistBinding
    private val adapter = WhitelistAdapter { item -> confirmarRemocao(item) }

    private val pegarContato = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            resolverContato(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhitelistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.fabAdd.setOnClickListener { mostrarDialogAdicionar() }

        carregar()
    }

    override fun onResume() {
        super.onResume()
        carregar()
    }

    private fun carregar() {
        lifecycleScope.launch {
            val lista = App.instance.database.whitelistDao().listarTodos()

            binding.txtContagem.text = if (App.instance.prefs.ativado) {
                getString(R.string.wl_limite_premium, lista.size)
            } else {
                getString(R.string.wl_limite_trial, lista.size)
            }

            if (lista.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recycler.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recycler.visibility = View.VISIBLE
                adapter.submitList(lista)
            }
        }
    }

    private fun mostrarDialogAdicionar() {
        val dialogBinding = DialogAddWhitelistBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnImportar.setOnClickListener {
            dialog.dismiss()
            abrirSeletorContato()
        }
        dialogBinding.btnDigitar.setOnClickListener {
            dialog.dismiss()
            mostrarDialogDigitar()
        }
        dialogBinding.btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarDialogDigitar() {
        val dialogBinding = DialogDigitarNumeroBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnSalvar.setOnClickListener {
            val nome = dialogBinding.edtNome.text.toString().trim()
            val telefone = dialogBinding.edtTelefone.text.toString().trim()
            if (nome.isBlank() || telefone.isBlank()) {
                return@setOnClickListener
            }
            salvar(nome, telefone)
            dialog.dismiss()
        }
        dialogBinding.btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun abrirSeletorContato() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        pegarContato.launch(intent)
    }

    private fun resolverContato(uri: android.net.Uri) {
        try {
            contentResolver.query(
                uri, arrayOf(
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                ), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nome = cursor.getString(0)
                    val telefone = cursor.getString(1)
                    salvar(nome, telefone)
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Erro ao ler contato", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun salvar(nome: String, telefone: String) {
        lifecycleScope.launch {
            val prefs = App.instance.prefs
            val db = App.instance.database
            // Trial: limite 30 contatos. Vitalício: ilimitado.
            if (!prefs.ativado) {
                val atual = db.whitelistDao().contar()
                if (atual >= 30) {
                    android.widget.Toast.makeText(
                        this@WhitelistActivity,
                        getString(R.string.wl_limite_atingido),
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
            }
            val normalizado = PhoneUtils.normalizar(telefone)
            if (db.whitelistDao().buscarPorTelefone(normalizado) != null) {
                android.widget.Toast.makeText(
                    this@WhitelistActivity,
                    getString(R.string.wl_erro_duplicado),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            db.whitelistDao().inserir(WhitelistEntity(
                nome = nome,
                telefone = normalizado,
                telefoneOriginal = PhoneUtils.formatar(telefone)
            ))
            carregar()
        }
    }

    private fun confirmarRemocao(item: WhitelistEntity) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.wl_remover_pergunta, item.nome))
            .setPositiveButton(R.string.wl_sim) { _, _ ->
                lifecycleScope.launch {
                    App.instance.database.whitelistDao().deletar(item)
                    carregar()
                }
            }
            .setNegativeButton(R.string.wl_cancelar, null)
            .show()
    }
}

class WhitelistAdapter(
    private val onRemover: (WhitelistEntity) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<WhitelistAdapter.VH>() {

    private val items = mutableListOf<WhitelistEntity>()

    fun submitList(novaLista: List<WhitelistEntity>) {
        items.clear()
        items.addAll(novaLista)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemWhitelistBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemWhitelistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.txtNome.text = item.nome
        holder.binding.txtTelefone.text = item.telefoneOriginal
        holder.binding.btnRemover.setOnClickListener { onRemover(item) }
    }

    override fun getItemCount() = items.size
}
