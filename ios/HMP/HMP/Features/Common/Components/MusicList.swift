import SwiftUI
import shared

// MARK: - MusicListConfig 配置

/// MusicList 统一配置 - 对应 Android MusicListConfig
struct MusicListConfig {
    let header: HeaderConfig
    let item: ItemConfig
    let currentPlayingIndex: Int?
    let callbacks: MusicListCallbacks
    let indexJump: IndexJumpConfig?

    init(
        header: HeaderConfig = .none,
        item: ItemConfig = ItemConfig.full,
        currentPlayingIndex: Int? = nil,
        callbacks: MusicListCallbacks = MusicListCallbacks(),
        indexJump: IndexJumpConfig? = nil
    ) {
        self.header = header
        self.item = item
        self.currentPlayingIndex = currentPlayingIndex
        self.callbacks = callbacks
        self.indexJump = indexJump
    }

    static var defaultConfig: MusicListConfig {
        MusicListConfig()
    }
    
    static func libraryPreset(
        selectedGenre: String = "全部",
        selectedOrder: String = "ASC",
        onOrderPlay: @escaping () -> Void,
        onShufflePlay: @escaping () -> Void,
        callbacks: MusicListCallbacks
    ) -> MusicListConfig {
        MusicListConfig(
            header: .full(
                selectedGenre: selectedGenre,
                selectedOrder: selectedOrder,
                onOrderPlay: onOrderPlay,
                onShufflePlay: onShufflePlay
            ),
            item: .full,
            currentPlayingIndex: nil,
            callbacks: callbacks
        )
    }
    
    static func playlistPreset(
        onOrderPlay: @escaping () -> Void,
        onShufflePlay: @escaping () -> Void,
        callbacks: MusicListCallbacks
    ) -> MusicListConfig {
        MusicListConfig(
            header: .simple(
                onOrderPlay: onOrderPlay,
                onShufflePlay: onShufflePlay,
                trailing: nil
            ),
            item: ItemConfig.full(showPin: true, showRemove: true, showMenu: true),
            currentPlayingIndex: nil,
            callbacks: callbacks
        )
    }
    
    static func galleryPreset(callbacks: MusicListCallbacks) -> MusicListConfig {
        MusicListConfig(
            header: .none,
            item: .gallery,
            currentPlayingIndex: nil,
            callbacks: callbacks
        )
    }
}

// MARK: - HeaderConfig

enum HeaderConfig {
    case none
    case simple(onOrderPlay: () -> Void, onShufflePlay: () -> Void, trailing: (() -> AnyView)?)
    case full(selectedGenre: String, selectedOrder: String, onOrderPlay: () -> Void, onShufflePlay: () -> Void)
    case custom(content: () -> AnyView)
}

// MARK: - IndexJumpConfig

struct IndexJumpConfig {
    let isLetterMode: Bool
    let isReversed: Bool
    let orderBy: String
}

// MARK: - ItemConfig

enum ItemVariant {
    case full
    case compact
    case gallery
    case custom
}

struct ItemConfig {
    let variant: ItemVariant
    let showIndex: Bool
    let showPin: Bool
    let showMoveButtons: Bool
    let showRemove: Bool
    let showMenu: Bool
    let extraMenuItems: [(String, (MusicInfo_) -> Void)]
    
    static var full: ItemConfig { ItemConfig(variant: .full, showIndex: false, showPin: false, showMoveButtons: false, showRemove: false, showMenu: false, extraMenuItems: []) }
    static var compact: ItemConfig { ItemConfig(variant: .compact, showIndex: false, showPin: false, showMoveButtons: false, showRemove: false, showMenu: false, extraMenuItems: []) }
    static var gallery: ItemConfig { ItemConfig(variant: .gallery, showIndex: true, showPin: false, showMoveButtons: false, showRemove: false, showMenu: true, extraMenuItems: []) }
    
    static func full(showPin: Bool = false, showMoveButtons: Bool = false, showRemove: Bool = false, showMenu: Bool = false, extraMenuItems: [(String, (MusicInfo_) -> Void)] = []) -> ItemConfig {
        ItemConfig(variant: .full, showIndex: true, showPin: showPin, showMoveButtons: showMoveButtons, showRemove: showRemove, showMenu: showMenu, extraMenuItems: extraMenuItems)
    }
    
