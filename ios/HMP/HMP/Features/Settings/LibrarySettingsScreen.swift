import SwiftUI
import shared

/// 音乐库设置页 - 对应 Android LibrarySettingsScreen
/// 提供音乐扫描和同步功能
struct LibrarySettingsScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = LibraryViewModel()
    @State private var showConfirmRescan = false

    var body: some View {
        List {
            // 音乐统计 - 统计卡片
            Section("音乐统计") {
                HStack(spacing: 12) {
                    StatsCard(
                        title: "已导入",
                        count: viewModel.musicCount,
                        icon: "music.note",
                        color: theme.primary
                    )
                    StatsCard(
                        title: "已分析",
                        count: viewModel.musicWithExtraCount,
                        icon: "waveform",
                        color: .orange
                    )
                }
                .listRowInsets(EdgeInsets(top: 8, leading: 0, bottom: 8, trailing: 0))
            }

            // 扫描操作
            Section("音乐扫描") {
                Button {
                    showConfirmRescan = true
                } label: {
                    HStack {
                        Image(systemName: "arrow.clockwise")
                        Text("全盘扫描")
                        Spacer()
                        if viewModel.isScanning {
                            ProgressView()
                        }
                    }
                }
                .disabled(viewModel.isScanning)

                Button {
                    Task {
                        await viewModel.incrementalSync()
                    }
                } label: {
                    HStack {
                        Image(systemName: "arrow.triangle.2.circlepath")
                        Text("增量同步")
                        Spacer()
                        if viewModel.isScanning {
                            ProgressView()
                        }
                    }
                }
                .disabled(viewModel.isScanning)
            }

            // 使用说明
            Section("使用说明") {
                VStack(alignment: .leading, spacing: 8) {
                    Text("如何导入音乐")
                        .font(.headline)
                    Text("1. 打开 iOS 的「文件」App")
                    Text("2. 将音乐文件复制到 HMP 文件夹中")
                    Text("3. 返回此页面点击「全盘扫描」")
                        .padding(.bottom, 8)

                    Text("支持格式")
                        .font(.headline)
                    Text("MP3, FLAC, M4A, AAC, WAV, OGG, OPUS, WMA")
                        .foregroundColor(theme.text.opacity(0.6))
                }
                .padding(.vertical, 8)
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("音乐库设置")
        .navigationBarTitleDisplayMode(.inline)
        .alert("确认全盘扫描", isPresented: $showConfirmRescan) {
            Button("取消", role: .cancel) {}
            Button("扫描") {
                Task {
                    await viewModel.fullRescan()
                }
            }
        } message: {
            Text("全盘扫描会重新扫描所有音乐文件，可能需要一些时间。")
        }
        .alert("错误", isPresented: .constant(viewModel.errorMessage != nil)) {
            Button("确定") {
                viewModel.errorMessage = nil
            }
        } message: {
            Text(viewModel.errorMessage ?? "")
        }
    }
}

// MARK: - Stats Card

struct StatsCard: View {
    @Environment(HMPTheme.self) private var theme

    let title: String
    let count: Int32
    let icon: String
    let color: Color

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 24))
                .foregroundColor(color)
            Text("\(count)")
                .font(TypographyTokens.titleLarge)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            Text(title)
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text.opacity(0.6))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .background(theme.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
