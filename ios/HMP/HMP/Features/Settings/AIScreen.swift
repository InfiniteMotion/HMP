import SwiftUI
import shared

struct AIScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var selectedProvider: AiProviderType = .openai
    @State private var apiKey: String = ""
    @State private var modelName: String = ""
    @State private var showPassword: Bool = false
    @State private var isTestingApi: Bool = false
    @State private var apiTestResult: String?
    @State private var autoBatchProcess: Bool = false
    @State private var refreshMode: String = "time"
    @State private var refreshHours: Int = 12
    @State private var startupCount: Int = 5
    @State private var showProviderPicker: Bool = false
    @State private var pendingCount: Int = 0
    @State private var musicWithExtraCount: Int = 0
    @State private var isProcessing: Bool = false
    @State private var isPaused: Bool = false
    @State private var currentMusicTitle: String = ""
    @State private var processedCount: Int = 0
    @State private var totalCount: Int = 0
    @State private var progress: Double = 0.0
    
    var body: some View {
        SubScreen(title: "AI 配置") {
            ScrollView {
                VStack(spacing: 24) {
                    AiProviderConfig(
                        selectedProvider: $selectedProvider,
                        apiKey: $apiKey,
                        modelName: $modelName,
                        showPassword: $showPassword,
                        isTestingApi: $isTestingApi,
                        apiTestResult: $apiTestResult,
                        showProviderPicker: $showProviderPicker,
                        onTestConnection: testConnection,
                        onSaveConfig: saveConfig
                    )
                    
                    LoadMusicExtraInfo(
                        pendingCount: pendingCount,
                        musicWithExtraCount: musicWithExtraCount,
                        isProcessing: isProcessing,
                        isPaused: isPaused,
                        currentMusicTitle: currentMusicTitle,
                        processedCount: processedCount,
                        totalCount: totalCount,
                        progress: progress,
                        autoBatchProcess: autoBatchProcess,
                        onAutoBatchProcessChange: { autoBatchProcess = $0 },
                        startAutoProcessExtraInfo: startBatchProcessing,
                        pauseProcess: pauseProcessing,
                        resumeProcess: resumeProcessing,
                        cancelProcess: cancelProcessing
                    )
                    
                    DailyRefreshSettings(
                        refreshMode: $refreshMode,
                        refreshHours: $refreshHours,
                        startupCount: $startupCount
                    )
                }
                .padding(24)
                .padding(.bottom, 64)
            }
            .onAppear {
                loadInitialData()
            }
            .sheet(isPresented: $showProviderPicker) {
                ProviderPicker(selectedProvider: $selectedProvider)
            }
        }
    }
    
    private func loadInitialData() {
        // 模拟加载数据
        selectedProvider = .openai
        modelName = AiProviderType.openai.defaultModel
        pendingCount = 42
        musicWithExtraCount = 156
        autoBatchProcess = true
        refreshMode = "time"
        refreshHours = 12
        startupCount = 5
    }
    
    private func testConnection() {
        guard !apiKey.isEmpty else { return }
        isTestingApi = true
        
        // 模拟测试
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            isTestingApi = false
            apiTestResult = "连接成功"
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                apiTestResult = nil
            }
        }
    }
    
    private func saveConfig() {
        guard !apiKey.isEmpty else { return }
        // 保存配置
    }
    
    private func startBatchProcessing() {
        isProcessing = true
        totalCount = pendingCount
        processedCount = 0
        progress = 0.0
        
        // 模拟处理
        Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { timer in
            guard processedCount < totalCount else {
                timer.invalidate()
                isProcessing = false
                return
            }
            guard !isPaused else { return }
            processedCount += 1
            progress = Double(processedCount) / Double(totalCount)
            currentMusicTitle = "歌曲 \(processedCount)"
        }
    }
    
    private func pauseProcessing() {
        isPaused = true
    }
    
    private func resumeProcessing() {
        isPaused = false
    }
    
    private func cancelProcessing() {
        isProcessing = false
        isPaused = false
        progress = 0.0
    }
}

struct AiProviderConfig: View {
    @Environment(HMPTheme.self) private var theme
    @Binding var selectedProvider: AiProviderType
    @Binding var apiKey: String
    @Binding var modelName: String
    @Binding var showPassword: Bool
    @Binding var isTestingApi: Bool
    @Binding var apiTestResult: String?
    @Binding var showProviderPicker: Bool
    
