import SwiftUI
import shared

/// 音乐库设置页 - 对应 Android LibrarySettingsScreen
/// 提供音乐扫描和同步功能
struct LibrarySettingsScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel = LibraryViewModel()
    @State private var showConfirmRescan = false

    var body: some View {
        List {
            // 音乐统计
            Section("音乐统计") {
                HStack {
                    Text("已导入音乐")
                    Spacer()
                    Text("\(viewModel.musicCount) 首")
                        .foregroundColor(theme.text.opacity(0.6))
                }
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
