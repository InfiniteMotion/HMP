import SwiftUI

/// 胶囊标签 - 对应 Android Capsule.kt
struct CapsuleTag: View {
    @Environment(HMPTheme.self) private var theme
    let text: String
    let color: Color?

    init(_ text: String, color: Color? = nil) {
        self.text = text
        self.color = color
    }

    var body: some View {
        Text(text)
            .font(TypographyTokens.labelMedium)
            .foregroundColor(.white)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(color ?? theme.primary, in: RoundedRectangle(cornerRadius: 16))
    }
}

/// 标签胶囊组 - 对应 Android LabelsCapsule.kt
struct LabelsCapsule: View {
    @Environment(HMPTheme.self) private var theme
    let labels: [String]
    let extraInfo: [String]?

    init(labels: [String], extraInfo: [String]? = nil) {
        self.labels = labels
        self.extraInfo = extraInfo
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // 额外信息
            if let extraInfo {
                ForEach(extraInfo, id: \.self) { info in
                    CapsuleTag(info, color: theme.tertiary)
                }
            }

            // 标签组
            if !labels.isEmpty {
                LazyVGrid(
                    columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 4),
                    spacing: 8
                ) {
                    ForEach(labels, id: \.self) { label in
                        CapsuleTag(label)
                            .font(TypographyTokens.labelSmall)
                    }
                }
            }
        }
        .padding(16)
    }
}
