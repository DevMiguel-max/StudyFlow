package com.studyflow.app.domain.ai

import java.io.IOException

sealed class AIError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ModelNotFound(
        message: String = "Modelo de IA não encontrado ou indisponível.",
        cause: Throwable? = null
    ) : AIError(message, cause)

    class PermissionDenied(
        message: String = "Acesso não autorizado ou App Check não habilitado.",
        cause: Throwable? = null
    ) : AIError(message, cause)

    class Network(
        message: String = "Sem conexão com a IA. Verifique sua internet.",
        cause: Throwable? = null
    ) : AIError(message, cause)

    class Unknown(
        message: String = "Erro inesperado ao processar requisição com a IA.",
        cause: Throwable? = null
    ) : AIError(message, cause)
}

fun Throwable.toAIError(): AIError {
    if (this is AIError) return this
    val msg = message.orEmpty()
    return when {
        msg.contains("404") || msg.contains("NOT_FOUND", ignoreCase = true) || msg.contains("not found", ignoreCase = true) ->
            AIError.ModelNotFound("Modelo de IA indisponível ou inexistente.", this)
        msg.contains("403") || msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("permission denied", ignoreCase = true) || msg.contains("App Check", ignoreCase = true) || msg.contains("blocked", ignoreCase = true) ->
            AIError.PermissionDenied("Permissão negada ou falha na verificação de integridade (App Check).", this)
        this is IOException || msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) || msg.contains("connection", ignoreCase = true) ->
            AIError.Network("Falha de rede ao contatar o serviço de IA.", this)
        else ->
            AIError.Unknown("Falha na geração de conteúdo: ${message ?: javaClass.simpleName}", this)
    }
}
