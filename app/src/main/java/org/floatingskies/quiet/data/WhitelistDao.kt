package org.floatingskies.quiet.data

import androidx.room.*

@Dao
interface WhitelistDao {

    @Query("SELECT * FROM whitelist ORDER BY nome ASC")
    suspend fun listarTodos(): List<WhitelistEntity>

    @Query("SELECT * FROM whitelist ORDER BY nome ASC")
    fun listarTodosLive(): List<WhitelistEntity>

    @Query("SELECT * FROM whitelist WHERE telefone = :telefone LIMIT 1")
    suspend fun buscarPorTelefone(telefone: String): WhitelistEntity?

    @Query("SELECT COUNT(*) FROM whitelist")
    suspend fun contar(): Int

    @Insert
    suspend fun inserir(whitelist: WhitelistEntity): Long

    @Update
    suspend fun atualizar(whitelist: WhitelistEntity)

    @Delete
    suspend fun deletar(whitelist: WhitelistEntity)

    @Query("DELETE FROM whitelist")
    suspend fun limparTudo()
}
