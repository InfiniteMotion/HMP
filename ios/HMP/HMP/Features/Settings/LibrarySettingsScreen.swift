import SwiftUI
import shared

struct LibrarySettingsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var libraryVM = LibraryViewModel()
    @State private var isScanning: Bool = false
    @State private var showFullRescanConfirm: Bool = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                LibraryStatsSection(
                    musicCount: Int(libraryVM.musicCount),
                    analyzedCount: Int(libraryVM.musicWithExtraCount)
                )
                
                ScanOptionsSection(
                    isScanning: $isScanning,
                    onIncrementalScan: {
                        Task {
                            await libraryVM.incrementalSync()
                        }
                    },
                    onFullRescan: {
                        showFullRescanConfirm = true
                    }
                )
                
                LibraryManagementSection()
            }
            .padding(24)
            .padding(.bottom, 64)
        }
        .alert("确认全量重建", isPresented: $showFullRescanConfirm) {
            Button("全量重建", role: .destructive) {
                Task {
                    await libraryVM.fullRescan()
                }
            }
            Button("取消", role: .cancel) { }
        } message: {
            Text("全量重建将清空现有数据库并重新扫描所有音乐,此操作不可撤销。")
        }
    }
}

struct LibraryStatsSection: View {
    @Environment(HMPTheme.self) private var theme
    let musicCount: Int
    let analyzedCount: Int
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("音乐库统计")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            HStack(spacing: 16) {
                StatsCard(
                    title: "总歌曲数",
                    value: "\(musicCount)",
                    icon: "music.note.list"
                )
                
                StatsCard(
                    title: "已分析歌曲",
                    value: "\(analyzedCount)",
                    icon: "chart.bar"
                )
            }
        }
    }
}

struct StatsCard: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let value: String
    let icon: String
    
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 24))
                .foregroundColor(theme.primary)
            
            Text(value)
                .font(.system(size: 28, weight: .bold))
                .foregroundColor(theme.text)
            
            Text(title)
                .font(.caption)
                .foregroundColor(theme.text.opacity(0.7))
        }
        .frame(maxWidth: .infinity)
        .padding(16)
        .background(theme.surfaceVariant.opacity(0.3))
        .cornerRadius(16)
    }
}

struct ScanOptionsSection: View {
    @Environment(HMPTheme.self) private var theme
    @Binding var isScanning: Bool
    let onIncrementalScan: () -> Void
    let onFullRescan: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("扫描选项")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            HStack(spacing: 16) {
                ScanOptionCard(
                    title: "增量扫描",
                    description: "快速检查新增音乐",
                    icon: "magnifyingglass",
                    onClick: onIncrementalScan,
                    isDestructive: false
                )
                
                ScanOptionCard(
                    title: "全量重建",
                    description: "清空并重新扫描",
                    icon: "arrow.triangle.2.circlepath",
                    onClick: onFullRescan,
                    isDestructive: true
                )
            }
            
            if isScanning {
                HStack(spacing: 8) {
                    ProgressView()
                    Text("扫描中...")
                        .font(.body)
                        .foregroundColor(theme.primary)
                }
                .frame(maxWidth: .infinity)
            }
        }
    }
}

struct ScanOptionCard: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let description: String
    let icon: String
    let onClick: () -> Void
    let isDestructive: Bool
    
    var body: some View {
        Button {
            onClick()
        } label: {
            VStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 32))
                    .foregroundColor(isDestructive ? .red : theme.text.opacity(0.7))
                
                Text(title)
                    .font(.headline)
                    .foregroundColor(isDestructive ? .red : theme.text)
                
                Text(description)
                    .font(.caption)
                    .foregroundColor(theme.text.opacity(0.6))
                    .multilineTextAlignment(.center)
                    .lineLimit(3)
            }
            .frame(maxWidth: .infinity, minHeight: 120)
            .padding(16)
            .background(theme.surfaceVariant.opacity(0.3))
            .cornerRadius(16)
        }
        .buttonStyle(.plain)
    }
}

struct LibraryManagementSection: View {
    @Environment(HMPTheme.self) private var theme
    @State private var scannedFolders: [FolderInfo] = []
    @State private var hiddenFolders: [HiddenFolderInfo] = []
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("音乐库管理")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            VStack(alignment: .leading, spacing: 12) {
                Text("已扫描文件夹")
                    .font(.subheadline)
                    .foregroundColor(theme.text.opacity(0.7))
                
                if scannedFolders.isEmpty {
                    Text("暂无已扫描的文件夹")
                        .font(.body)
                        .foregroundColor(theme.text.opacity(0.5))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                } else {
                    ForEach(scannedFolders, id: \.path) { folder in
                        FolderItem(
                            path: folder.path,
                            songCount: Int(folder.songCount),
                            onHide: { /* 隐藏文件夹 */ }
                        )
                    }
                }
                
                if !hiddenFolders.isEmpty {
                    Divider()
                        .padding(.vertical, 8)
                    
                    Text("已隐藏文件夹")
                        .font(.subheadline)
                        .foregroundColor(theme.text.opacity(0.7))
                    
                    ForEach(hiddenFolders, id: \.path) { hidden in
                        HiddenFolderItem(
                            path: hidden.path,
                            songCount: Int(hidden.songCount),
                            onUnhide: { /* 取消隐藏 */ }
                        )
                    }
                }
            }
            .padding(16)
            .background(theme.surfaceVariant.opacity(0.1))
            .cornerRadius(16)
        }
    }
}

struct FolderItem: View {
    @Environment(HMPTheme.self) private var theme
    let path: String
    let songCount: Int
    let onHide: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "folder")
                .font(.system(size: 24))
                .foregroundColor(theme.secondary)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(path)
                    .font(.caption)
                    .foregroundColor(theme.text)
                    .lineLimit(1)
                
                Text("\(songCount) 首歌曲")
                    .font(.caption2)
                    .foregroundColor(theme.text.opacity(0.6))
            }
            
            Spacer()
            
            Button {
                onHide()
            } label: {
                Text("隐藏")
                    .font(.caption)
                    .foregroundColor(.red)
            }
        }
        .padding(12)
        .background(theme.surfaceVariant.opacity(0.3))
        .cornerRadius(12)
    }
}

struct HiddenFolderItem: View {
    @Environment(HMPTheme.self) private var theme
    let path: String
    let songCount: Int
    let onUnhide: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "folder.badge.minus")
                .font(.system(size: 24))
                .foregroundColor(theme.text.opacity(0.5))
            
            VStack(alignment: .leading, spacing: 4) {
                Text(path)
                    .font(.caption)
                    .foregroundColor(theme.text.opacity(0.7))
                    .lineLimit(1)
                
                Text("\(songCount) 首歌曲")
                    .font(.caption2)
                    .foregroundColor(theme.text.opacity(0.5))
            }
            
            Spacer()
            
            Button {
                onUnhide()
            } label: {
                Text("取消隐藏")
                    .font(.caption)
                    .foregroundColor(theme.primary)
            }
        }
        .padding(12)
        .background(theme.surfaceVariant.opacity(0.2))
        .cornerRadius(12)
    }
}

struct FolderInfo {
    let path: String
    let songCount: Int32
}

struct HiddenFolderInfo {
    let path: String
    let songCount: Int32
    let musicIds: [Int64]
}

#Preview {
    LibrarySettingsScreen()
}