    var onTestConnection: () -> Void
    var onSaveConfig: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("AI 服务商配置")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            VStack(spacing: 16) {
                // 服务商选择
                VStack(alignment: .leading, spacing: 8) {
                    Text("当前 AI 服务商")
                        .font(.subheadline)
                        .foregroundColor(theme.text)
                    
                    Button {
                        showProviderPicker = true
                    } label: {
                        HStack {
                            Text(selectedProvider.displayName)
                                .foregroundColor(theme.text)
                            Spacer()
                            Image(systemName: "chevron.down")
                                .foregroundColor(theme.text.opacity(0.6))
                        }
                        .padding()
                        .background(theme.surfaceVariant.opacity(0.3))
                        .cornerRadius(12)
                    }
                }
                
                // 配置状态
                HStack {
                    Text("已配置")
                        .font(.subheadline)
                        .foregroundColor(theme.primary)
                    Spacer()
                }
                
                // API Key
                VStack(alignment: .leading, spacing: 8) {
                    Text("API Key")
                        .font(.subheadline)
                        .foregroundColor(theme.text)
                    
                    HStack {
                        if showPassword {
                            TextField("请输入 API Key", text: $apiKey)
                        } else {
                            SecureField("请输入 API Key", text: $apiKey)
                        }
                        
                        Button {
                            showPassword.toggle()
                        } label: {
                            Image(systemName: showPassword ? "eye.slash" : "eye")
                                .foregroundColor(theme.text.opacity(0.6))
                        }
                    }
                    .padding()
                    .background(theme.surfaceVariant.opacity(0.3))
                    .cornerRadius(12)
                }
                
                // Model Name
                VStack(alignment: .leading, spacing: 8) {
                    Text("模型名称")
                        .font(.subheadline)
                        .foregroundColor(theme.text)
                    
                    TextField("默认: \(selectedProvider.defaultModel)", text: $modelName)
                        .padding()
                        .background(theme.surfaceVariant.opacity(0.3))
                        .cornerRadius(12)
                }
                
                // 按钮
                HStack(spacing: 32) {
                    Button {
                        onTestConnection()
                    } label: {
                        if isTestingApi {
                            ProgressView()
                                .frame(width: 120)
                        } else {
                            Text("测试连接")
                                .frame(width: 120)
                        }
                    }
                    .disabled(apiKey.isEmpty || isTestingApi)
                    .buttonStyle(.borderedProminent)
                    .tint(theme.primary)
                    
                    Button {
                        onSaveConfig()
                    } label: {
                        Text("保存配置")
                            .frame(width: 120)
                    }
                    .disabled(apiKey.isEmpty)
                    .buttonStyle(.borderedProminent)
                    .tint(theme.primary)
                }
                .frame(maxWidth: .infinity)
                
                if let result = apiTestResult {
                    Text(result)
                        .font(.caption)
                        .foregroundColor(theme.primary)
                        .frame(maxWidth: .infinity)
                }
                
                // 提示信息
                Text("切换服务商需要重新输入 API Key")
                    .font(.caption)
                    .foregroundColor(theme.text.opacity(0.6))
            }
            .padding(16)
            .background(theme.surfaceVariant.opacity(0.1))
            .cornerRadius(16)
        }
    }
}

