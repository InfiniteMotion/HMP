import SwiftUI

/// 定时关闭对话框 - 对应 Android TimerDialog.kt
struct TimerDialog: View {
    @Environment(\.dismiss) private var dismiss

    let onSet: (TimeInterval) -> Void
    @State private var selectedMinutes: Double = 30

    private static let options: [Double] = [5, 10, 15, 30, 45, 60, 90, 120]

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("定时关闭")) {
                    Picker("时长", selection: $selectedMinutes) {
                        ForEach(Self.options, id: \.self) { minutes in
                            Text("\(Int(minutes)) 分钟")
                                .tag(minutes)
                        }
                    }
                    .pickerStyle(.segmented)
                }

                Section {
                    Button {
                        HapticManager.shared.confirm()
                        onSet(selectedMinutes * 60)
                        dismiss()
                    } label: {
                        Text("设置 \(Int(selectedMinutes)) 分钟后关闭")
                            .frame(maxWidth: .infinity)
                    }
                }
            }
            .navigationTitle("定时关闭")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
