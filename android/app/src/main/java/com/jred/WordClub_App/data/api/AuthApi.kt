package com.jred.WordClub_App.data.api

import com.jred.WordClub_App.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResult<AuthResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResult<Unit>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResult<Unit>>

    @GET("auth/me")
    suspend fun me(): Response<ApiResult<UserInfo>>
}