struct LoadMusicExtraInfo: View {
    @Environment(HMPTheme.self) private var theme
    let pendingCount: Int
    let musicWithExtraCount: Int
    let isProcessing: Bool
    let isPaused: Bool
    let currentMusicTitle: String
    let processedCount: Int
    let totalCount: Int
    let progress: Double
    let autoBatchProcess: Bool
    let onAutoBatchProcessChange: (Bool) -> Void
    let startAutoProcessExtraInfo: () -> Void
    let pauseProcess: () -> Void
    let resumeProcess: () -> Void
    let cancelProcess: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("音乐信息补全")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            VStack(spacing: 16) {
                // 自动后台处理开关
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("自动后台补全")
                            .font(.body)
                            .foregroundColor(theme.text)
                        Text("在后台自动补全音乐信息")
                            .font(.caption)
                            .foregroundColor(theme.text.opacity(0.6))
                    }
                    Spacer()
                    Toggle("", isOn: Binding(
                        get: { autoBatchProcess },
                        set: { onAutoBatchProcessChange($0) }
                    ))
                }
                
                Divider()
                
                if isProcessing {
                    processingView
                } else {
                    pendingView
                }
            }
            .padding(16)
            .background(theme.surfaceVariant.opacity(0.1))
            .cornerRadius(16)
        }
    }
    
    private var pendingView: some View {
        VStack(spacing: 16) {
            VStack(spacing: 4) {
                Text("待处理音乐: \(pendingCount)")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(theme.text)
                Text("已补全音乐: \(musicWithExtraCount)")
                    .font(.body)
                    .foregroundColor(theme.text.opacity(0.7))
            }
            
            Button {
                startAutoProcessExtraInfo()
            } label: {
                Text("开始批量补全")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .tint(theme.primary)
        }
    }
    
    private var processingView: some View {
        VStack(spacing: 16) {
            Text("正在处理: \(currentMusicTitle)")
                .font(.body)
                .foregroundColor(theme.text)
                .lineLimit(1)
            
            ProgressView(value: progress)
            
            Text("\(processedCount) / \(totalCount)")
                .font(.caption)
                .foregroundColor(theme.text.opacity(0.7))
            
            if isPaused {
                Text("已暂停")
                    .font(.body)
                    .foregroundColor(theme.primary)
            }
            
            HStack(spacing: 16) {
                if isPaused {
                    Button {
                        resumeProcess()
                    } label: {
                        Text("继续")
                            .frame(width: 100)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(theme.primary)
                } else {
                    Button {
                        pauseProcess()
                    } label: {
                        Text("暂停")
                            .frame(width: 100)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(theme.primary)
                }
                
                Button {
                    cancelProcess()
                } label: {
                    Text("取消")
                        .frame(width: 100)
                }
                .buttonStyle(.borderedProminent)
                .tint(.red)
            }
        }
    }
}

struct DailyRefreshSettings: View {
    @Environment(HMPTheme.self) private var theme
    @Binding var refreshMode: String
    @Binding var refreshHours: Int
    @Binding var startupCount: Int
    @State private var showModePicker: Bool = false
    
    private let refreshModes = [
        ("time", "按时间间隔刷新"),
        ("startup", "按启动次数刷新"),
        ("smart", "智能刷新")
    ]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("每日推荐刷新策略")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            VStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("选择刷新策略")
                        .font(.body)
                        .foregroundColor(theme.text)
                    
                    Button {
                        showModePicker = true
                    } label: {
                        HStack {
                            Text(refreshModes.first { $0.0 == refreshMode }?.1 ?? "按时间间隔刷新")
                                .foregroundColor(theme.text)
                            Spacer()
                            Image(systemName: "chevron.down")
                                .foregroundColor(theme.text.opacity(0.6))
                        }
                        .padding()
                        .background(theme.surfaceVariant.opacity(0.3))
                        .cornerRadius(12)
                    }
                }
                
                switch refreshMode {
                case "time":
                    timeModeSettings
                case "startup":
                    startupModeSettings
                case "smart":
                    smartModeSettings
                default:
                    EmptyView()
                }
            }
            .padding(16)
            .background(theme.surfaceVariant.opacity(0.1))
            .cornerRadius(16)
        }
        .confirmationDialog("选择刷新策略", isPresented: $showModePicker, titleVisibility: .visible) {
            Button("按时间间隔刷新") { refreshMode = "time" }
            Button("按启动次数刷新") { refreshMode = "startup" }
            Button("智能刷新") { refreshMode = "smart" }
            Button("取消", role: .cancel) { }
        }
    }
    
    private var timeModeSettings: some View {
        VStack(alignment: .leading, spacing: 8) {
            VStack(alignment: .leading, spacing: 8) {
                Text("刷新间隔（小时）")
                    .font(.subheadline)
                    .foregroundColor(theme.text)
                
                TextField("12", value: $refreshHours, format: .number)
                    .padding()
                    .background(theme.surfaceVariant.opacity(0.3))
                    .cornerRadius(12)
                    .keyboardType(.numberPad)
            }
            
            Text("当前设置: 每 \(refreshHours) 小时刷新一次")
                .font(.caption)
                .foregroundColor(theme.text.opacity(0.6))
        }
    }
    
    private var startupModeSettings: some View {
        VStack(alignment: .leading, spacing: 8) {
            VStack(alignment: .leading, spacing: 8) {
                Text("启动次数")
                    .font(.subheadline)
                    .foregroundColor(theme.text)
                
                TextField("5", value: $startupCount, format: .number)
                    .padding()
                    .background(theme.surfaceVariant.opacity(0.3))
                    .cornerRadius(12)
                    .keyboardType(.numberPad)
            }
            
            Text("当前设置: 每启动 \(startupCount) 次刷新一次")
                .font(.caption)
                .foregroundColor(theme.text.opacity(0.6))
        }
    }
    
    private var smartModeSettings: some View {
        Text("智能模式会根据你的使用习惯自动选择刷新时机。")
            .font(.body)
            .foregroundColor(theme.text.opacity(0.8))
    }
}

struct ProviderPicker: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var selectedProvider: AiProviderType
    
    var body: some View {
        NavigationStack {
            List(AiProviderType.allCases, id: \.self) { provider in
                Button {
                    selectedProvider = provider
                    dismiss()
                } label: {
                    HStack {
                        Text(provider.displayName)
                            .foregroundColor(.primary)
                        Spacer()
                        if provider == selectedProvider {
                            Image(systemName: "checkmark")
                                .foregroundColor(.blue)
                        }
                    }
                }
            }
            .navigationTitle("选择服务商")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") {
                        dismiss()
                    }
                }
            }
        }
    }
}

// 简单的扩展
extension AiProviderType: CaseIterable {
    public static var allCases: [AiProviderType] {
        [.openai, .deepseek, .claude, .qwen, .ernie]
    }
}

extension AiProviderType: Identifiable {
    public var id: String { name }
}

#Preview {
    AIScreen()
}