    static func compact(showPin: Bool = false, showMoveButtons: Bool = false, showRemove: Bool = false, showMenu: Bool = false, extraMenuItems: [(String, (MusicInfo_) -> Void)] = []) -> ItemConfig {
        ItemConfig(variant: .compact, showIndex: false, showPin: showPin, showMoveButtons: showMoveButtons, showRemove: showRemove, showMenu: showMenu, extraMenuItems: extraMenuItems)
    }
    
    static func gallery(showPin: Bool = false, showMoveButtons: Bool = false, showRemove: Bool = false, showMenu: Bool = false, extraMenuItems: [(String, (MusicInfo_) -> Void)] = []) -> ItemConfig {
        ItemConfig(variant: .gallery, showIndex: true, showPin: showPin, showMoveButtons: showMoveButtons, showRemove: showRemove, showMenu: showMenu, extraMenuItems: extraMenuItems)
    }
}

// MARK: - Callbacks

class MusicListCallbacks: ObservableObject {
    var onItemClick: ((MusicInfo_, Int) -> Void)?
    var onMenuClick: ((MusicInfo_) -> Void)?
    var onPinToTop: ((MusicInfo_) -> Void)?
    var onMoveUp: ((Int) -> Void)?
    var onMoveDown: ((Int) -> Void)?
    var onRemove: ((MusicInfo_) -> Void)?
    var onAddToPlaylist: ((MusicInfo_) -> Void)?
    var onSelectionChange: ((Int64, Bool) -> Void)?
    var onEnterEditMode: (() -> Void)?
    var onExitEditMode: (() -> Void)?
    var onBatchDelete: ((Set<Int64>) -> Void)?
    var onBatchAddToPlaylist: ((Set<Int64>) -> Void)?
}

// MARK: - MusicList 组件

/// 完整的 MusicList 组件 - 对应 Android MusicList.kt
struct MusicList: View {
    @Environment(HMPTheme.self) private var theme
    @StateObject private var listState = MusicListState()
    
    let musicInfoList: [MusicInfo_]
    let config: MusicListConfig
    
    init(
        musicInfoList: [MusicInfo_],
        config: MusicListConfig = .defaultConfig
    ) {
        self.musicInfoList = musicInfoList
        self.config = config
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // 编辑模式工具栏
            if listState.isEditMode {
                editToolbar
            } else {
                headerView
            }

            if !musicInfoList.isEmpty {
                Divider().padding(.vertical, 8)
            }

            // 列表内容
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(Array(musicInfoList.enumerated()), id: \.offset) { index, info in
                            MusicListItem(
                                musicInfo: info,
                                index: index,
                                config: config,
                                isCurrentPlaying: config.currentPlayingIndex == index,
                                isEditMode: listState.isEditMode,
                                isSelected: listState.selectedIds.contains(info.music.id),
                                callbacks: config.callbacks
                            )
                            .id(index)
                            .onLongPressGesture {
                                if !listState.isEditMode {
                                    withAnimation {
                                        listState.enterEditMode()
                                        listState.selectedIds.insert(info.music.id)
                                    }
                                    config.callbacks.onEnterEditMode?()
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 8)
                }
                .onReceive(NotificationCenter.default.publisher(for: .indexStripJump)) { notification in
                    if let index = notification.userInfo?["index"] as? Int {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            proxy.scrollTo(index, anchor: .center)
                        }
                    }
                }
            }
        }
    }
    
    // MARK: - Header
    @ViewBuilder
    private var headerView: some View {
        switch config.header {
        case .none:
            EmptyView()
        case .simple(let onOrderPlay, let onShufflePlay, let trailing):
            SimpleHeader(
                count: musicInfoList.count,
                onOrderPlay: onOrderPlay,
                onShufflePlay: onShufflePlay,
                trailing: trailing
            )
        case .full(let selectedGenre, let selectedOrder, let onOrderPlay, let onShufflePlay):
            FullHeader(
                selectedGenre: selectedGenre,
                selectedOrder: selectedOrder,
                count: musicInfoList.count,
                onOrderPlay: onOrderPlay,
                onShufflePlay: onShufflePlay
            )
        case .custom(let content):
            content()
        }
    }
    
    // MARK: - Edit Toolbar
    private var editToolbar: some View {
        HStack(spacing: 12) {
            // 全选/取消
            Button {
                withAnimation {
                    if listState.selectedIds.count == musicInfoList.count {
                        listState.selectedIds.removeAll()
                    } else {
                        listState.selectedIds = Set(musicInfoList.map { $0.music.id })
                    }
                }
            } label: {
                Text(listState.selectedIds.count == musicInfoList.count ? "取消全选" : "全选")
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.primary)
            }
            
            Spacer()
            
            // 批量删除
            if config.callbacks.onBatchDelete != nil {
                Button {
                    config.callbacks.onBatchDelete?(listState.selectedIds)
                    withAnimation { listState.exitEditMode() }
                } label: {
                    Image(systemName: "trash")
                        .font(.system(size: 18))
                        .foregroundColor(.red)
                }
            }
            
            // 批量加入播放列表
            if config.callbacks.onBatchAddToPlaylist != nil {
                Button {
                    config.callbacks.onBatchAddToPlaylist?(listState.selectedIds)
                    withAnimation { listState.exitEditMode() }
                } label: {
                    Image(systemName: "plus.circle")
                        .font(.system(size: 18))
                        .foregroundColor(theme.primary)
                }
            }
            
            // 退出编辑
            Button {
                withAnimation {
                    listState.exitEditMode()
                    config.callbacks.onExitEditMode?()
                }
            } label: {
                Text("完成")
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.primary)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(theme.surface)
    }
}

