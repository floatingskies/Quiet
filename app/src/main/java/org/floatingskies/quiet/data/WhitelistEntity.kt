package org.floatingskies.quiet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa um contato que ESTÁ autorizado a ligar (whitelist).
 */
@Entity(tableName = "whitelist")
data class WhitelistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val telefone: String,         // formato normalizado: só dígitos, com DDI 55
    val telefoneOriginal: String, // formato exibido (ex: (11) 99999-9999)
    val dataCriacao: Long = System.currentTimeMillis()
)
