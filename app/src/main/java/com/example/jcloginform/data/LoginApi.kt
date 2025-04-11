package com.example.jcloginform.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String
)

interface LoginApi {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
} 