// MARK: - FixedMusicList (非懒加载版)

/// 非懒加载版 MusicList - 对应 Android FixedMusicList
struct FixedMusicList: View {
    @Environment(HMPTheme.self) private var theme
    @StateObject private var listState = MusicListState()
    
    let musicInfoList: [MusicInfo_]
    let config: MusicListConfig
    
    var body: some View {
        VStack(spacing: 0) {
            if listState.isEditMode {
                editToolbar
            } else {
                headerView
            }
            
            if !musicInfoList.isEmpty {
                Divider().padding(.vertical, 8)
            }
            
            ForEach(Array(musicInfoList.enumerated()), id: \.offset) { index, info in
                MusicListItem(
                    musicInfo: info,
                    index: index,
                    config: config,
                    isCurrentPlaying: config.currentPlayingIndex == index,
                    isEditMode: listState.isEditMode,
                    isSelected: listState.selectedIds.contains(info.music.id),
                    callbacks: config.callbacks
                )
            }
        }
    }
    
    @ViewBuilder
    private var headerView: some View {
        switch config.header {
        case .none:
            EmptyView()
        case .simple(let onOrderPlay, let onShufflePlay, let trailing):
            SimpleHeader(count: musicInfoList.count, onOrderPlay: onOrderPlay, onShufflePlay: onShufflePlay, trailing: trailing)
        case .full(let selectedGenre, let selectedOrder, let onOrderPlay, let onShufflePlay):
            FullHeader(selectedGenre: selectedGenre, selectedOrder: selectedOrder, count: musicInfoList.count, onOrderPlay: onOrderPlay, onShufflePlay: onShufflePlay)
        case .custom(let content):
            content()
        }
    }
    
    private var editToolbar: some View {
        HStack(spacing: 12) {
            Button {
                listState.selectedIds = listState.selectedIds.count == musicInfoList.count ? [] : Set(musicInfoList.map { $0.music.id })
            } label: {
                Text(listState.selectedIds.count == musicInfoList.count ? "取消全选" : "全选")
                    .font(TypographyTokens.bodySmall).foregroundColor(theme.primary)
            }
            Spacer()
            Button { listState.exitEditMode(); config.callbacks.onExitEditMode?() } label: {
                Text("完成").font(TypographyTokens.bodySmall).foregroundColor(theme.primary)
            }
        }
        .padding(.horizontal, 16).padding(.vertical, 8).background(theme.surface)
    }
}

// MARK: - MusicListState

