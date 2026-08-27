package com.hmp.data.network

import com.hmp.data.network.dto.OpenAiFunctionSpec
import com.hmp.data.network.dto.OpenAiMessage
import com.hmp.data.network.dto.OpenAiStyleRequest
import com.hmp.data.network.dto.OpenAiTool
import com.hmp.domain.agent.port.LlmEvent
import com.hmp.domain.agent.port.LlmMessage
import com.hmp.domain.agent.port.LlmToolSpec
import com.hmp.domain.agent.port.LlmTransport
import com.hmp.domain.setting.model.AiEndpointConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/** 流式调用失败（HTTP 非 2xx 等），由 [OpenAiLlmTransport] 转 Failed 事件。 */
class LlmStreamException(message: String) : Exception(message)

/**
 * OpenAI 兼容协议的 [LlmTransport] 实现（设计总纲 6.1 端口平面 IV 层）。
 *
 * 职责：领域消息/工具 → OpenAI 报文（tools + tool_choice=auto），SSE 流 → [LlmEvent]。
 * 工具调用按 index 分片组装（arguments 分片跨 chunk 到达），`finish_reason=tool_calls`
 * 或流结束兜底时一次性发出完整 [LlmEvent.ToolCall]。
 */
class OpenAiLlmTransport(
    private val adapter: OpenAiCompatibleAdapter,
    private val json: Json,
) : LlmTransport {

    override suspend fun streamChat(
        config: AiEndpointConfig,
        messages: List<LlmMessage>,
        tools: List<LlmToolSpec>?,
        temperature: Float,
    ): Flow<LlmEvent> = flow {
        val request = OpenAiStyleRequest(
            model = config.selectedModel,
            messages = messages.map { OpenAiMessage(role = it.role, content = it.content, toolCallId = it.toolCallId) },
            temperature = temperature,
            tools = tools?.map { spec ->
                OpenAiTool(function = OpenAiFunctionSpec(name = spec.name, description = spec.description, parameters = spec.parameters))
            },
            toolChoice = if (tools.isNullOrEmpty()) null else JsonPrimitive("auto"),
            stream = true,
        )

        val toolAccumulators = mutableMapOf<Int, ToolCallAccumulator>()
        try {
            adapter.streamChatCompletion(config, request).collect { chunk ->
                val choice = chunk.choices?.firstOrNull() ?: return@collect
                val delta = choice.delta ?: return@collect

                delta.content?.takeIf { it.isNotEmpty() }?.let { emit(LlmEvent.TextDelta(it)) }

                delta.toolCalls?.forEach { tc ->
                    val acc = toolAccumulators.getOrPut(tc.index ?: 0) { ToolCallAccumulator() }
                    tc.id?.let { acc.id = it }
                    tc.function?.name?.let { acc.name = it }
                    tc.function?.arguments?.let { acc.arguments.append(it) }
                }

                if (choice.finishReason == "tool_calls") {
                    flushToolCalls(toolAccumulators, this)
                    toolAccumulators.clear()
                }
            }
            // 兜底：部分端点不发送 finish_reason=tool_calls，流结束时补发
            flushToolCalls(toolAccumulators, this)
            emit(LlmEvent.Completed)
        } catch (e: Exception) {
            emit(LlmEvent.Failed(e.message ?: "LLM stream failed"))
        }
    }

    private suspend fun flushToolCalls(
        accumulators: Map<Int, ToolCallAccumulator>,
        collector: FlowCollector<LlmEvent>,
    ) {
        accumulators.values.forEach { acc ->
            val name = acc.name
            if (!name.isNullOrBlank()) {
                collector.emit(
                    LlmEvent.ToolCall(
                        id = acc.id ?: "",
                        name = name,
                        argumentsJson = acc.arguments.toString(),
                    )
                )
            }
        }
    }

    private class ToolCallAccumulator {
        var id: String? = null
        var name: String? = null
        val arguments = StringBuilder()
    }
}