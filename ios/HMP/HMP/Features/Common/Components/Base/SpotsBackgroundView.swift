import SwiftUI

/// 沉浸光斑背景 - 对应 Android SpotsBackground.kt
struct SpotsBackgroundView: View {
    let paletteColors: PaletteColors
    let isDark: Bool

    private let animator = BackgroundAnimator.shared

    var body: some View {
        GeometryReader { geometry in
            let width = geometry.size.width
            let height = geometry.size.height
            let maxDim = max(width, height)
            let minDim = min(width, height)
            let centerX = width / 2
            let centerY = height / 2

            let baseBackgroundColor = isDark ? Color(red: 0.07, green: 0.07, blue: 0.07) : Color.white

            ZStack {
                baseBackgroundColor

                let rawPrimary = isDark ? paletteColors.dominantColor : paletteColors.lightVibrantColor
                let rawSecondary = isDark ? paletteColors.darkVibrantColor : paletteColors.vibrantColor
                let rawTertiary = isDark ? paletteColors.darkMutedColor : paletteColors.lightMutedColor
                let quaternaryColor = rawPrimary.shiftHue(180)

                let alphaPrimary = isDark ? 0.40 : 0.30
                let alphaSecondary = isDark ? 0.35 : 0.25
                let alphaTertiary = isDark ? 0.30 : 0.20
                let alphaQuaternary = isDark ? 0.25 : 0.15

                let angle1 = animator.spotsRotation1 * (.pi / 180)
                let offset1X = centerX + cos(angle1) * CGFloat(minDim * 0.35)
                let offset1Y = centerY + sin(angle1) * CGFloat(minDim * 0.35)

                Circle()
                    .fill(
                        RadialGradient(
                            gradient: Gradient(colors: [
                                rawPrimary.opacity(alphaPrimary),
                                rawPrimary.opacity(alphaPrimary * 0.6),
                                Color.clear
                            ]),
                            center: .center, startRadius: 0, endRadius: maxDim * 1.0 * animator.spotsScale
                        )
                    )
                    .frame(width: maxDim * 2.0 * animator.spotsScale, height: maxDim * 2.0 * animator.spotsScale)
                    .position(x: offset1X, y: offset1Y)

                let angle2 = animator.spotsRotation2 * (.pi / 180)
                let offset2X = centerX + cos(angle2) * CGFloat(minDim * 0.45)
                let offset2Y = centerY + sin(angle2) * CGFloat(minDim * 0.45)

                Circle()
                    .fill(
                        RadialGradient(
                            gradient: Gradient(colors: [
                                rawSecondary.opacity(alphaSecondary),
                                rawSecondary.opacity(alphaSecondary * 0.6),
                                Color.clear
                            ]),
                            center: .center, startRadius: 0, endRadius: maxDim * 0.9 * animator.spotsScale
                        )
                    )
                    .frame(width: maxDim * 1.8 * animator.spotsScale, height: maxDim * 1.8 * animator.spotsScale)
                    .position(x: offset2X, y: offset2Y)

                Circle()
                    .fill(
                        RadialGradient(
                            gradient: Gradient(colors: [
                                rawTertiary.opacity(alphaTertiary),
                                Color.clear
                            ]),
                            center: .center, startRadius: 0, endRadius: maxDim * 1.1 * (2.3 - animator.spotsScale)
                        )
                    )
                    .frame(width: maxDim * 2.2 * (2.3 - animator.spotsScale), height: maxDim * 2.2 * (2.3 - animator.spotsScale))
                    .position(x: centerX, y: centerY)

                let angle4 = animator.spotsRotation1 * 1.5 * (.pi / 180)
                let offset4X = centerX + cos(angle4 + .pi) * CGFloat(minDim * 0.5)
                let offset4Y = centerY + sin(angle4 + .pi) * CGFloat(minDim * 0.5)

                Circle()
                    .fill(
                        RadialGradient(
                            gradient: Gradient(colors: [
                                quaternaryColor.opacity(alphaQuaternary),
                                quaternaryColor.opacity(alphaQuaternary * 0.5),
                                Color.clear
                            ]),
                            center: .center, startRadius: 0, endRadius: maxDim * 0.6 * animator.spotsScale
                        )
                    )
                    .frame(width: maxDim * 1.2 * animator.spotsScale, height: maxDim * 1.2 * animator.spotsScale)
                    .position(x: offset4X, y: offset4Y)
            }
        }
        .clipped()
    }
}