class MusicListState: ObservableObject {
    @Published var isEditMode = false
    @Published var selectedIds: Set<Int64> = []
    
    func enterEditMode() { isEditMode = true }
    func exitEditMode() { isEditMode = false; selectedIds.removeAll() }
}

// MARK: - MusicListItem

struct MusicListItem: View {
    @Environment(HMPTheme.self) private var theme
    @State private var showMenu = false
    
    let musicInfo: MusicInfo_
    let index: Int
    let config: MusicListConfig
    let isCurrentPlaying: Bool
    let isEditMode: Bool
    let isSelected: Bool
    let callbacks: MusicListCallbacks
    
    var body: some View {
        HStack(spacing: 10) {
            // 序号或复选框
            if config.item.showIndex || isEditMode {
                if isEditMode {
                    Checkbox(isChecked: isSelected)
                        .frame(width: 24)
                } else {
                    Text("\(index + 1)")
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(isCurrentPlaying ? theme.primary : theme.text.opacity(0.4))
                        .frame(width: 24)
                }
            }
            
            // 行内容
            switch config.item.variant {
            case .full:
                FullRowContent(musicInfo: musicInfo, index: index, isCurrentPlaying: isCurrentPlaying, config: config.item, callbacks: callbacks, showMenu: $showMenu)
            case .compact:
                CompactRowContent(musicInfo: musicInfo, isCurrentPlaying: isCurrentPlaying, config: config.item, callbacks: callbacks, showMenu: $showMenu)
            case .gallery:
                GalleryRowContent(musicInfo: musicInfo, isCurrentPlaying: isCurrentPlaying, config: config.item, callbacks: callbacks, showMenu: $showMenu)
            case .custom:
                EmptyView()
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.leading, config.item.showIndex ? 4 : 10)
        .padding(.trailing, config.item.variant == .gallery ? 28 : 6)
        .padding(.vertical, 4)
        .contentShape(Rectangle())
        .onTapGesture {
            if isEditMode {
                callbacks.onSelectionChange?(musicInfo.music.id, !isSelected)
            } else {
                HapticManager.shared.click()
                callbacks.onItemClick?(musicInfo, index)
            }
        }
    }
}

// MARK: - FullRowContent

struct FullRowContent: View {
    @Environment(HMPTheme.self) private var theme
    let musicInfo: MusicInfo_
    let index: Int
    let isCurrentPlaying: Bool
    let config: ItemConfig
    let callbacks: MusicListCallbacks
    @Binding var showMenu: Bool
    
    var body: some View {
        AlbumCover(
            uri: musicInfo.music.albumArtUri,
            musicPath: musicInfo.music.path,
            size: 56,
            cornerRadius: 10
        )
        
        VStack(alignment: .leading, spacing: 2) {
            Text(musicInfo.music.title)
                .font(TypographyTokens.headlineSmall)
                .foregroundColor(theme.text)
                .lineLimit(1)
            Text(musicInfo.music.artist)
                .font(TypographyTokens.labelSmall)
                .foregroundColor(theme.text.opacity(0.6))
                .lineLimit(1)
            Text(musicInfo.music.album)
                .font(TypographyTokens.labelSmall)
                .foregroundColor(theme.text.opacity(0.5))
                .lineLimit(1)
        }
        
        Spacer()
        
        // 操作按钮
        HStack(spacing: 0) {
            if config.showPin {
                actionButton(systemName: "chevron.up.circle", tint: isCurrentPlaying ? theme.primary : theme.text.opacity(0.6)) {
                    HapticManager.shared.click()
                    callbacks.onPinToTop?(musicInfo)
                }
            }
            if config.showMoveButtons {
                moveButtons
            }
            if config.showRemove {
                actionButton(systemName: "trash", tint: isCurrentPlaying ? theme.primary : theme.text.opacity(0.6)) {
                    HapticManager.shared.click()
                    callbacks.onRemove?(musicInfo)
                }
            }
            if config.showMenu {
                MenuButtonView(musicInfo: musicInfo, config: config, callbacks: callbacks, showMenu: $showMenu)
            }
        }
    }
    
