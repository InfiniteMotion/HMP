import SwiftUI

/// 创建/编辑播放列表对话框 - 对应 Android CreatePlaylistDialog.kt
struct CreatePlaylistDialog: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    let onCreate: (String) -> Void
    var isEditing: Bool = false
    var initialName: String = ""

    @State private var name = ""
    @State private var nameError: String? = nil

    private let maxNameLength = 30

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("播放列表名称", text: $name)
                        .textFieldStyle(.roundedBorder)
                        .onChange(of: name) { _, newValue in
                            if newValue.count > maxNameLength {
                                name = String(newValue.prefix(maxNameLength))
                            }
                            nameError = nil
                        }

                    if let error = nameError {
                        Text(error)
                            .font(TypographyTokens.bodySmall)
                            .foregroundColor(.red)
                    }

                    Text("\(name.count)/\(maxNameLength)")
                        .font(TypographyTokens.bodySmall)
                        .foregroundColor(theme.text.opacity(0.4))
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
            }
            .navigationTitle(isEditing ? "编辑播放列表" : "创建播放列表")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(isEditing ? "保存" : "创建") {
                        let trimmed = name.trimmingCharacters(in: .whitespaces)
                        guard !trimmed.isEmpty else {
                            nameError = "名称不能为空"
                            return
                        }
                        HapticManager.shared.confirm()
                        onCreate(trimmed)
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .presentationDetents([.medium])
        .onAppear {
            if isEditing {
                name = initialName
            }
        }
    }
}
