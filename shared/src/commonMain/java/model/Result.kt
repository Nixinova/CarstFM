package com.nixinova.carstfm.shared.model

class Result<T> {
    val rawValue: T?
    val value: T
        get() = rawValue ?: throw Exception("Cannot access value of failed result")
    val valueOrNull: T?
        get() = rawValue

    val rawError: String?
    val error: String
        get() = rawError ?: throw Exception("Cannot access error of successful result")

    val isOk
        get() = this.rawValue != null
    val isError
        get() = this.rawError != null

    inline fun valueOrReturn(onFail: (String) -> Nothing): T {
        if (isError) {
            onFail(error)
        } else {
            return value
        }
    }

    private constructor(value: T?, error: String?) {
        this.rawValue = value
        this.rawError = error
    }

    companion object {
        fun <T> Ok(value: T): Result<T> {
            return Result(value, null)
        }

        fun <T> Fail(error: String): Result<T> {
            return Result(null, error);
        }
    }
}
