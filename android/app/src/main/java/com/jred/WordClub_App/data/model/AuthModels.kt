package com.jred.WordClub_App.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserInfo
)

data class UserInfo(
    val id: Long,
    val username: String,
    val email: String,
    val nickname: String?,
    val avatarUrl: String?
)

data class ApiResult<T>(
    val code: Int,
    val message: String,
    val data: T?
)
