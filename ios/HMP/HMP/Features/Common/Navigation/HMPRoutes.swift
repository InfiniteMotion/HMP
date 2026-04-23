import Foundation

/// HMP 路由枚举 - 对应 Android Routes.kt
/// 使用 Hashable 枚举实现类型安全的路由，替代 Navigation3 的 NavKey
enum HMPRoute: Hashable {
    // MARK: - 主标签页
    case tabs(TabItem)
    case home
    case gallery
    case list
    case user

    // MARK: - 播放器
    case player
    case lyrics
    case audioEffects

    // MARK: - 音乐库
    case search
    case songDetail(musicId: Int64)
    case artist(name: String)
    case album(name: String)
    case custom

    // MARK: - 播放列表
    case playlist(name: String)
    case customPlaylist(playlistId: Int64)
    case userPlaylistManage

    // MARK: - 设置
    case setting
    case profileSettings
    case backupSettings
    case librarySettings

    // MARK: - AI / 用户数据
    case ai
    case userUsageData
}

/// Tab 页枚举
enum TabItem: String, CaseIterable, Hashable {
    case library = "music.note.list"
    case player = "play.circle"
    case playlist = "list.bullet"
    case settings = "gearshape"

    var title: String {
        switch self {
        case .library: return "音乐库"
        case .player: return "播放"
        case .playlist: return "列表"
        case .settings: return "设置"
        }
    }
}
