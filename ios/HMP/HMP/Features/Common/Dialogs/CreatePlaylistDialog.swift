import SwiftUI

/// 创建播放列表对话框 - 对应 Android CreatePlaylistDialog.kt
struct CreatePlaylistDialog: View {
    @Environment(\.dismiss) private var dismiss

    let onCreate: (String) -> Void
    @State private var name = ""

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("播放列表名称", text: $name)
                        .textFieldStyle(.roundedBorder)
                }
            }
            .navigationTitle("创建播放列表")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("创建") {
                        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                        HapticManager.shared.confirm()
                        onCreate(name.trimmingCharacters(in: .whitespaces))
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .presentationDetents([.medium])
    }
}
