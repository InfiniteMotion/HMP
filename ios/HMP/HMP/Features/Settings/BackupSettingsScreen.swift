import SwiftUI
import shared
import UniformTypeIdentifiers

/// 备份与恢复 — 对应 Android BackupSettingsScreen.kt
struct BackupSettingsScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    @State private var vm = BackupViewModel()
    @State private var showImporter = false
    @State private var showShareSheet = false
    @State private var shareURL: URL?

    var body: some View {
        List {
            Section("操作") {
                Button {
                    Task { await vm.exportBackup() }
                } label: {
                    HStack {
                        Image(systemName: "square.and.arrow.up"); Text("导出备份")
                        Spacer(); if vm.isExporting { ProgressView() }
                    }
                }
                .disabled(vm.isExporting)

                Button {
                    showImporter = true
                } label: {
                    HStack {
                        Image(systemName: "square.and.arrow.down"); Text("导入备份")
                        Spacer(); if vm.isImporting { ProgressView() }
                    }
                }
                .disabled(vm.isImporting)
            }

            if !vm.backupFiles.isEmpty {
                Section("本地备份 (\(vm.backupFiles.count))") {
                    ForEach(vm.backupFiles.indices, id: \.self) { idx in
                        let file = vm.backupFiles[idx]
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(backupDisplayName(file)).font(TypographyTokens.bodyMedium)
                                Text(file).font(.caption).foregroundColor(theme.text.opacity(0.4)).lineLimit(1)
                            }
                            Spacer()
                            Button("恢复") { Task { await vm.importBackup(url: URL(fileURLWithPath: file)) } }
                                .buttonStyle(.bordered).controlSize(.small)
                            Button("删除") { Task { await vm.deleteBackup(path: file) } }
                                .buttonStyle(.bordered).tint(.red).controlSize(.small)
                        }
                    }
                }
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("备份与恢复")
        .navigationBarTitleDisplayMode(.inline)
        .fileImporter(isPresented: $showImporter, allowedContentTypes: [.json]) { result in
            if case .success(let url) = result { Task { await vm.importBackup(url: url) } }
        }
        .sheet(isPresented: $showShareSheet) {
            if let url = shareURL { ActivityViewController(activityItems: [url]) }
        }
        .alert(vm.alertMessage ?? "", isPresented: .constant(vm.alertMessage != nil)) {
            Button("确定") { vm.alertMessage = nil }
        }
        .onChange(of: vm.exportedFileURL) { _, url in
            if url != nil { shareURL = url; showShareSheet = true }
        }
    }

    private func backupDisplayName(_ path: String) -> String {
        URL(fileURLWithPath: path).lastPathComponent
            .replacingOccurrences(of: "hearable-backup-v", with: "")
            .replacingOccurrences(of: ".json", with: "")
    }
}

// MARK: - BackupViewModel

@Observable
class BackupViewModel {
    var backupFiles: [String] = []
    var isExporting = false
    var isImporting = false
    var exportedFileURL: URL?
    var alertMessage: String?

    init() { loadBackups() }

    func loadBackups() {
        Task {
            do {
                let files = try await KoinHelperKt.getBackupFiles()
                await MainActor.run { self.backupFiles = files }
            } catch {
                await MainActor.run { self.alertMessage = error.localizedDescription }
            }
        }
    }

    func exportBackup() async {
        isExporting = true
        do {
            let path = try await KoinHelperKt.exportBackup()
            await MainActor.run {
                isExporting = false
                if path.isEmpty { self.alertMessage = "导出失败" }
                else { self.exportedFileURL = URL(fileURLWithPath: path) }
            }
        } catch {
            await MainActor.run { isExporting = false; alertMessage = error.localizedDescription }
        }
    }

    func importBackup(url: URL) async {
        isImporting = true
        do {
            try await KoinHelperKt.importBackup(filePath: url.path)
            await MainActor.run {
                isImporting = false; alertMessage = "恢复成功"; loadBackups()
            }
        } catch {
            await MainActor.run { isImporting = false; alertMessage = error.localizedDescription }
        }
    }

    func deleteBackup(path: String) async {
        do {
            try await KoinHelperKt.deleteBackupFile(filePath: path)
            await MainActor.run { loadBackups() }
        } catch {
            await MainActor.run { alertMessage = error.localizedDescription }
        }
    }
}

private struct ActivityViewController: UIViewControllerRepresentable {
    let activityItems: [Any]
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
