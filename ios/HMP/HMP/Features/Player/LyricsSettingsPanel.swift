import SwiftUI
import shared

/// 歌词设置面板 - 对应 Android LyricsScreen.kt 中的 LyricsSettingsPanel
struct LyricsSettingsPanel: View {
    @Environment(HMPTheme.self) private var theme
    @ObservedObject var viewModel: LyricsSettingsViewModel

    var body: some View {
        VStack(spacing: 16) {
            // Display mode selector
            HStack(spacing: 0) {
                modeButton("原文", mode: .lang1)
                modeButton("译文", mode: .lang2)
                modeButton("双语", mode: .dual)
            }
            .background(theme.surface, in: RoundedRectangle(cornerRadius: 8))

            // Alignment selector
            HStack(spacing: 0) {
                alignButton("左对齐", alignment: .left, icon: "text.alignleft")
                alignButton("居中", alignment: .center, icon: "text.aligncenter")
                alignButton("右对齐", alignment: .right, icon: "text.alignright")
            }
            .background(theme.surface, in: RoundedRectangle(cornerRadius: 8))

            // Size controls
            VStack(spacing: 12) {
                if viewModel.displayMode == .lang1 || viewModel.displayMode == .dual {
                    sizeControl(
                        label: "原文大小",
                        value: $viewModel.originalTextSize,
                        range: 12...24,
                        onSave: { viewModel.saveOriginalTextSize($0) }
                    )
                }

                if viewModel.displayMode == .lang2 || viewModel.displayMode == .dual {
                    sizeControl(
                        label: "译文大小",
                        value: $viewModel.translatedTextSize,
                        range: 12...24,
                        onSave: { viewModel.saveTranslatedTextSize($0) }
                    )
                }

                sizeControl(
                    label: "当前行大小",
                    value: $viewModel.currentTimeTextSize,
                    range: 14...32,
                    onSave: { viewModel.saveCurrentTimeTextSize($0) }
                )

                sizeControl(
                    label: "行间距",
                    value: $viewModel.lineSpacing,
                    range: 2...20,
                    onSave: { viewModel.saveLineSpacing($0) }
                )
            }
        }
        .padding(16)
    }

    // MARK: - Mode Button

    private func modeButton(_ title: String, mode: DisplayMode) -> some View {
        Button {
            HapticManager.shared.click()
            viewModel.saveDisplayMode(mode)
        } label: {
            Text(title)
                .font(TypographyTokens.bodySmall)
                .foregroundColor(viewModel.displayMode == mode ? .white : theme.text)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(
                    viewModel.displayMode == mode ? theme.primary : Color.clear
                )
        }
        .buttonStyle(.plain)
    }

    // MARK: - Alignment Button

    private func alignButton(_ title: String, alignment: LyricsAlignment, icon: String) -> some View {
        Button {
            HapticManager.shared.click()
            viewModel.saveAlignment(alignment)
        } label: {
            VStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                Text(title)
                    .font(TypographyTokens.labelSmall)
            }
            .foregroundColor(viewModel.alignment == alignment ? .white : theme.text)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
            .background(
                viewModel.alignment == alignment ? theme.primary : Color.clear
            )
        }
        .buttonStyle(.plain)
    }

    // MARK: - Size Control

    private func sizeControl(label: String, value: Binding<Int>, range: ClosedRange<Int>, onSave: @escaping (Int) -> Void) -> some View {
        HStack {
            Text(label)
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text)

            Spacer()

            HStack(spacing: 12) {
                Button {
                    HapticManager.shared.click()
                    let newVal = max(range.lowerBound, value.wrappedValue - 1)
                    value.wrappedValue = newVal
                    onSave(newVal)
                } label: {
                    Image(systemName: "minus.circle")
                        .font(.system(size: 20))
                        .foregroundColor(theme.primary)
                }
                .buttonStyle(.plain)

                Text("\(value.wrappedValue)")
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text)
                    .frame(width: 30)

                Button {
                    HapticManager.shared.click()
                    let newVal = min(range.upperBound, value.wrappedValue + 1)
                    value.wrappedValue = newVal
                    onSave(newVal)
                } label: {
                    Image(systemName: "plus.circle")
                        .font(.system(size: 20))
                        .foregroundColor(theme.primary)
                }
                .buttonStyle(.plain)
            }
        }
    }
}
