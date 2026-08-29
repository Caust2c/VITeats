package com.viteats.app.data.remote

import com.google.gson.annotations.SerializedName

// --- Authentication ---
data class LoginRequest(
    @SerializedName("MobileNo") val mobileNo: String,
    @SerializedName("OtpNo") val otpNo: String
)

data class LoginResponse(
    @SerializedName("OtpSts") val otpSts: String,
    val prefer: String?,
    val ouid: String?,
    val zname: String?,
    val ctrid: String?,
    val logid: String?,
    val bstm: String?,
    val betm: String?
)

// --- Student Info ---
data class BalanceResponse(
    val cardno: String,
    val bal: Double,
    val name: String,
    val regno: String,
    val email: String,
    val sts: Int,
    val custid: String
)

// --- Order Requests ---
data class OrderListRequest(
    @SerializedName("MobileNo") val mobileNo: String
)

// --- Menu ---
data class MenuGroup(
    val grpid: Int,
    val grpname: String
)

data class MenuCategory(
    val skid: Int,
    val skname: String,
    val grpid: Int,
    val grpname: String
)

data class MenuItem(
    val meitid: Int,
    val meitdes: String,
    val retrt: Double,
    val odtdes: String,
    val skuid: Int,
    val dispname: String,
    val skudes: String,
    val StockQty: Int,
    val icat: Int,
    val StartTime: String?,
    val EndTime: String?,
    // Helper to get image URL
    val imageUrl: String = "https://vit-proodle.expertsoftsys.com/images/$meitid.jpg"
)

// --- Orders ---
data class Order(
    val OrderDate: String,
    val OrderTime: String,
    val NetAmount: Double,
    val OrderId: String,
    val Status: String,
    val CancelStatus: String,
    val sname: String,
    val qrstat: Int,
    val RegNo: String,
    val studname: String
)
//String response stored for QR and then base64 decode to image (tested and works)
typealias OrderQRResponse = String
