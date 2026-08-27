package com.hmp.data.network

import com.hmp.data.network.dto.OpenAiFunctionSpec
import com.hmp.data.network.dto.OpenAiMessage
import com.hmp.data.network.dto.OpenAiStyleRequest
import com.hmp.data.network.dto.OpenAiTool
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * 协议 DTO 序列化形状测试（任务书 M2-T1 + M2-T3）：
 * tools/tool_choice 的形状与省略规则、temperature 修正档位。
 */
class OpenAiRequestDtoTest {

    private val json = Json

    @Test
    fun requestWithTools_serializesFunctionShape() {
        val encoded = json.encodeToString(
            OpenAiStyleRequest(
                model = "m",
                messages = listOf(OpenAiMessage(role = "user", content = "hi")),
                temperature = 0.3f,
                tools = listOf(
                    OpenAiTool(
                        function = OpenAiFunctionSpec(
                            name = "searchLibrary",
                            description = "搜索曲库",
                            parameters = JsonObject(mapOf("type" to JsonPrimitive("object"))),
                        )
                    )
                ),
                toolChoice = JsonPrimitive("auto"),
            )
        )

        assertTrue(encoded.contains("\"tools\""), "应序列化 tools")
        assertTrue(encoded.contains("\"type\":\"function\""), "工具类型应为 function")
        assertTrue(encoded.contains("\"name\":\"searchLibrary\""))
        assertTrue(encoded.contains("\"description\":\"搜索曲库\""))
        assertTrue(encoded.contains("\"parameters\":{\"type\":\"object\"}"))
        assertTrue(encoded.contains("\"tool_choice\":\"auto\""))
        assertTrue(encoded.contains("\"temperature\":0.3"), "JSON 任务默认档 0.3（M2-T3 修正）")
    }

    @Test
    fun requestWithoutTools_omitsToolsAndToolChoice() {
        val encoded = json.encodeToString(
            OpenAiStyleRequest(model = "m", messages = listOf(OpenAiMessage("user", "hi")))
        )

        assertFalse(encoded.contains("\"tools\""))
        assertFalse(encoded.contains("tool_choice"))
    }

    @Test
    fun toolResultMessage_carriesToolCallId() {
        val encoded = json.encodeToString(
            OpenAiStyleRequest(
                model = "m",
                messages = listOf(
                    OpenAiMessage(role = "tool", content = "{\"ok\":true}", toolCallId = "call_1"),
                )
            )
        )

        assertTrue(encoded.contains("\"tool_call_id\":\"call_1\""))
    }

    @Test
    fun temperatureDefaultInDto_isLegacySafe() {
        // DTO 默认 0.7 保持既有调用兼容；显式档位由调用方传入（M2-T3）
        val encoded = json.encodeToString(
            OpenAiStyleRequest(model = "m", messages = listOf(OpenAiMessage("user", "hi")))
        )
        assertTrue(encoded.contains("\"temperature\":0.7"))
    }

    @Test
    fun nonStreamingRequest_omitsStreamField() {
        // callChatApi（非流式，富化管道）不传 stream——端点返回普通 JSON
        val encoded = json.encodeToString(
            OpenAiStyleRequest(model = "m", messages = listOf(OpenAiMessage("user", "hi")))
        )
        assertFalse(encoded.contains("\"stream\""))
    }

    @Test
    fun streamingRequest_carriesStreamTrue() {
        val encoded = json.encodeToString(
            OpenAiStyleRequest(model = "m", messages = listOf(OpenAiMessage("user", "hi")), stream = true)
        )
        assertTrue(encoded.contains("\"stream\":true"))
    }

    @Test
    fun temperature_roundTripsFloats() {
        val encoded = json.encodeToString(
            OpenAiStyleRequest(model = "m", messages = listOf(OpenAiMessage("user", "hi")), temperature = 0.25f)
        )
        val decoded = json.decodeFromString<OpenAiStyleRequest>(encoded)
        assertEquals(0.25f, decoded.temperature)
    }
}