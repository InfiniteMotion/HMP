import SwiftUI
import shared

struct BackupSettingsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var localBackups: [String] = []
    @State private var isExporting: Bool = false
    @State private var isRestoring: Bool = false
    @State private var showRestoreConfirm: Bool = false
    @State private var selectedBackupPath: String?
    @State private var showMessage: String?
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                ExportBackupSection(
                    isExporting: $isExporting,
                    onExport: exportBackup
                )
                
                ImportBackupSection(
                    isRestoring: $isRestoring,
                    onSelectFile: { showRestoreConfirm = true }
                )
                
                ManageBackupsSection(
                    backups: localBackups,
                    onRefresh: loadLocalBackups,
                    onRestore: { path in
                        selectedBackupPath = path
                        showRestoreConfirm = true
                    },
                    onDelete: deleteBackup
                )
            }
            .padding(24)
            .padding(.bottom, 64)
        }
        .onAppear {
            loadLocalBackups()
        }
        .alert("确认恢复", isPresented: $showRestoreConfirm) {
            Button("恢复", role: .destructive) {
                if let path = selectedBackupPath {
                    restoreBackup(path: path)
                }
            }
            Button("取消", role: .cancel) { }
        } message: {
            Text("恢复备份将覆盖当前数据,是否继续?")
        }
        .alert("提示", isPresented: .init(
            get: { showMessage != nil },
            set: { if !$0 { showMessage = nil } }
        )) {
            Button("确定", role: .cancel) { }
        } message: {
            Text(showMessage ?? "")
        }
    }
    
    private func loadLocalBackups() {
        // 模拟加载本地备份
        localBackups = []
    }
    
    private func exportBackup() {
        isExporting = true
        // 模拟导出
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            isExporting = false
            showMessage = "备份导出成功"
        }
    }
    
    private func restoreBackup(path: String) {
        isRestoring = true
        // 模拟恢复
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            isRestoring = false
            showMessage = "恢复成功"
        }
    }
    
    private func deleteBackup(path: String) {
        localBackups.removeAll { $0 == path }
        showMessage = "备份已删除"
    }
}

struct ExportBackupSection: View {
    @Environment(HMPTheme.self) private var theme
    @Binding var isExporting: Bool
    let onExport: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("导出备份")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            VStack(alignment: .leading, spacing: 12) {
                Text("将你的播放列表、设置和偏好数据导出为备份文件,方便换设备时快速恢复。")
                    .font(.body)
                    .foregroundColor(theme.text.opacity(0.7))
                    .lineSpacing(4)
                
                Button {
                    onExport()
                } label: {
                    HStack {
                        if isExporting {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Image(systemName: "square.and.arrow.up")
                        }
                        Text(isExporting ? "导出中..." : "导出备份")
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(theme.primary)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .disabled(isExporting)
            }
            .padding(16)
            .background(theme.surfaceVariant.opacity(0.1))
            .cornerRadius(16)
        }
    }
}

struct ImportBackupSection: View {
    @Environment(HMPTheme.self) private var theme
    @Binding var isRestoring: Bool
    let onSelectFile: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("恢复备份")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            VStack(alignment: .leading, spacing: 12) {
                Text("从之前导出的备份文件恢复播放列表、设置和偏好数据。")
                    .font(.body)
                    .foregroundColor(theme.text.opacity(0.7))
                    .lineSpacing(4)
                
                Button {
                    onSelectFile()
                } label: {
                    HStack {
                        if isRestoring {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Image(systemName: "square.and.arrow.down")
                        }
                        Text(isRestoring ? "恢复中..." : "选择备份文件")
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(theme.primary)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .disabled(isRestoring)
            }
            .padding(16)
            .background(theme.surfaceVariant.opacity(0.1))
            .cornerRadius(16)
        }
    }
}

struct ManageBackupsSection: View {
    @Environment(HMPTheme.self) private var theme
    let backups: [String]
    let onRefresh: () -> Void
    let onRestore: (String) -> Void
    let onDelete: (String) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("本地备份")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(theme.text)
                
                Spacer()
                
                Button {
                    onRefresh()
                } label: {
                    Image(systemName: "arrow.clockwise")
                        .foregroundColor(theme.primary)
                }
            }
            
            VStack(alignment: .leading, spacing: 12) {
                Text("管理本地存储的备份文件。")
                    .font(.body)
                    .foregroundColor(theme.text.opacity(0.7))
                    .lineSpacing(4)
                
                if backups.isEmpty {
                    Text("暂无本地备份")
                        .font(.body)
                        .foregroundColor(theme.text.opacity(0.5))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                } else {
                    ForEach(backups, id: \.self) { path in
                        BackupItem(
                            filePath: path,
                            onRestore: { onRestore(path) },
                            onDelete: { onDelete(path) }
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

struct BackupItem: View {
    @Environment(HMPTheme.self) private var theme
    let filePath: String
    let onRestore: () -> Void
    let onDelete: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text((filePath as NSString).lastPathComponent)
                    .font(.body)
                    .foregroundColor(theme.text)
                    .lineLimit(1)
                
                Text(formatDate(Date()))
                    .font(.caption)
                    .foregroundColor(theme.text.opacity(0.6))
            }
            
            Spacer()
            
            Button {
                onRestore()
            } label: {
                Image(systemName: "arrow.down.circle")
                    .foregroundColor(theme.primary)
            }
            
            Button {
                onDelete()
            } label: {
                Image(systemName: "trash")
                    .foregroundColor(.red)
            }
        }
        .padding(12)
        .background(theme.surfaceVariant.opacity(0.3))
        .cornerRadius(12)
    }
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy/MM/dd HH:mm"
        return formatter.string(from: date)
    }
}

#Preview {
    BackupSettingsScreen()
}
