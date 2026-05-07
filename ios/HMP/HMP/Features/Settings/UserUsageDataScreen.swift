import SwiftUI
import shared

struct UserUsageDataScreen: View {
    @Environment(\.dismiss) private var dismiss
    @State private var isLoading: Bool = false
    @State private var selectedTab: String = "overview"
    
    // 模拟数据
    @State private var totalListeningMinutes: Int = 12580
    @State private var thisWeekMinutes: Int = 420
    @State private var lastWeekMinutes: Int = 380
    @State private var totalPlayCount: Int = 1250
    @State private var totalSkipCount: Int = 156
    @State private var likedCount: Int = 89
    @State private var completionRate: Double = 0.78
    @State private var skipRate: Double = 0.12
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                OverviewCard()
                    .padding(.horizontal, 20)
                
                TasteCard()
                    .padding(.horizontal, 20)
                
                RankingAndHistoryCard()
                    .padding(.horizontal, 20)
                
                RecentHistoryBlock()
                    .padding(.horizontal, 20)
            }
            .padding(.vertical, 16)
            .padding(.bottom, 64)
        }
    }
    
    @ViewBuilder
    private func OverviewCard() -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("听歌洞察")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 16) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("总听歌时长")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text("\(totalListeningMinutes)")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                    }
                    
                    Spacer()
                    
                    let weekTrend = thisWeekMinutes > 0 ? ((Double(thisWeekMinutes - lastWeekMinutes) / Double(lastWeekMinutes)) * 100) : 0
                    VStack(alignment: .trailing, spacing: 4) {
                        Text("本周")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        HStack(spacing: 2) {
                            Text("\(thisWeekMinutes) 分钟")
                            Image(systemName: weekTrend >= 0 ? "arrow.up" : "arrow.down")
                            Text("\(abs(Int(weekTrend)))%")
                        }
                        .font(.caption)
                    }
                }
                
                // Insight Pills
                LazyVGrid(columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ], spacing: 8) {
                    InsightPill(label: "本周", value: "\(thisWeekMinutes)")
                    InsightPill(label: "上周", value: "\(lastWeekMinutes)")
                }
                
                LazyVGrid(columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ], spacing: 8) {
                    InsightPill(label: "播放次数", value: "\(totalPlayCount)")
                    InsightPill(label: "跳过次数", value: "\(totalSkipCount)")
                    InsightPill(label: "收藏", value: "\(likedCount)")
                }
                
                // 进度条
                VStack(spacing: 8) {
                    ProgressRow(label: "完成率", value: completionRate, isPositive: true)
                    ProgressRow(label: "跳过率", value: skipRate, isPositive: false)
                }
            }
            .padding(16)
            .background(Color.secondary.opacity(0.1))
            .cornerRadius(20)
        }
    }
    
    @ViewBuilder
    private func InsightPill(label: String, value: String) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.headline)
                .fontWeight(.bold)
            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(10)
        .background(Color.secondary.opacity(0.1))
        .cornerRadius(12)
    }
    
    @ViewBuilder
    private func ProgressRow(label: String, value: Double, isPositive: Bool) -> some View {
        VStack(spacing: 4) {
            HStack {
                Text(label)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Spacer()
                Text("\(Int(value * 100))%")
                    .font(.caption)
                    .fontWeight(.semibold)
            }
            ProgressView(value: value)
                .tint(isPositive ? .blue : .gray)
        }
    }
    
    @ViewBuilder
    private func TasteCard() -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("听歌口味")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(alignment: .leading, spacing: 12) {
                Text("Top 流派")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                // 模拟标签数据
                LabelStackedBar(labels: [
                    ("流行", 0.45),
                    ("摇滚", 0.30),
                    ("民谣", 0.15),
                    ("电子", 0.10)
                ])
                
                Text("Top 心情")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.top, 8)
                
                LabelStackedBar(labels: [
                    ("轻松", 0.40),
                    ("动感", 0.35),
                    ("安静", 0.25)
                ])
            }
            .padding(16)
            .background(Color.secondary.opacity(0.1))
            .cornerRadius(20)
        }
    }
    
    @ViewBuilder
    private func LabelStackedBar(labels: [(String, Double)]) -> some View {
        VStack(spacing: 8) {
            // 条形图
            GeometryReader { geo in
                HStack(spacing: 2) {
                    ForEach(Array(labels.enumerated()), id: \.offset) { index, label in
                        Rectangle()
                            .fill(barColor(for: index))
                            .frame(width: geo.size.width * label.1)
                    }
                }
            }
            .frame(height: 24)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            
            // 图例
            ForEach(Array(labels.enumerated()), id: \.offset) { index, label in
                HStack {
                    Circle()
                        .fill(barColor(for: index))
                        .frame(width: 8, height: 8)
                    Text(label.0)
                        .font(.caption)
                    Spacer()
                    Text("\(Int(label.1 * 100))%")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
    }
    
    private func barColor(for index: Int) -> Color {
        let colors: [Color] = [.blue, .purple, .orange, .green, .pink]
        return colors[index % colors.count]
    }
    
    @ViewBuilder
    private func RankingAndHistoryCard() -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("排行榜与历史")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 12) {
                Picker("", selection: $selectedTab) {
                    Text("播放最多").tag("top_played")
                    Text("歌手排行").tag("top_artists")
                }
                .pickerStyle(.segmented)
                
                // 模拟排行数据
                VStack(spacing: 8) {
                    ForEach(0..<5) { index in
                        RankingItem(
                            rank: index + 1,
                            title: "歌曲 \(index + 1)",
                            subtitle: "艺术家 \(index + 1)",
                            count: 100 - index * 15
                        )
                    }
                }
            }
            .padding(16)
            .background(Color.secondary.opacity(0.1))
            .cornerRadius(20)
        }
    }
    
    @ViewBuilder
    private func RankingItem(rank: Int, title: String, subtitle: String, count: Int) -> some View {
        HStack(spacing: 12) {
            if rank <= 3 {
                Circle()
                    .fill(rankColor(for: rank))
                    .frame(width: 28, height: 28)
                    .overlay(
                        Text("\(rank)")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                    )
            } else {
                Circle()
                    .fill(Color.secondary.opacity(0.3))
                    .frame(width: 28, height: 28)
                    .overlay(
                        Text("\(rank)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    )
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.body)
                    .fontWeight(rank <= 3 ? .semibold : .regular)
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Text("\(count)")
                .font(.headline)
                .fontWeight(.semibold)
        }
        .padding(12)
        .background(Color.secondary.opacity(rank <= 3 ? 0.15 : 0.05))
        .cornerRadius(14)
    }
    
    private func rankColor(for rank: Int) -> Color {
        switch rank {
        case 1: return .yellow
        case 2: return .gray
        case 3: return .orange
        default: return .blue
        }
    }
    
    @ViewBuilder
    private func RecentHistoryBlock() -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Rectangle()
                    .fill(Color.blue)
                    .frame(width: 4, height: 20)
                Text("最近播放")
                    .font(.headline)
            }
            .foregroundColor(.primary)
            
            VStack(spacing: 8) {
                ForEach(0..<5) { index in
                    RecentPlaybackItem(
                        title: "歌曲 \(index + 1)",
                        artist: "艺术家 \(index + 1)",
                        time: formatTimeAgo(hoursAgo: index * 2),
                        duration: "3:45",
                        isCompleted: index % 3 != 0
                    )
                }
            }
        }
    }
    
    @ViewBuilder
    private func RecentPlaybackItem(title: String, artist: String, time: String, duration: String, isCompleted: Bool) -> some View {
        HStack(spacing: 12) {
            Rectangle()
                .fill(isCompleted ? Color.blue.opacity(0.8) : Color.secondary.opacity(0.5))
                .frame(width: 4, height: 40)
                .cornerRadius(2)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.body)
                    .fontWeight(.medium)
                Text(artist)
                    .font(.caption)
                    .foregroundColor(.secondary)
                HStack(spacing: 12) {
                    Text(duration)
                    Text(time)
                }
                .font(.caption2)
                .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Text(isCompleted ? "已完成" : "未完成")
                .font(.caption2)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.secondary.opacity(0.2))
                .cornerRadius(8)
        }
        .padding(12)
        .background(Color.secondary.opacity(0.05))
        .cornerRadius(14)
    }
    
    private func formatTimeAgo(hoursAgo: Int) -> String {
        if hoursAgo < 1 {
            return "刚刚"
        } else if hoursAgo < 24 {
            return "\(hoursAgo)小时前"
        } else {
            return "\(hoursAgo / 24)天前"
        }
    }
}

#Preview {
    UserUsageDataScreen()
}
