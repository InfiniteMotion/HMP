package com.example.hearablemusicplayer.data.repository

/**
 * 统一的结果封装类型,用于 Repository 层的错误处理
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * 应用层异常封装
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    // 数据库相关错误
    class DatabaseError(message: String, cause: Throwable? = null) : AppException(message, cause)
    
    // 网络相关错误
    class NetworkError(message: String, cause: Throwable? = null) : AppException(message, cause)
    
    // I/O 错误
    class IOError(message: String, cause: Throwable? = null) : AppException(message, cause)
    
    // 数据解析错误
    class ParseError(message: String, cause: Throwable? = null) : AppException(message, cause)
    
    // 业务逻辑错误
    class BusinessError(message: String) : AppException(message)
    
    // 未知错误
    class UnknownError(message: String, cause: Throwable? = null) : AppException(message, cause)
}

/**
 * 扩展函数:安全执行并返回 Result
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(mapException(e))
    }
}

/**
 * 异常映射
 */
private fun mapException(e: Exception): AppException {
    return when (e) {
        is android.database.SQLException -> AppException.DatabaseError("数据库操作失败", e)
        is java.io.IOException -> AppException.IOError("文件操作失败", e)
        is com.google.gson.JsonSyntaxException -> AppException.ParseError("数据解析失败", e)
        is java.net.UnknownHostException -> AppException.NetworkError("网络连接失败", e)
        is java.net.SocketTimeoutException -> AppException.NetworkError("网络请求超时", e)
        else -> AppException.UnknownError("未知错误: ${e.message}", e)
    }
}
