import SwiftUI
import shared
import PhotosUI

/// 个人资料编辑 — 对应 Android ProfileSettingsScreen.kt
struct ProfileSettingsScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    @State private var settingsVM = SettingsViewModel()
    @State private var selectedPhoto: PhotosPickerItem?

    var body: some View {
        SubScreen(title: "个人资料") {
            List {
                Section("头像") {
                    HStack {
                        Spacer()
                        PhotosPicker(selection: $selectedPhoto, matching: .images) {
                            Avatar(text: settingsVM.userName, size: 96, imageUri: settingsVM.avatarUri)
                        }
                        Spacer()
                    }
                    .padding(.vertical, 8)
                    .onChange(of: selectedPhoto) { _, newItem in
                        Task {
                            if let data = try? await newItem?.loadTransferable(type: Data.self) {
                                let tmpDir = FileManager.default.temporaryDirectory
                                let fileURL = tmpDir.appendingPathComponent("avatar_\(UUID().uuidString).jpg")
                                try? data.write(to: fileURL)
                                await MainActor.run {
                                    settingsVM.saveAvatarUri(fileURL.path)
                                }
                            }
                        }
                    }
                }

                Section("用户名") {
                    TextField("输入用户名", text: Binding(
                        get: { settingsVM.userName },
                        set: { settingsVM.saveUserName($0) }
                    ))
                }
            }
            .listStyle(.insetGrouped)
        }
    }
}
