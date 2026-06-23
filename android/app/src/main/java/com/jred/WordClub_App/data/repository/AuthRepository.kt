package com.jred.WordClub_App.data.repository

import com.jred.WordClub_App.data.api.RetrofitClient
import com.jred.WordClub_App.data.model.*
import com.jred.WordClub_App.util.TokenManager

class AuthRepository(private val tokenManager: TokenManager) {

    private val api = RetrofitClient.authApi

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 200 && body.data != null) {
                    tokenManager.saveToken(body.data.token)
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "登录失败"))
                }
            } else {
                val msg = parseError(response.errorBody()?.string())
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.message}"))
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<Unit> {
        return try {
            val response = api.register(RegisterRequest(username, email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 200) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(body?.message ?: "注册失败"))
                }
            } else {
                val msg = parseError(response.errorBody()?.string())
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.message}"))
        }
    }

    suspend fun logout() {
        try {
            api.logout()
        } catch (_: Exception) {}
        tokenManager.clearToken()
    }

    suspend fun fetchUser(): Result<UserInfo> {
        return try {
            val response = api.me()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 200 && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("获取用户信息失败"))
                }
            } else {
                tokenManager.clearToken()
                Result.failure(Exception("登录已过期"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.message}"))
        }
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody.isNullOrEmpty()) return "请求失败"
        return try {
            val gson = com.google.gson.Gson()
            val result = gson.fromJson(errorBody, ApiResult::class.java)
            result.message ?: "请求失败"
        } catch (_: Exception) {
            "请求失败"
        }
    }
}