    @ViewBuilder
    private var moveButtons: some View {
        VStack(spacing: 0) {
            Button {
                HapticManager.shared.click()
                callbacks.onMoveUp?(index)
            } label: {
                Image(systemName: "chevron.up")
                    .font(.system(size: 16))
                    .foregroundColor(theme.text.opacity(0.6))
            }
            .buttonStyle(.plain)
            .frame(width: 28, height: 20)
            
            Button {
                HapticManager.shared.click()
                callbacks.onMoveDown?(index)
            } label: {
                Image(systemName: "chevron.down")
                    .font(.system(size: 16))
                    .foregroundColor(theme.text.opacity(0.6))
            }
            .buttonStyle(.plain)
            .frame(width: 28, height: 20)
        }
    }
}

// MARK: - CompactRowContent

struct CompactRowContent: View {
    @Environment(HMPTheme.self) private var theme
    let musicInfo: MusicInfo_
    let isCurrentPlaying: Bool
    let config: ItemConfig
    let callbacks: MusicListCallbacks
    @Binding var showMenu: Bool
    
    var body: some View {
        AlbumCover(
            uri: musicInfo.music.albumArtUri,
            musicPath: musicInfo.music.path,
            size: 48,
            cornerRadius: 8
        )
        
        VStack(alignment: .leading, spacing: 2) {
            Text(musicInfo.music.title)
                .font(TypographyTokens.bodyMedium)
                .foregroundColor(theme.text)
                .lineLimit(1)
            Text(musicInfo.music.artist)
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text.opacity(0.6))
                .lineLimit(1)
        }
        
        Spacer()
        
        HStack(spacing: 0) {
            if config.showPin {
                actionButton(systemName: "chevron.up.circle", size: 20, tint: isCurrentPlaying ? theme.primary : theme.text.opacity(0.6)) {
                    callbacks.onPinToTop?(musicInfo)
                }
            }
            if config.showRemove {
                actionButton(systemName: "trash", size: 20, tint: isCurrentPlaying ? theme.primary : theme.text.opacity(0.6)) {
                    callbacks.onRemove?(musicInfo)
                }
            }
            if config.showMenu {
                MenuButtonView(musicInfo: musicInfo, config: config, callbacks: callbacks, showMenu: $showMenu)
            }
        }
    }
}

// MARK: - GalleryRowContent

struct GalleryRowContent: View {
    @Environment(HMPTheme.self) private var theme
    let musicInfo: MusicInfo_
    let isCurrentPlaying: Bool
    let config: ItemConfig
    let callbacks: MusicListCallbacks
    @Binding var showMenu: Bool
    
    var body: some View {
        AlbumCover(
            uri: musicInfo.music.albumArtUri,
            musicPath: musicInfo.music.path,
            size: 56,
            cornerRadius: 10
        )
        
        VStack(alignment: .leading, spacing: 2) {
            Text(musicInfo.music.title)
                .font(TypographyTokens.headlineSmall)
                .foregroundColor(theme.text)
                .lineLimit(1)
            Text(musicInfo.music.artist)
                .font(TypographyTokens.labelSmall)
                .foregroundColor(theme.text.opacity(0.6))
                .lineLimit(1)
            Text(musicInfo.music.album)
                .font(TypographyTokens.labelSmall)
                .foregroundColor(theme.text.opacity(0.5))
                .lineLimit(1)
        }
        
        Spacer()
        
        HStack(spacing: 0) {
            if config.showPin {
                actionButton(systemName: "chevron.up.circle", size: 20, tint: isCurrentPlaying ? theme.primary : theme.text.opacity(0.6)) {
                    callbacks.onPinToTop?(musicInfo)
                }
            }
            if config.showRemove {
                actionButton(systemName: "trash", size: 20, tint: isCurrentPlaying ? theme.primary : theme.text.opacity(0.6)) {
                    callbacks.onRemove?(musicInfo)
                }
            }
            if config.showMenu {
                MenuButtonView(musicInfo: musicInfo, config: config, callbacks: callbacks, showMenu: $showMenu)
            }
        }
    }
}

// MARK: - Header Views

struct SimpleHeader: View {
    @Environment(HMPTheme.self) private var theme
    let count: Int
    let onOrderPlay: () -> Void
    let onShufflePlay: () -> Void
    let trailing: (() -> AnyView)?
    
