package org.floatingskies.quiet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro de uma chamada que foi bloqueada (log).
 */
@Entity(tableName = "chamadas_bloqueadas")
data class BlockedCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val telefone: String,
    val telefoneOriginal: String,
    val dataHora: Long,
    val vezes: Int = 1
)
