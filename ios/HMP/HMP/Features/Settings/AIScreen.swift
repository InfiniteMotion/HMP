import SwiftUI
import shared

/// AI 设置页面 - 对应 Android AIScreen.kt
/// AI 服务商配置、API Key 管理、批量处理、每日推荐策略
struct AIScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel = SettingsViewModel()

    @State private var showApiKey: Bool = false
    @State private var showSaveSuccess: Bool = false
    @State private var saveErrorMessage: String?

    private let providers: [AiProviderType] = [
        .deepseek, .openai, .claude, .qwen, .ernie
    ]

    var body: some View {
        Form {
            aiProviderSection
            batchProcessSection
            dailyRefreshSection
        }
        .navigationTitle("AI 设置")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.refreshPendingCount()
        }
        .onChange(of: viewModel.currentAiProvider) { _, newProvider in
            viewModel.switchAiProvider(newProvider)
        }
        .alert("已保存", isPresented: $showSaveSuccess) {
            Button("确定", role: .cancel) {}
        }
        .alert("保存失败", isPresented: .constant(saveErrorMessage != nil)) {
            Button("确定") { saveErrorMessage = nil }
        } message: {
            Text(saveErrorMessage ?? "")
        }
    }

    // MARK: - AI 服务商配置

    @ViewBuilder
    private var aiProviderSection: some View {
        Section("AI 服务商") {
            providerPicker
            apiKeyField
            modelField
            connectionButtons
        }
    }

    private var providerPicker: some View {
        Picker("服务商", selection: $viewModel.currentAiProvider) {
            ForEach(providers, id: \.self) { provider in
                HStack {
                    Text(provider.displayName)
                    if provider == viewModel.currentAiProvider && viewModel.isProviderConfigured {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(.green)
                            .font(.caption)
                    }
                }
                .tag(provider)
            }
        }
    }

    private var apiKeyField: some View {
        HStack {
            if showApiKey {
                TextField("API Key", text: $viewModel.providerApiKey)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
            } else {
                SecureField("API Key", text: $viewModel.providerApiKey)
            }
            Button {
                showApiKey.toggle()
            } label: {
                Image(systemName: showApiKey ? "eye.slash.fill" : "eye.fill")
                    .foregroundColor(theme.text.opacity(0.5))
            }
        }
    }

    private var modelField: some View {
        HStack {
            TextField("模型名称", text: $viewModel.providerModel)
                .autocapitalization(.none)
                .disableAutocorrection(true)
            Text(viewModel.currentAiProvider.defaultModel)
                .font(.caption)
                .foregroundColor(theme.text.opacity(0.4))
        }
    }

    private var connectionButtons: some View {
        VStack(spacing: 12) {
            // Test Connection
            Button {
                viewModel.testConnection()
            } label: {
                HStack {
                    if viewModel.isTestingApi {
                        ProgressView()
                            .scaleEffect(0.8)
                    } else {
                        Image(systemName: "antenna.radiowaves.left.and.right")
                    }
                    Text(viewModel.isTestingApi ? "测试中..." : "测试连接")
                }
                .frame(maxWidth: .infinity)
            }
            .disabled(viewModel.isTestingApi || viewModel.providerApiKey.isEmpty)
            .buttonStyle(.bordered)

            // Test Result
            if let result = viewModel.apiTestResult {
                HStack {
                    Image(systemName: result.icon)
                    Text(result.message)
                        .font(.caption)
                }
                .foregroundColor(result.color)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(result.color.opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 6))
            }

            // Save Button
            Button {
                viewModel.saveProviderConfig()
                showSaveSuccess = true
            } label: {
                HStack {
                    Image(systemName: "square.and.arrow.down")
                    Text("保存配置")
                }
                .frame(maxWidth: .infinity)
            }
            .disabled(viewModel.providerApiKey.isEmpty)
            .buttonStyle(.borderedProminent)
            .tint(theme.primary)
        }
        .padding(.vertical, 4)
    }

    // MARK: - 批量处理

    @ViewBuilder
    private var batchProcessSection: some View {
        Section {
            VStack(alignment: .leading, spacing: 8) {
                // Status info
                HStack {
                    Text("待处理歌曲")
                    Spacer()
                    Text("\(viewModel.pendingMusicCount) 首")
                        .foregroundColor(theme.text.opacity(0.6))

                    if viewModel.pendingMusicCount > 0 {
                        Button("刷新") {
                            viewModel.refreshPendingCount()
                        }
                        .font(.caption)
                    }
                }

                // Auto batch toggle
                Toggle("后台自动补全", isOn: Binding(
                    get: { viewModel.autoBatchProcess },
                    set: { viewModel.saveAutoBatchProcess($0) }
                ))

                if !viewModel.autoBatchProcess && viewModel.pendingMusicCount > 0 {
                    Divider()

                    if viewModel.isProcessing {
                        // Progress
                        VStack(spacing: 8) {
                            ProgressView(value: Double(viewModel.processingProgress))
                                .tint(theme.primary)

                            HStack {
                                Button("暂停") {
                                    viewModel.pauseBatchProcess()
                                }
                                .buttonStyle(.bordered)
                                .controlSize(.small)

                                Spacer()

                                Button("取消") {
                                    viewModel.cancelBatchProcess()
                                }
                                .buttonStyle(.bordered)
                                .tint(.red)
                                .controlSize(.small)
                            }
                        }
                    } else {
                        // Start button
                        Button {
                            viewModel.startBatchProcess()
                        } label: {
                            HStack {
                                Image(systemName: "play.fill")
                                Text("开始补全 (\(viewModel.pendingMusicCount) 首)")
                            }
                            .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(theme.primary)
                    }
                }
            }
        } header: {
            Text("AI 元数据补全")
        }
    }

    // MARK: - 每日推荐策略

    @ViewBuilder
    private var dailyRefreshSection: some View {
        Section("每日推荐") {
            Picker("刷新策略", selection: $viewModel.dailyRefreshMode) {
                Text("按时间间隔").tag("time")
                Text("按启动次数").tag("startup")
                Text("智能刷新").tag("smart")
            }
            .onChange(of: viewModel.dailyRefreshMode) { _, newMode in
                viewModel.saveDailyRefreshMode(newMode)
            }

            if viewModel.dailyRefreshMode == "time" {
                HStack {
                    Text("刷新间隔 (小时)")
                    Spacer()
                    Stepper("\(viewModel.dailyRefreshHours)", value: $viewModel.dailyRefreshHours, in: 1...72)
                        .onChange(of: viewModel.dailyRefreshHours) { _, newVal in
                            viewModel.saveDailyRefreshHours(newVal)
                        }
                }
            }

            if viewModel.dailyRefreshMode == "startup" {
                HStack {
                    Text("启动次数触发")
                    Spacer()
                    Stepper("\(viewModel.dailyRefreshStartupCount)", value: $viewModel.dailyRefreshStartupCount, in: 1...20)
                        .onChange(of: viewModel.dailyRefreshStartupCount) { _, newVal in
                            viewModel.saveDailyRefreshStartupCount(newVal)
                        }
                }
            }

            if viewModel.dailyRefreshMode == "smart" {
                VStack(alignment: .leading, spacing: 4) {
                    Text("智能刷新会根据您的使用习惯自动选择最佳刷新时机")
                        .font(.caption)
                        .foregroundColor(theme.text.opacity(0.5))
                }
            }
        }
    }
}

// MARK: - ApiTestResult extensions

private extension ApiTestResult {
    var icon: String {
        switch self {
        case .success: return "checkmark.circle.fill"
        case .error: return "xmark.circle.fill"
        }
    }

    var message: String {
        switch self {
        case .success: return "连接成功"
        case .error(let msg): return msg
        }
    }

    var color: Color {
        switch self {
        case .success: return .green
        case .error: return .red
        }
    }
}
