package com.viteats.app.data.repository

import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.BalanceResponse
import com.viteats.app.data.remote.ProodleApi
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class StudentRepository(
    private val api: ProodleApi,
    private val sessionManager: SessionManager
) {
    suspend fun getBalance(): Response<List<BalanceResponse>> {
        val regNo = sessionManager.registrationNumber
        if (regNo.isNullOrBlank()) {
            sessionManager.notifySessionExpired()
            return Response.error(401, "".toResponseBody(null))
        }
        val response = api.getBalanceInfo(regNo)
        if (response.code() == 401 || response.code() == 403) {
            sessionManager.notifySessionExpired()
        }
        return response
    }
}
