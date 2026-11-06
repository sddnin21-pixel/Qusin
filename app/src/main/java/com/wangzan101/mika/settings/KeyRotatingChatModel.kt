package com.wangzan101.mika.settings

import com.wangzan101.mika.core.model.Capabilities
import com.wangzan101.mika.core.model.ChatModel
import com.wangzan101.mika.core.model.Message
import com.wangzan101.mika.core.model.ModelError
import com.wangzan101.mika.core.model.ModelReply
import com.wangzan101.mika.core.model.StreamingChatModel
import com.wangzan101.mika.core.model.ToolSpec

/**
 * Thử lần lượt các API key của cùng một nhà cung cấp khi key hiện tại bị từ chối hoặc hết quota.
 * Lỗi mạng, lỗi server, lỗi model và lỗi nội dung không làm đổi key.
 */
internal class KeyRotatingChatModel(
    private val models: List<ChatModel>,
    private val activeStartIndex: () -> Int,
    private val onKeyRejected: (failedIndex: Int) -> Unit,
    private val onAllKeysRejected: (nextStartIndex: Int) -> Unit,
) : StreamingChatModel {
    init {
        require(models.isNotEmpty()) { "at least one keyed model is required" }
    }

    override val capabilities: Capabilities get() = models.first().capabilities
    override val name: String get() = models.first().name

    override suspend fun complete(system: String, messages: List<Message>, tools: List<ToolSpec>): ModelReply {
        var lastError: ModelError? = null
        val startIndex = activeStartIndex().coerceIn(models.indices)
        for (offset in models.indices) {
            val index = (startIndex + offset) % models.size
            try {
                return models[index].complete(system, messages, tools)
            } catch (error: ModelError) {
                if (!error.isKeyExhaustion() || offset == models.lastIndex) {
                    if (error.isKeyExhaustion()) onAllKeysRejected((index + 1) % models.size)
                    throw error
                }
                lastError = error
                onKeyRejected(index)
            }
        }
        throw lastError ?: ModelError.Auth()
    }

    override suspend fun completeStreaming(
        system: String,
        messages: List<Message>,
        tools: List<ToolSpec>,
        onText: (String) -> Unit,
    ): ModelReply {
        var lastError: ModelError? = null
        val startIndex = activeStartIndex().coerceIn(models.indices)
        for (offset in models.indices) {
            val index = (startIndex + offset) % models.size
            val delegate = models[index]
            var emittedText = false
            try {
                return if (delegate is StreamingChatModel) {
                    delegate.completeStreaming(system, messages, tools) { text ->
                        emittedText = true
                        onText(text)
                    }
                } else {
                    delegate.complete(system, messages, tools)
                }
            } catch (error: ModelError) {
                // Không thử lại sau khi đã phát chữ ra UI, tránh câu trả lời bị lặp khi stream bị ngắt giữa chừng.
                if (emittedText || !error.isKeyExhaustion() || offset == models.lastIndex) {
                    if (!emittedText && error.isKeyExhaustion()) onAllKeysRejected((index + 1) % models.size)
                    throw error
                }
                lastError = error
                onKeyRejected(index)
            }
        }
        throw lastError ?: ModelError.Auth()
    }

    private fun ModelError.isKeyExhaustion(): Boolean =
        this is ModelError.Auth || this is ModelError.QuotaExceeded || this is ModelError.RateLimited
}
