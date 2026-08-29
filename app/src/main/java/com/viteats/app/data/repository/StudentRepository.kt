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
        val regNo = sessionManager.registrationNumber ?: return Response.error(401, "".toResponseBody(null))
        return api.getBalanceInfo(regNo)
    }
}
