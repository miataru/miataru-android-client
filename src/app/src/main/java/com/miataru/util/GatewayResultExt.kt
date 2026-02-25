package com.miataru.util

import com.miataru.data.remote.GatewayResult

fun <T> GatewayResult<T>.toResult(): Result<T> {
    return when (this) {
        is GatewayResult.Success -> Result.success(value)
        is GatewayResult.Error -> Result.failure(IllegalStateException(message, cause))
    }
}
