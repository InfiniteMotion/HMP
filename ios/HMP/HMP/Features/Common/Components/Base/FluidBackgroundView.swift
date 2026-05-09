import SwiftUI
import CoreImage

/// 流体极光背景 - 对应 Android FluidBackground.kt
struct FluidBackgroundView: View {
    let albumArtUri: String?
    let musicPath: String?
    let isDark: Bool

    @State private var resolvedImage: UIImage?
    private let animator = BackgroundAnimator.shared

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                let baseColor = isDark ? Color(red: 0.07, green: 0.07, blue: 0.07) : Color(red: 0.96, green: 0.96, blue: 0.96)
                baseColor
                    .frame(width: geometry.size.width, height: geometry.size.height)

                if let resolvedImage {
                    let imageAlpha = isDark ? 0.6 : 0.4

                    Image(uiImage: resolvedImage)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: geometry.size.width, height: geometry.size.height)
                        .scaleEffect(animator.fluidScale)
                        .offset(x: animator.fluidOffsetX, y: 0)
                        .rotationEffect(.degrees(animator.fluidBaseRotation))
                        .opacity(imageAlpha)
                        .blur(radius: 40)

                    Image(uiImage: resolvedImage)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: geometry.size.width, height: geometry.size.height)
                        .scaleEffect(animator.fluidScale * 1.1)
                        .offset(x: 0, y: animator.fluidOffsetY)
                        .rotationEffect(.degrees(animator.fluidRotation1))
                        .opacity(imageAlpha * 0.7)
                        .blur(radius: 25)
                } else {
                    placeholderView
                        .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
        }
        .clipped()
        .task(id: albumArtUri) { resolveImage() }
    }

    private func resolveImage() {
        Task.detached(priority: .userInitiated) {
            var loadedImage: UIImage? = nil

            if let uri = albumArtUri, !uri.isEmpty {
                let fileManager = FileManager.default
                if fileManager.fileExists(atPath: uri) {
                    if let image = CoverCache.shared.get(path: uri) {
                        loadedImage = image
                    } else if let directImage = UIImage(contentsOfFile: uri) {
                        loadedImage = directImage
                    }
                }
            }

            if loadedImage == nil, let path = musicPath, !path.isEmpty {
                if let image = CoverCache.shared.getOrExtract(musicPath: path) {
                    loadedImage = image
                }
            }

            if let image = loadedImage {
                let filteredImage = await applyColorFilterAsync(image: image, isDark: isDark)
                await MainActor.run { resolvedImage = filteredImage }
            }
        }
    }

    private func applyColorFilterAsync(image: UIImage, isDark: Bool) async -> UIImage? {
        guard let ciImage = CIImage(image: image) else { return image }

        let context = CIContext(options: [
            .useSoftwareRenderer: false,
            .priorityRequestLow: true
        ])

        let saturationFilter = CIFilter(name: "CIColorControls")!
        saturationFilter.setValue(ciImage, forKey: kCIInputImageKey)
        saturationFilter.setValue(1.6, forKey: kCIInputSaturationKey)

        var outputImage = saturationFilter.outputImage

        if !isDark {
            let contrastFilter = CIFilter(name: "CIColorControls")!
            contrastFilter.setValue(outputImage, forKey: kCIInputImageKey)
            contrastFilter.setValue(1.2, forKey: kCIInputContrastKey)
            outputImage = contrastFilter.outputImage
        }

        guard let finalCIImage = outputImage else { return image }
        guard let cgImage = context.createCGImage(finalCIImage, from: finalCIImage.extent) else { return image }

        return UIImage(cgImage: cgImage)
    }

    private var placeholderView: some View {
        (isDark ? Color(red: 0.07, green: 0.07, blue: 0.07) : Color(red: 0.96, green: 0.96, blue: 0.96))
    }
}
