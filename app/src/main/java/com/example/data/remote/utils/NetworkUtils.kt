package com.example.data.remote.utils

import com.example.core.utils.Logger
import com.example.core.utils.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> {
                    Logger.e("Network Error", throwable)
                    Result.Error(throwable, "Network Error: Please check your internet connection.")
                }
                is HttpException -> {
                    Logger.e("HTTP Error ${throwable.code()}", throwable)
                    val errorResponse = throwable.response()?.errorBody()?.string()
                    Result.Error(throwable, errorResponse ?: "Unknown HTTP Error")
                }
                else -> {
                    Logger.e("Unknown Error", throwable)
                    Result.Error(throwable, throwable.message)
                }
            }
        }
    }
}