    var body: some View {
        HStack {
            Text("共 \(count) 首歌曲")
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text.opacity(0.5))
            
            Spacer()
            
            if let trailing {
                trailing()
            }
            
            Button {
                HapticManager.shared.click()
                onOrderPlay()
            } label: {
                Image(systemName: "play.fill").font(.system(size: 14)).foregroundColor(theme.primary)
            }
            
            Button {
                HapticManager.shared.click()
                onShufflePlay()
            } label: {
                Image(systemName: "shuffle").font(.system(size: 14)).foregroundColor(theme.primary)
            }
        }
        .padding(.horizontal, 8)
    }
}

struct FullHeader: View {
    @Environment(HMPTheme.self) private var theme
    let selectedGenre: String
    let selectedOrder: String
    let count: Int
    let onOrderPlay: () -> Void
    let onShufflePlay: () -> Void
    
    var body: some View {
        HStack {
            Text("共 \(count) 首歌曲")
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text.opacity(0.5))
            
            Spacer()
            
            Button {
                HapticManager.shared.click()
                onOrderPlay()
            } label: {
                Image(systemName: "play.fill").font(.system(size: 14)).foregroundColor(theme.primary)
            }
            
            Button {
                HapticManager.shared.click()
                onShufflePlay()
            } label: {
                Image(systemName: "shuffle").font(.system(size: 14)).foregroundColor(theme.primary)
            }
        }
        .padding(.horizontal, 8)
    }
}

// MARK: - Helpers

func actionButton(systemName: String, size: CGFloat = 24, tint: Color, action: @escaping () -> Void) -> some View {
    Button {
        HapticManager.shared.click()
        action()
    } label: {
        Image(systemName: systemName)
            .font(.system(size: size))
            .foregroundColor(tint)
    }
    .buttonStyle(.plain)
    .frame(width: 32, height: 40)
}

struct Checkbox: View {
    @Environment(HMPTheme.self) private var theme
    let isChecked: Bool
    
    var body: some View {
        ZStack {
            Circle()
                .stroke(isChecked ? theme.primary : theme.text.opacity(0.5), lineWidth: 1.5)
                .frame(width: 14, height: 14)
            if isChecked {
                Circle()
                    .fill(theme.primary)
                    .frame(width: 6, height: 6)
            }
        }
    }
}

// MARK: - Menu Button

struct MenuButtonView: View {
    @Environment(HMPTheme.self) private var theme
    let musicInfo: MusicInfo_
    let config: ItemConfig
    let callbacks: MusicListCallbacks
    @Binding var showMenu: Bool
    
    var body: some View {
        Button {
            HapticManager.shared.click()
            showMenu.toggle()
        } label: {
            Image(systemName: "ellipsis.circle")
                .font(.system(size: 20))
                .foregroundColor(theme.text.opacity(0.6))
        }
        .buttonStyle(.plain)
        .frame(width: 32, height: 40)
        .popover(isPresented: $showMenu) {
            VStack(alignment: .leading, spacing: 0) {
                menuButton("歌曲详情") {
                    showMenu = false
                    callbacks.onMenuClick?(musicInfo)
                }
                if config.extraMenuItems.contains(where: { $0.0 == "加入播放列表" }) {
                    menuButton("加入播放列表") {
                        showMenu = false
                        if let idx = config.extraMenuItems.firstIndex(where: { $0.0 == "加入播放列表" }) {
                            config.extraMenuItems[idx].1(musicInfo)
                        }
                    }
                }
                ForEach(config.extraMenuItems.filter { $0.0 != "加入播放列表" }, id: \.0) { item in
                    menuButton(item.0) {
                        showMenu = false
                        item.1(musicInfo)
                    }
                }
            }
            .padding(8)
            .presentationCompactAdaptation(.popover)
        }
    }
    
    private func menuButton(_ title: String, action: @escaping () -> Void) -> some View {
        Button {
            HapticManager.shared.click()
            action()
        } label: {
            Text(title)
                .font(TypographyTokens.bodyMedium)
                .foregroundColor(theme.text)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
        }
        .buttonStyle(.plain)
    }
}
