package com.hmp.data.network

import com.hmp.data.network.dto.OpenAiFunctionDelta
import com.hmp.data.network.dto.OpenAiStreamChunk
import com.hmp.data.network.dto.OpenAiStreamChoice
import com.hmp.data.network.dto.OpenAiStreamDelta
import com.hmp.data.network.dto.OpenAiToolCallDelta
import com.hmp.domain.agent.port.LlmEvent
import com.hmp.domain.agent.port.LlmMessage
import com.hmp.domain.agent.port.LlmToolSpec
import com.hmp.domain.setting.model.AiEndpointConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * LlmTransport 流式端到端测试（任务书 M2-T2，MockEngine 模拟端点）。
 * 覆盖：文本 delta 流、工具调用分片组装、请求形状（tools/tool_choice/temperature）、
 * 无工具端点退化纯文本、HTTP 错误转 Failed、畸形 chunk 跳过。
 */
class OpenAiLlmTransportTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val config = AiEndpointConfig(
        endpoint = "https://example.com/v1",
        apiKey = "test-key",
        selectedModel = "test-model",
        isConfigured = true,
    )

    private fun sse(vararg payloads: String): String =
        payloads.joinToString("\n\n") { "data: $it" } + "\n\n"

    private fun sse(chunks: List<OpenAiStreamChunk>): String = sse(*chunks.map {
        json.encodeToString(OpenAiStreamChunk.serializer(), it)
    }.toTypedArray())

    private fun transport(
        captureBody: (String?) -> Unit = {},
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "",
    ): Pair<OpenAiLlmTransport, HttpClient> {
        val engine = MockEngine { request ->
            captureBody(
                when (val b = request.body) {
                    is TextContent -> b.text
                    is ByteArrayContent -> b.bytes().decodeToString()
                    else -> null.also { println("[TEST] 未识别的请求体类型: ${b::class.simpleName}") }
                }
            )
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "text/event-stream"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(this@OpenAiLlmTransportTest.json) }
        }
        return OpenAiLlmTransport(OpenAiCompatibleAdapter(client, json), json) to client
    }

    @Test
    fun plainTextStream_emitsDeltasThenCompleted() = runTest {
        val (llm, client) = transport(body = sse(
            listOf(
                OpenAiStreamChunk(choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "你好")))),
                OpenAiStreamChunk(choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "，伙伴"), finishReason = "stop"))),
            )
        ))
        val events = llm.streamChat(config, listOf(LlmMessage("user", "hi"))).toList()
        client.close()

        assertEquals(
            listOf(LlmEvent.TextDelta("你好"), LlmEvent.TextDelta("，伙伴"), LlmEvent.Completed),
            events,
        )
    }

    @Test
    fun fragmentedToolArguments_areAssembledIntoSingleToolCall() = runTest {
        val body = sse(
            listOf(
                OpenAiStreamChunk(
                    choices = listOf(
                        OpenAiStreamChoice(
                            delta = OpenAiStreamDelta(
                                toolCalls = listOf(
                                    OpenAiToolCallDelta(
                                        index = 0, id = "call_7", type = "function",
                                        function = OpenAiFunctionDelta(name = "searchLibrary", arguments = "{\"query\":\"摇"),
                                    )
                                )
                            )
                        )
                    )
                ),
                OpenAiStreamChunk(
                    choices = listOf(
                        OpenAiStreamChoice(
                            delta = OpenAiStreamDelta(
                                toolCalls = listOf(
                                    OpenAiToolCallDelta(index = 0, function = OpenAiFunctionDelta(arguments = "滚\"}")),
                                )
                            ),
                            finishReason = "tool_calls",
                        )
                    )
                ),
            )
        )
        val (llm, client) = transport(body = body)
        val events = llm.streamChat(
            config,
            listOf(LlmMessage("user", "找点摇滚")),
            tools = listOf(LlmToolSpec(name = "searchLibrary", description = "search")),
        ).toList()
        client.close()

        assertEquals(
            listOf(
                LlmEvent.ToolCall(id = "call_7", name = "searchLibrary", argumentsJson = "{\"query\":\"摇滚\"}"),
                LlmEvent.Completed,
            ),
            events,
        )
    }

    @Test
    fun requestCarriesToolsToolChoiceAndTemperature() = runTest {
        var captured: String? = null
        val (llm, client) = transport(captureBody = { captured = it })
        llm.streamChat(
            config,
            listOf(LlmMessage("user", "hi")),
            tools = listOf(
                LlmToolSpec(
                    name = "searchLibrary",
                    description = "搜索曲库",
                    parameters = buildJsonObject("type" to "object"),
                )
            ),
            temperature = 0.3f,
        ).toList()
        client.close()

        val body = json.parseToJsonElement(captured!!).jsonObject
        assertEquals("test-model", body["model"]!!.jsonPrimitive.content)
        assertEquals(0.3f, body["temperature"]!!.jsonPrimitive.content.toFloat())
        assertEquals("auto", body["tool_choice"]!!.jsonPrimitive.content)
        assertEquals(true, body["stream"]!!.jsonPrimitive.content.toBoolean(), "流式请求必须带 stream=true（审查修复）")
        val tools = body["tools"] as JsonArray
        val tool = (tools[0] as JsonObject)
        assertEquals("function", tool["type"]!!.jsonPrimitive.content)
        val fn = tool["function"]!!.jsonObject
        assertEquals("searchLibrary", fn["name"]!!.jsonPrimitive.content)
        assertEquals("搜索曲库", fn["description"]!!.jsonPrimitive.content)
        assertTrue(fn.containsKey("parameters"))
    }

    @Test
    fun requestWithoutTools_omitsToolsAndToolChoice() = runTest {
        var captured: String? = null
        val (llm, client) = transport(captureBody = { captured = it })
        llm.streamChat(config, listOf(LlmMessage("user", "hi"))).toList()
        client.close()

        assertFalse(captured!!.contains("\"tools\""))
        assertFalse(captured!!.contains("\"tool_choice\""))
        assertTrue(captured!!.contains("\"stream\":true"), "stream 与 tools 无关：流式请求始终带 stream=true")
    }

    @Test
    fun temperatureIsParameterizedPerTask() = runTest {
        var captured: String? = null
        val (llm, client) = transport(captureBody = { captured = it })
        llm.streamChat(config, listOf(LlmMessage("user", "hi")), temperature = 0.7f).toList()
        client.close()

        val body = json.parseToJsonElement(captured!!).jsonObject
        assertEquals(0.7f, body["temperature"]!!.jsonPrimitive.content.toFloat())
    }

    @Test
    fun toolsRequestedButEndpointIgnores_fallsBackToPlainText() = runTest {
        // 模拟无 tools 支持的服务商：忽略 tools 字段，直接回文本流
        val (llm, client) = transport(
            body = sse(
                listOf(
                    OpenAiStreamChunk(choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "结果如下")))),
                )
            )
        )
        val events = llm.streamChat(
            config,
            listOf(LlmMessage("user", "hi")),
            tools = listOf(LlmToolSpec(name = "searchLibrary")),
        ).toList()
        client.close()

        assertEquals(listOf(LlmEvent.TextDelta("结果如下"), LlmEvent.Completed), events)
    }

    @Test
    fun httpError_emitsFailedEvent() = runTest {
        val (llm, client) = transport(status = HttpStatusCode.Unauthorized)
        val events = llm.streamChat(config, listOf(LlmMessage("user", "hi"))).toList()
        client.close()

        assertEquals(1, events.size)
        assertTrue(events.single() is LlmEvent.Failed)
    }

    @Test
    fun malformedChunk_isSkipped_streamContinues() = runTest {
        val body = "data: {\"choices\":[{\"delta\":{\"content\":\"第一段\"}}]}\n\n" +
            "data: 这不是合法JSON\n\n" +
            "data: {\"choices\":[{\"delta\":{\"content\":\"第二段\"},\"finish_reason\":\"stop\"}]}\n\n" +
            "data: [DONE]\n\n"
        val (llm, client) = transport(body = body)
        val events = llm.streamChat(config, listOf(LlmMessage("user", "hi"))).toList()
        client.close()

        assertEquals(listOf(LlmEvent.TextDelta("第一段"), LlmEvent.TextDelta("第二段"), LlmEvent.Completed), events)
    }

    @Test
    fun toolCallWithoutFinishReason_isFlushedAtStreamEnd() = runTest {
        // 部分端点不发送 finish_reason=tool_calls：依赖流结束兜底 flush
        val (llm, client) = transport(
            body = sse(
                listOf(
                    OpenAiStreamChunk(
                        choices = listOf(
                            OpenAiStreamChoice(
                                delta = OpenAiStreamDelta(
                                    toolCalls = listOf(
                                        OpenAiToolCallDelta(
                                            index = 0, id = "call_9",
                                            function = OpenAiFunctionDelta(name = "getNowPlayingContext", arguments = "{}"),
                                        )
                                    )
                                )
                            )
                        )
                    ),
                )
            )
        )
        val events = llm.streamChat(
            config,
            listOf(LlmMessage("user", "hi")),
            tools = listOf(LlmToolSpec(name = "getNowPlayingContext")),
        ).toList()
        client.close()

        assertEquals(
            listOf(LlmEvent.ToolCall(id = "call_9", name = "getNowPlayingContext", argumentsJson = "{}"), LlmEvent.Completed),
            events,
        )
        assertNull(events.filterIsInstance<LlmEvent.TextDelta>().firstOrNull())
    }

    private fun buildJsonObject(vararg pairs: Pair<String, String>): JsonObject =
        JsonObject(pairs.map { it.first to json.parseToJsonElement("\"${it.second}\"").jsonPrimitive }.toMap())
}