import SwiftUI
import shared

/// 多选歌曲 Picker Dialog - 对应 Android MusicPickerDialog.kt
struct MusicPickerDialog: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    let allMusic: [MusicInfo_]
    let selectedIds: Set<Int64>
    let title: String
    let onConfirm: (Set<Int64>) -> Void

    @State private var currentSelectedIds: Set<Int64>

    init(allMusic: [MusicInfo_], selectedIds: Set<Int64>, title: String, onConfirm: @escaping (Set<Int64>) -> Void) {
        self.allMusic = allMusic
        self.selectedIds = selectedIds
        self.title = title
        self.onConfirm = onConfirm
        _currentSelectedIds = State(initialValue: selectedIds)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Selection count
                HStack {
                    Text("已选择 \(currentSelectedIds.count) 首")
                        .font(TypographyTokens.bodySmall)
                        .foregroundColor(theme.text.opacity(0.6))
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)

                // Music list
                List {
                    ForEach(allMusic, id: \.music.id) { info in
                        Button {
                            HapticManager.shared.click()
                            toggleSelection(info.music.id)
                        } label: {
                            HStack(spacing: 12) {
                                Image(systemName: currentSelectedIds.contains(info.music.id) ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(currentSelectedIds.contains(info.music.id) ? theme.primary : theme.text.opacity(0.3))
                                    .font(.system(size: 20))

                                VStack(alignment: .leading, spacing: 2) {
                                    Text(info.music.title)
                                        .font(TypographyTokens.bodyMedium)
                                        .foregroundColor(theme.text)
                                        .lineLimit(1)
                                    Text(info.music.artist)
                                        .font(TypographyTokens.bodySmall)
                                        .foregroundColor(theme.text.opacity(0.6))
                                        .lineLimit(1)
                                }
                            }
                            .padding(.vertical, 4)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .listStyle(.plain)
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") {
                        HapticManager.shared.click()
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("确认") {
                        HapticManager.shared.confirm()
                        onConfirm(currentSelectedIds)
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func toggleSelection(_ id: Int64) {
        if currentSelectedIds.contains(id) {
            currentSelectedIds.remove(id)
        } else {
            currentSelectedIds.insert(id)
        }
    }
}
