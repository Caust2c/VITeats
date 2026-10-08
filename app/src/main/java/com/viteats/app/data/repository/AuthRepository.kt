package com.viteats.app.data.repository

import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.LoginRequest
import com.viteats.app.data.remote.LoginResponse
import com.viteats.app.data.remote.ProodleApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class AuthRepository(
    private val api: ProodleApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(mobileNo: String, otpNo: String): Response<List<LoginResponse>> {
        val response = api.verifyOtp(LoginRequest(mobileNo, otpNo))
        if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
            val otpSts = response.body()!![0].otpSts
            if (otpSts.startsWith("duplicate")) {
                sessionManager.registrationNumber = mobileNo
                
                val parts = otpSts.split("|")
                if (parts.size > 6) {
                    sessionManager.userIdentifier = parts[6]
                }
                sessionManager.cachedPin = otpNo
            } else {
                // Strictly return error if OtpSts does not start with "duplicate"
                return Response.error(401, "{\"error\":\"Invalid PIN\"}".toResponseBody("application/json".toMediaTypeOrNull()))
            }
        }
        return response
    }

    fun getCachedPin(): String? = sessionManager.cachedPin

    fun getRegistrationNumber(): String? = sessionManager.registrationNumber

    fun logout() {
        sessionManager.clear()
    }

    fun isLoggedIn(): Boolean = sessionManager.hasValidSession()
}
