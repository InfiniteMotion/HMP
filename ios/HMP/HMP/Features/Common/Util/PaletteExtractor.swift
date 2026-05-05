import SwiftUI
import CoreImage

/// 从专辑封面提取调色板颜色 - 对应 Android Palette API
class PaletteColors: NSObject {
    var dominantColor: Color = Color(red: 0.07, green: 0.07, blue: 0.07)
    var primaryColor: Color = Color(red: 0.12, green: 0.12, blue: 0.12)
    var vibrantColor: Color = Color(red: 0.17, green: 0.17, blue: 0.17)
    var darkVibrantColor: Color = Color(red: 0.06, green: 0.06, blue: 0.06)
    var lightVibrantColor: Color = Color(red: 0.5, green: 0.5, blue: 0.5)
    var mutedColor: Color = Color(red: 0.13, green: 0.13, blue: 0.13)
    var darkMutedColor: Color = Color(red: 0.07, green: 0.07, blue: 0.07)
    var lightMutedColor: Color = Color(red: 0.4, green: 0.4, blue: 0.4)
    var accentColor: Color = Color(red: 0.27, green: 0.27, blue: 0.27)
    
    override init() {
        super.init()
    }
    
    init(
        dominantColor: Color,
        primaryColor: Color,
        vibrantColor: Color,
        darkVibrantColor: Color,
        lightVibrantColor: Color,
        mutedColor: Color,
        darkMutedColor: Color,
        lightMutedColor: Color,
        accentColor: Color
    ) {
        self.dominantColor = dominantColor
        self.primaryColor = primaryColor
        self.vibrantColor = vibrantColor
        self.darkVibrantColor = darkVibrantColor
        self.lightVibrantColor = lightVibrantColor
        self.mutedColor = mutedColor
        self.darkMutedColor = darkMutedColor
        self.lightMutedColor = lightMutedColor
        self.accentColor = accentColor
        super.init()
    }
}

/// iOS 端取色引擎 - 用 Core Image 替代 Android Palette API
final class PaletteExtractor {
    static let shared = PaletteExtractor()

    private let context = CIContext(options: [.useSoftwareRenderer: false])
    private let cache = NSCache<NSString, PaletteColors>()

    private init() {}

    func extract(from image: UIImage?) -> PaletteColors? {
        guard let image = image else { return nil }

        let key = "\(image.hash)" as NSString
        if let cached = cache.object(forKey: key) {
            return cached
        }

        guard let ciImage = CIImage(image: image) else { return nil }

        // Downsample to 50x50 for performance
        let scale = min(50.0 / ciImage.extent.width, 50.0 / ciImage.extent.height, 1.0)
        let scaled = ciImage.transformed(by: CGAffineTransform(scaleX: scale, y: scale))

        // Extract dominant color using CIAreaAverage
        let filter = CIFilter(name: "CIAreaAverage", parameters: [
            kCIInputImageKey: scaled,
            kCIInputExtentKey: CIVector(cgRect: scaled.extent)
        ])!

        guard let output = filter.outputImage,
              let bitmap = context.createCGImage(output, from: output.extent) else { return nil }

        let data = bitmap.dataProvider!.data
        let ptr = CFDataGetBytePtr(data)!

        let r = Double(ptr[0]) / 255.0
        let g = Double(ptr[1]) / 255.0
        let b = Double(ptr[2]) / 255.0

        let dominant = Color(red: r, green: g, blue: b)
        let (h, s, v) = rgbToHsv(r: r, g: g, b: b)

        // Generate palette via HSV splitting
        let vibrant = Color(hue: h, saturation: min(s * 1.3, 1.0), brightness: min(v * 1.2, 1.0))
        let darkVibrant = Color(hue: h, saturation: min(s * 1.2, 1.0), brightness: v * 0.6)
        let lightVibrant = Color(hue: h, saturation: min(s * 0.8, 1.0), brightness: min(v * 1.4, 1.0))
        let muted = Color(hue: h, saturation: s * 0.5, brightness: v * 0.9)
        let darkMuted = Color(hue: h, saturation: s * 0.4, brightness: v * 0.5)
        let lightMuted = Color(hue: h, saturation: s * 0.6, brightness: min(v * 1.3, 1.0))

        let colors = PaletteColors(
            dominantColor: dominant,
            primaryColor: vibrant,
            vibrantColor: vibrant,
            darkVibrantColor: darkVibrant,
            lightVibrantColor: lightVibrant,
            mutedColor: muted,
            darkMutedColor: darkMuted,
            lightMutedColor: lightMuted,
            accentColor: lightVibrant
        )

        cache.setObject(colors, forKey: key)
        return colors
    }

    /// Load image from file path and extract palette
    func extractFromPath(_ path: String?) -> PaletteColors? {
        guard let path = path, !path.isEmpty else { return nil }
        guard let image = UIImage(contentsOfFile: path) else { return nil }
        return extract(from: image)
    }

    // MARK: - HSV Helpers

    private func rgbToHsv(r: Double, g: Double, b: Double) -> (h: Double, s: Double, v: Double) {
        let maxC = max(r, g, b)
        let minC = min(r, g, b)
        let v = maxC
        let delta = maxC - minC

        guard delta > 0.001 else { return (0, 0, v) }

        let s = delta / maxC
        var h: Double

        if maxC == r {
            h = (g - b) / delta + (g < b ? 6 : 0)
        } else if maxC == g {
            h = (b - r) / delta + 2
        } else {
            h = (r - g) / delta + 4
        }

        return (h / 6.0, s, v)
    }
}

// MARK: - Color HSV Extension

extension Color {
    func shiftHue(_ degrees: Double) -> Color {
        let uiColor = UIColor(self)
        var h: CGFloat = 0, s: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        uiColor.getHue(&h, saturation: &s, brightness: &b, alpha: &a)
        return Color(hue: (h + degrees / 360.0).truncatingRemainder(dividingBy: 1.0), saturation: s, brightness: b)
    }
}
