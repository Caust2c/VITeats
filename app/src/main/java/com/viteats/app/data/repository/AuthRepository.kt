package com.viteats.app.data.repository

import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.LoginRequest
import com.viteats.app.data.remote.LoginResponse
import com.viteats.app.data.remote.ProodleApi
import retrofit2.Response

class AuthRepository(
    private val api: ProodleApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(mobileNo: String, otpNo: String): Response<List<LoginResponse>> {
        val response = api.verifyOtp(LoginRequest(mobileNo, otpNo))
        if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
            val otpSts = response.body()!![0].otpSts
            if (otpSts.startsWith("duplicate") || otpSts.isNotBlank()) {
                sessionManager.registrationNumber = mobileNo
                
                val parts = otpSts.split("|")
                if (parts.size > 6) {
                    sessionManager.userIdentifier = parts[6]
                }
            }
        }
        return response
    }

    fun logout() {
        sessionManager.clear()
    }

    fun isLoggedIn(): Boolean = sessionManager.registrationNumber != null
}
