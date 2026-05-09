import SwiftUI
import shared

struct SharedLabelIcon: View {
    let iconName: String
    let size: CGFloat
    @State private var image: UIImage?

    var body: some View {
        Group {
            if let image {
                Image(uiImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: size, height: size)
            } else {
                Image(systemName: "music.note")
                    .font(.system(size: size * 0.6))
                    .frame(width: size, height: size)
            }
        }
        .task {
            if let nsData = SharedIconLoader.shared.loadIconAsData(iconName: iconName.lowercased()) {
                let data = nsData as Data
                if let uiImage = UIImage(data: data) {
                    await MainActor.run { self.image = uiImage }
                }
            }
        }
    }
}
