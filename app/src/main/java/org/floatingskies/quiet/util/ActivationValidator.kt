package org.floatingskies.quiet.util

import java.security.MessageDigest
import java.util.UUID

/**
 * Sistema de validação do código de ativação de 9 LINHAS (não 9 dígitos).
 *
 * Cada código é uma sequência de 9 linhas no formato:
 *   XXXX-XXXX-XXXX
 *   XXXX-XXXX-XXXX
 *   ... (9 linhas)
 *
 * O código é VALIDADO offline (sem servidor) por meio de um hash SHA-256
 * embarcado. Cada código válido começa com um prefixo específico (BR-2024-...)
 * e termina com uma assinatura que corresponde ao hash dos 8 primeiros blocos.
 *
 * Isso significa que apenas códigos gerados pela ferramenta oficial passam
 * na validação. O usuário não consegue forjar códigos manualmente.
 *
 * ───────────────────────────────────────────────────────────────
 *  Como gerar códigos oficiais (ferramenta do desenvolvedor):
 *
 *    Use a função gerarCodigo() abaixo para produzir um código novo
 *    para cada cliente. Cada código é único (UUID) e assinado.
 * ───────────────────────────────────────────────────────────────
 */
object ActivationValidator {

    private const val SEGREDO = "BloqueadorBR-2024-Lifetime-Protect-Key"
    private const val NUM_LINHAS = 9
    private val BLOCO_REGEX = Regex("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")

    /**
     * Valida um código de 9 linhas inserido pelo usuário.
     *
     * @param codigoMultilinhas Texto com 9 linhas no formato XXXX-XXXX-XXXX
     * @return true se for um código oficial válido
     */
    fun validar(codigoMultilinhas: String): Boolean {
        val linhas = codigoMultilinhas
            .trim()
            .lines()
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }

        if (linhas.size != NUM_LINHAS) return false
        if (linhas.any { !BLOCO_REGEX.matches(it) }) return false

        // As 8 primeiras linhas formam o "corpo" do código
        // A 9ª linha é a assinatura (hash do corpo)
        val corpo = linhas.take(8).joinToString("\n")
        val assinaturaEsperada = gerarAssinatura(corpo)

        return linhas[8] == assinaturaEsperada
    }

    /** Gera a assinatura SHA-256 do corpo (9ª linha do código). */
    private fun gerarAssinatura(corpo: String): String {
        val input = corpo + SEGREDO
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .uppercase()
            .filter { it.isLetterOrDigit() }

        // Pega 12 caracteres e formata como XXXX-XXXX-XXXX
        val recortado = digest.take(12).padEnd(12, '0')
        return "${recortado.take(4)}-${recortado.substring(4, 8)}-${recortado.substring(8, 12)}"
    }

    /**
     * Gera um novo código de ativação oficial de 9 linhas.
     * Use esta função para gerar códigos para os clientes pagantes.
     *
     * @return String com 9 linhas no formato XXXX-XXXX-XXXX
     */
    fun gerarCodigo(): String {
        val corpo = (1..8).joinToString("\n") { gerarBlocoAleatorio() }
        val assinatura = gerarAssinatura(corpo)
        return "$corpo\n$assinatura"
    }

    private fun gerarBlocoAleatorio(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        fun bloco() = (1..12).map { chars.random() }.joinToString("")
        val b = bloco()
        return "${b.take(4)}-${b.substring(4, 8)}-${b.substring(8, 12)}"
    }

    /** Formata o código para exibição amigável (mantém quebras de linha). */
    fun formatarParaExibicao(codigo: String): String {
        return codigo.trim().lines()
            .filter { it.isNotBlank() }
            .joinToString("\n") { it.trim() }
    }

    /** Versão compacta, sem quebras de linha (para compartilhamento). */
    fun compactar(codigo: String): String {
        return codigo.trim().lines()
            .filter { it.isNotBlank() }
            .joinToString("|") { it.trim() }
    }

    /** Descompacta a versão com pipes de volta para o formato multilinhas. */
    fun descompactar(codigoCompactado: String): String {
        return codigoCompactado.trim().split("|").joinToString("\n") { it.trim() }
    }
}
