package com.hmp.domain.agent.tool

import com.hmp.domain.agent.port.LlmToolSpec
import kotlinx.serialization.json.JsonObject

/**
 * 工具不存在。 */
class ToolNotFoundException(name: String) : Exception("未知工具：$name")

/**
 * 工具注册表（S 阶段——5 能力域 17 原子工具）。
 *
 * 按名路由到具体工具，暴露全部工具的 [LlmToolSpec] 供 M4 下发 function-calling。
 *
 * 回填语义：
 * - **强制回填**：工具返回的 [ToolResult.summary] 在成功时被强制非空——补充空结果也返回
 *   「未命中/无数据」的摘要，防止模型幻觉型假成功（不把「空」当「找到了」）；
 * - **失败入审计**：[ToolResult.failureReason] 在失败时携带，M4 策略层据此写 audit_log；
 *   Registry 自身不落审计（职责在 M4），此处仅保证契约成立（失败必有 failureReason）。
 */
class ToolRegistry(
    tools: List<AgentTool>,
) {
    private val byName: Map<String, AgentTool> = tools.associateBy { it.name }

    init {
        require(tools.map { it.name }.distinct().size == tools.size) { "工具名不能重复" }
        // 理论完整清单见 ToolNames.ALL；Registry 注册集以当前实现为准（预留常量可暂不注册，等实现后追加）
    }

    fun all(): List<AgentTool> = byName.values.sortedBy { it.name }

    fun find(name: String): AgentTool? = byName[name]

    /** 全部工具的 function-calling 声明（M4 调用 [LlmTransport.streamChat] 的 tools 参数）。 */
    val allLlmSpecs: List<LlmToolSpec>
        get() = all().map { it.llmSpec }

    /** 按名执行并校验参数；未知工具抛 [ToolNotFoundException]，参数非法转 [ToolResult.failure](不中断，M4 留审计)。 */
    suspend fun executeTool(name: String, arguments: JsonObject): ToolResult {
        val tool = byName[name] ?: throw ToolNotFoundException(name)
        return try {
            tool.execute(arguments)
        } catch (e: ToolParamError) {
            // 参数越界/缺失 → 工具层失败并携带明确原因（供审计）
            ToolResult.failure(e.message ?: "参数校验失败")
        }
    }

    companion object {
        /**
         * 构造完整工具集（S 阶段 5 域 19 原子）。
         *
         * 域前缀统一：
         * - playback_*：播放控制（playback_state / playback_control / playback_play_at / playback_enqueue）
         * - playlist_*：歌单管理（list/detail/create/rename/delete + add/remove/reorder）
         * - library_*：曲库检索（search / similar / stats / recent_history）
         * - song_*：标签富化（tags_get / enrich_llm）
         * - agent_budget：会话配额快照
         */
        fun create(deps: ToolDependencies): ToolRegistry = ToolRegistry(
            listOf(
                // ── Playback ──
                GetNowPlayingContextTool(deps),   // playback_state
                PlaybackControlTool(deps),        // playback_control
                PlaybackPlayAtTool(deps),         // playback_play_at

                // ── Playlist 实体 CRUD ──
                PlaylistListTool(deps),           // playlist_list
                PlaylistDetailTool(deps),         // playlist_detail
                PlaylistCreateTool(deps),         // playlist_create
                PlaylistRenameTool(deps),         // playlist_rename
                PlaylistDeleteTool(deps),         // playlist_delete

                // ── Playlist 曲目管理 ──
                PlaylistAddSongTool(deps),        // playlist_add_song
                PlaylistRemoveSongTool(deps),     // playlist_remove_song
                PlaylistReorderTool(deps),        // playlist_reorder

                // ── Library ──
                SearchLibraryTool(deps),          // library_search
                GetSimilarSongsTool(deps),        // library_similar
                GetListenStatsTool(deps),         // library_stats
                GetRecentHistoryTool(deps),       // library_recent_history

                // ── Song 富化 ──
                SongTagsGetTool(deps),            // song_tags_get
                EnrichSongTool(deps),             // song_enrich_llm
                // ── Batch B ──
                PlaybackEnqueueTool(deps),       // playback_enqueue
                LibraryTagsTool(deps),           // library_tags
                LibrarySongsByTagTool(deps),     // library_songs_by_tag
                LibrarySongsByArtistTool(deps),  // library_songs_by_artist
                LibrarySongsByAlbumTool(deps),   // library_songs_by_album
                LibraryArtistsTool(deps),        // library_artists
                LibraryAlbumsTool(deps),         // library_albums
                AgentBudgetTool(deps),           // agent_budget
                SongTagUserAddTool(deps),        // song_tag_user_add
                SongTagUserRemoveTool(deps),      // song_tag_user_remove
            )
        )
    }
}
