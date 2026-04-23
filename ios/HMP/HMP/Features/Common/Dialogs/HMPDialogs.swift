import SwiftUI

// MARK: - ConfirmDialog (对应 Android ConfirmDialog.kt)
struct ConfirmDialog: View {
    let title: String
    let message: String
    let confirmText: String
    let dismissText: String
    let onConfirm: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        ConfirmationDialog(
            title,
            titleVisibility: .visible,
            presenting: true
        ) {
            Button(confirmText, role: .destructive) {
                HapticManager.shared.click()
                onConfirm()
            }
            Button(dismissText, role: .cancel) {
                onDismiss()
            }
        } message: {
            Text(message)
        }
    }
}

// MARK: - InputDialog (对应 Android InputDialog.kt)
struct InputDialog: View {
    let title: String
    let hint: String
    let initialValue: String
    let confirmText: String
    let dismissText: String
    let onConfirm: (String) -> Void
    let onDismiss: () -> Void
    @State private var inputValue: String

    init(
        title: String,
        hint: String,
        initialValue: String = "",
        confirmText: String = "确定",
        dismissText: String = "取消",
        onConfirm: @escaping (String) -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.title = title
        self.hint = hint
        self.initialValue = initialValue
        self.confirmText = confirmText
        self.dismissText = dismissText
        self.onConfirm = onConfirm
        self.onDismiss = onDismiss
        self._inputValue = State(initialValue: initialValue)
    }

    var body: some View {
        Alert(
            title: Text(title),
            message: Text(hint),
            primaryButton: .default(Text(confirmText)) {
                HapticManager.shared.click()
                onConfirm(inputValue)
            },
            secondaryButton: .cancel(Text(dismissText)) {
                onDismiss()
            }
        )
        // Note: SwiftUI Alert 不支持 TextField, 如需输入框使用 sheet 包裹自定义视图
    }
}

// MARK: - 自定义输入对话框 (sheet 模式, 支持 TextField)
struct InputDialogSheet: View {
    @Environment(\.dismiss) private var dismiss
    let title: String
    let hint: String
    let initialValue: String
    let onConfirm: (String) -> Void

    @State private var text: String

    init(
        title: String,
        hint: String,
        initialValue: String = "",
        onConfirm: @escaping (String) -> Void
    ) {
        self.title = title
        self.hint = hint
        self.initialValue = initialValue
        self.onConfirm = onConfirm
        self._text = State(initialValue: initialValue)
    }

    var body: some View {
        NavigationStack {
            Form {
                TextField(hint, text: $text)
                    .textFieldStyle(.roundedBorder)
            }
            .navigationTitle(title)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("确定") {
                        HapticManager.shared.confirm()
                        onConfirm(text)
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }
}

// MARK: - ScrimDialog (通用对话框)
struct ScrimDialog<Content: View>: View {
    let isPresented: Binding<Bool>
    let title: String
    let content: Content

    init(
        isPresented: Binding<Bool>,
        title: String,
        @ViewBuilder content: () -> Content
    ) {
        self.isPresented = isPresented
        self.title = title
        self.content = content()
    }

    var body: some View {
        NavigationStack {
            content
                .navigationTitle(title)
        }
        .sheet(isPresented: isPresented) {
            // iOS 原生 sheet 自带 scrim，对应 Android ScrimDialog
        }
    }
}
