package com.hearablemusic.player.ui.chat

import com.hmp.domain.agent.engine.AgentOrchestrator
import com.hmp.domain.agent.engine.ConfirmGate
import com.hmp.domain.agent.engine.ConfirmRequest
import com.hmp.domain.agent.engine.ContextBudget
import com.hmp.domain.agent.engine.PolicyGuard
import com.hmp.domain.agent.engine.PresenceBus
import com.hmp.domain.agent.engine.RunContextInput
import com.hmp.domain.agent.engine.SessionStore
import com.hmp.domain.agent.engine.TerminationReason
import com.hmp.domain.agent.engine.TrustLedger
import com.hmp.domain.agent.port.AuditLogPort
import com.hmp.domain.agent.port.LlmTransport
import com.hmp.domain.agent.tool.ToolDependencies
import com.hmp.domain.agent.tool.ToolRegistry
import com.hmp.domain.setting.model.AiEndpointConfig
import com.hearablemusic.player.ui.platform.currentTimeMillis
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * M5-T1 对话页 UI 面向引擎的接缝事件（真实网关经 AgentOrchestrator 产出；单测注入 Fake 网关）。
 */
sealed interface ChatAgentEvent {
    /** 引擎请求确认：携带本轮全部待确认项，UI 据此渲染确认卡并令 [ConfirmBridge.submit] 恢复引擎。 */
    data class NeedConfirm(val turnId: String, val requests: List<ConfirmRequest>) : ChatAgentEvent
    /** 单条工具执行回执（写审计成功后送达）。 */
    data class ToolExecuted(val record: com.hmp.domain.agent.engine.ToolExecutionRecord) : ChatAgentEvent
    /** 单轮结束：最终答复文本 + 全量执行记录 + 终止原因。 */
    data class Finished(
        val text: String,
        val toolCalls: List<com.hmp.domain.agent.engine.ToolExecutionRecord>,
        val terminatedBy: TerminationReason,
    ) : ChatAgentEvent

    data class Failed(val message: String) : ChatAgentEvent
}

/**
 * 确认桥：引擎侧 suspend ConfirmGate 与 UI 之间的握手。
 *
 * 引擎在 [ConfirmGate.request] 中挂起等待用户勾选；本桥经 [onRequest] 向 UI 抛
 * [ChatAgentEvent.NeedConfirm]，UI 勾选后调 [submit] 用批准列表恢复引擎。
 * [CompletableDeferred] 跨协程线程安全，客服 ContinuousFlow/VM 均可安全调用。
 */
class ConfirmBridge {
    private val pending = LinkedHashMap<String, CompletableDeferred<List<Boolean>>>()
    private var counter = 0
    /** 向 UI 请求确认的回调（由网关在 flow 生产者上下文中注入，可 emit）。 */
    var onRequest: (suspend (String, List<ConfirmRequest>) -> Unit)? = null

    /** 引擎侧：登记一批待确认项并挂起，直到 [submit] 提供同序批准列表。 */
    suspend fun awaitApprovals(requests: List<ConfirmRequest>): List<Boolean> {
        val turnId = "confirm_${counter++}"
        val deferred = CompletableDeferred<List<Boolean>>()
        pending[turnId] = deferred
        onRequest?.invoke(turnId, requests)
        return deferred.await()
    }

    /**
     * UI 侧：提交批准列表，恢复对应 [turnId] 的挂起。
     * @return true 该批次仍待确认（成功投递）；false 该批次已关闭（竞态/已取消）。
     */
    fun submit(turnId: String, approvals: List<Boolean>): Boolean =
        pending.remove(turnId)?.complete(approvals) ?: false
}

/** 对话引擎接缝（M5-T1）：把一次用户输入换算为面向 UI 的事件流。 */
interface ChatAgentGateway {
    /**
     * 启动一轮对话。
     * @param input 用户输入
     * @param config 生效 AI 端点配置（FREE 内置 / CUSTOM）
     * @param bridge 确认桥（本轮引擎 — UI 握手）；NeedConfirm 事件出现时 UI 用其 [ConfirmBridge.submit] 恢复者
     * @return 事件流；消费协程取消即取消引擎（步数预算熔断以 cancel 实现）
     */
    fun run(
        input: String,
        config: AiEndpointConfig,
        bridge: ConfirmBridge,
        ctx: RunContextInput = RunContextInput(),
    ): Flow<ChatAgentEvent>
}

/**
 * 真实网关：每轮装配 [AgentOrchestrator]（注入的传输/工具依赖/审计），桥接确认门到 [ConfirmBridge]。
 *
 * 与 M4 测试的约定一致：信任账本/护栏/预算/会话/存在感在该轮内一次性创建（M5 对话页首航线，
 * 跨会话持久化的信任阶梯/云端额度留 M6 存在感接线时上浮）。
 */
class OrchestratorChatGateway(
    private val transport: LlmTransport,
    private val toolDeps: ToolDependencies,
    private val auditLog: AuditLogPort,
    private val dailyCloudQuota: Int,
) : ChatAgentGateway {

    override fun run(
        input: String,
        config: AiEndpointConfig,
        bridge: ConfirmBridge,
        ctx: RunContextInput,
    ): Flow<ChatAgentEvent> = flow {
        val registry = ToolRegistry.create(toolDeps)
        val ledger = TrustLedger()
        val policy = PolicyGuard(ledger, auditLog)
        val budget = ContextBudget({ timeMillis() }, dailyCloudQuota = dailyCloudQuota)
        val session = SessionStore({ timeMillis() })
        val presence = PresenceBus()
        bridge.onRequest = { turnId, requests -> emit(ChatAgentEvent.NeedConfirm(turnId, requests)) }
        val confirmGate = ConfirmGate { requests -> bridge.awaitApprovals(requests) }
        val orchestrator = AgentOrchestrator(
            transport = transport,
            registry = registry,
            policyGuard = policy,
            contextBudget = budget,
            sessionStore = session,
            presenceBus = presence,
            auditLog = auditLog,
            confirmGate = confirmGate,
        )
        try {
            val result = orchestrator.run(input, config, ctx)
            result.toolCalls.forEach { emit(ChatAgentEvent.ToolExecuted(it)) }
            emit(ChatAgentEvent.Finished(result.text, result.toolCalls, result.terminatedBy))
        } catch (ce: CancellationException) {
            throw ce // 预算熔断 / 用户取消经 cancel 实现，避免误判失败
        } catch (e: Exception) {
            emit(ChatAgentEvent.Failed(e.message ?: "对话失败"))
        }
    }

    private fun timeMillis(): Long = currentTimeMillis()
}