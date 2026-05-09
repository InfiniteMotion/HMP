import SwiftUI

/// 复古模糊背景 - 对应 Android BlurBackground.kt
struct BlurBackgroundView: View {
    let albumArtUri: String?
    let musicPath: String?
    let isDark: Bool
    
    @State private var resolvedImage: UIImage?
    @State private var scale: CGFloat = 1.2
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                let baseColor = isDark ? Color(red: 0.07, green: 0.07, blue: 0.07) : Color.white
                baseColor
                    .frame(width: geometry.size.width, height: geometry.size.height)
                
                if let resolvedImage {
                    Image(uiImage: resolvedImage)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: geometry.size.width, height: geometry.size.height)
                        .scaleEffect(scale)
                        .opacity(isDark ? 0.4 : 0.3)
                        .blur(radius: 50)
                } else {
                    placeholderView
                        .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
        }
        .clipped()
        .task(id: albumArtUri) { resolveImage() }
        .onAppear {
            withAnimation(.linear(duration: 30).repeatForever(autoreverses: true)) {
                scale = 1.4
            }
        }
    }
    
    private func resolveImage() {
        Task.detached(priority: .userInitiated) {
            var loadedImage: UIImage? = nil
            
            if let uri = albumArtUri, !uri.isEmpty {
                let fileManager = FileManager.default
                if fileManager.fileExists(atPath: uri) {
                    if let image = CoverCache.shared.get(path: uri) {
                        print("✅ BlurBackground: Loaded image from cache: \(uri)")
                        loadedImage = image
                    } else if let directImage = UIImage(contentsOfFile: uri) {
                        print("✅ BlurBackground: Loaded image directly: \(uri)")
                        loadedImage = directImage
                    }
                } else {
                    print("⚠️ BlurBackground: File does not exist at path: \(uri)")
                }
            }
            
            if loadedImage == nil, let path = musicPath, !path.isEmpty {
                print("🔄 BlurBackground: Trying to extract cover from music file: \(path)")
                if let image = CoverCache.shared.getOrExtract(musicPath: path) {
                    print("✅ BlurBackground: Extracted cover from music file")
                    loadedImage = image
                } else {
                    print("❌ BlurBackground: Failed to extract cover from music file")
                }
            }
            
            await MainActor.run {
                resolvedImage = loadedImage
            }
        }
    }
    
    private var placeholderView: some View {
        (isDark ? Color(red: 0.07, green: 0.07, blue: 0.07) : Color.white)
    }
}
