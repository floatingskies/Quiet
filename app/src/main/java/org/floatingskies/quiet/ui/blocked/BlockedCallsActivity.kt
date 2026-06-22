package org.floatingskies.quiet.ui.blocked

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.floatingskies.quiet.App
import org.floatingskies.quiet.R
import org.floatingskies.quiet.data.BlockedCallEntity
import org.floatingskies.quiet.databinding.ActivityBlockedBinding
import org.floatingskies.quiet.databinding.ItemBlockedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlockedCallsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockedBinding
    private val adapter = BlockedAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.btnLimpar.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("Limpar todo o histórico de chamadas bloqueadas?")
                .setPositiveButton("Sim, limpar") { _, _ ->
                    lifecycleScope.launch {
                        App.instance.database.blockedCallDao().limparTudo()
                        carregar()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.btnExportar.setOnClickListener { exportarCsv() }

        carregar()
    }

    override fun onResume() {
        super.onResume()
        carregar()
    }

    private fun carregar() {
        lifecycleScope.launch {
            val lista = App.instance.database.blockedCallDao().listarTodos()
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

    private fun exportarCsv() {
        lifecycleScope.launch {
            val lista = withContext(Dispatchers.IO) {
                App.instance.database.blockedCallDao().listarTodos()
            }
            if (lista.isEmpty()) {
                android.widget.Toast.makeText(
                    this@BlockedCallsActivity,
                    "Nenhuma chamada bloqueada para exportar",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val csv = File(cacheDir, "chamadas_bloqueadas_${System.currentTimeMillis()}.csv")
            FileWriter(csv).use { writer ->
                writer.append("Telefone,DataHora,Vezes\n")
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
                lista.forEach {
                    writer.append("\"${it.telefoneOriginal}\",")
                    writer.append("\"${sdf.format(Date(it.dataHora))}\",")
                    writer.append("${it.vezes}\n")
                }
            }

            val uri = FileProvider.getUriForFile(
                this@BlockedCallsActivity,
                "${packageName}.fileprovider",
                csv
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Exportar CSV"))
        }
    }
}

class BlockedAdapter : RecyclerView.Adapter<BlockedAdapter.VH>() {

    private val items = mutableListOf<BlockedCallEntity>()
    private val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))

    fun submitList(novaLista: List<BlockedCallEntity>) {
        items.clear()
        items.addAll(novaLista)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemBlockedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBlockedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.txtTelefone.text = item.telefoneOriginal
        holder.binding.txtData.text = "Última tentativa: ${sdf.format(Date(item.dataHora))}"
        holder.binding.txtVezes.text = "Bloqueada ${item.vezes} vez(es)"
    }

    override fun getItemCount() = items.size
}
