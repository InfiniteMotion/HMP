import SwiftUI

// MARK: - 全局动画状态（独立于视图生命周期，避免重建跳变）

@Observable
class BackgroundAnimator {
    static let shared = BackgroundAnimator()

    var fluidOffsetX: CGFloat = 0
    var fluidRotation1: CGFloat = 0
    var fluidOffsetY: CGFloat = 0
    var fluidRotation2: CGFloat = 0
    var fluidBaseRotation: CGFloat = 0
    var fluidScale: CGFloat = 3.2
    var spotsRotation1: CGFloat = 0
    var spotsRotation2: CGFloat = 0
    var spotsScale: CGFloat = 1.15

    private var timer: Timer?
    private(set) var isRunning = false

    func start() {
        guard !isRunning else { return }
        isRunning = true
        let fps: TimeInterval = 1.0 / 60.0
        let t = Timer(timeInterval: fps, repeats: true) { [weak self] _ in
            guard let self else { return }
            let now = CACurrentMediaTime()
            self.fluidOffsetX = CGFloat(sin(now * 2 * .pi / 20.0) * 150)
            self.fluidRotation1 = CGFloat((now / 60.0).truncatingRemainder(dividingBy: 1.0) * 360)
            self.fluidOffsetY = CGFloat(sin(now * 2 * .pi / 25.0 + 1.5) * 120)
            self.fluidRotation2 = CGFloat((1.0 - (now / 90.0).truncatingRemainder(dividingBy: 1.0)) * 360)
            self.fluidBaseRotation = CGFloat((1.0 - (now / 120.0).truncatingRemainder(dividingBy: 1.0)) * 360)
            self.fluidScale = CGFloat(3.2 + sin(now * 2 * .pi / 30.0) * 0.2)
            self.spotsRotation1 = CGFloat((now / 60.0).truncatingRemainder(dividingBy: 1.0) * 360)
            self.spotsRotation2 = CGFloat((1.0 - (now / 45.0).truncatingRemainder(dividingBy: 1.0)) * 360)
            self.spotsScale = CGFloat(1.15 + sin(now * 2 * .pi / 20.0) * 0.15)
        }
        timer = t
        RunLoop.current.add(t, forMode: .common)
    }

    func stop() {
        timer?.invalidate()
        timer = nil
        isRunning = false
    }
}

// MARK: - BackgroundStyle

/// 动态背景风格枚举 - 对应 Android BackgroundStyle
enum BackgroundStyle {
    case fluid      // 流体极光
    case spots      // 沉浸光斑
    case blur       // 复古模糊
}

/// 动态背景主入口组件 - 对应 Android DynamicBackground.kt
struct DynamicBackground: View {
    let albumArtUri: String?
    let musicPath: String?
    let paletteColors: PaletteColors
    let isDark: Bool
    let style: BackgroundStyle
    
    init(
        albumArtUri: String?,
        musicPath: String? = nil,
        paletteColors: PaletteColors,
        isDark: Bool = true,
        style: BackgroundStyle = .fluid
    ) {
        self.albumArtUri = albumArtUri
        self.musicPath = musicPath
        self.paletteColors = paletteColors
        self.isDark = isDark
        self.style = style
    }
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                switch style {
                case .fluid:
                    FluidBackgroundView(
                        albumArtUri: albumArtUri,
                        musicPath: musicPath,
                        isDark: isDark
                    )
                case .spots:
                    SpotsBackgroundView(
                        paletteColors: paletteColors,
                        isDark: isDark
                    )
                case .blur:
                    BlurBackgroundView(
                        albumArtUri: albumArtUri,
                        musicPath: musicPath,
                        isDark: isDark
                    )
                }
                
                if isDark {
                    LinearGradient(
                        gradient: Gradient(
                            colors: [
                                Color.black.opacity(0.3),
                                Color.clear,
                                Color.clear,
                                Color.black.opacity(0.6)
                            ]
                        ),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                    .frame(width: geometry.size.width, height: geometry.size.height)
                } else {
                    Color.white.opacity(0.1)
                        .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
        }
        .onAppear { BackgroundAnimator.shared.start() }
        .onDisappear { BackgroundAnimator.shared.stop() }
    }
}
