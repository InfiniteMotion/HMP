import SwiftUI

/// Tab 页面指示器 - 对应 Android TabPageIndicator.kt
/// 顶部横条指示器，当前页高亮+主色，非当前页半透明
struct TabPageIndicator: View {
    let currentPage: Int
    let totalPages: Int
    var activeColor: Color = .white
    var inactiveColor: Color = .white.opacity(0.3)

    var body: some View {
        HStack(spacing: 12) {
            ForEach(0..<totalPages, id: \.self) { index in
                let isSelected = index == currentPage
                RoundedRectangle(cornerRadius: 1.5)
                    .fill(isSelected ? activeColor : inactiveColor)
                    .frame(width: 32, height: isSelected ? 3 : 2)
                    .animation(.easeInOut(duration: 0.2), value: currentPage)
            }
        }
        .padding(.top, 8)
    }
}
