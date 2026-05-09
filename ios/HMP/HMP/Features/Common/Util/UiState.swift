/// UI 状态枚举 - 对应 Android UiState.kt
enum UiState<T> {
    case idle
    case loading
    case success(T)
    case error(String)
    case empty
}

// MARK: - 便捷属性

extension UiState {
    var isLoading: Bool {
        if case .loading = self { return true }
        return false
    }

    var isSuccess: Bool {
        if case .success = self { return true }
        return false
    }

    var data: T? {
        if case .success(let data) = self { return data }
        return nil
    }

    var errorMessage: String? {
        if case .error(let message) = self { return message }
        return nil
    }
}
