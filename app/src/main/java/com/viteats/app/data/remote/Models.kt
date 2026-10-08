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

// --- Order Placement ---
data class OrderIdRequest(
    @SerializedName("MobileNo") val mobileNo: String,
    @SerializedName("TableNo") val tableNo: String = "1"
)

data class OrderIdResponse(
    @SerializedName("OrderNo") val orderNo: String
)

data class OrderInsertItem(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("ides") val ides: String,
    @SerializedName("rt") val rt: Double,
    @SerializedName("amt") val amt: Double,
    @SerializedName("tp") val tp: String = "P",
    @SerializedName("odt") val odt: String,
    @SerializedName("odtdes") val odtdes: String,
    @SerializedName("tb") val tb: Int = 2,
    @SerializedName("pid") val pid: String,
    @SerializedName("dtstr") val dtstr: String,
    @SerializedName("ldes") val ldes: String,
    @SerializedName("cal") val cal: String = "",
    @SerializedName("sname") val sname: String,
    @SerializedName("skid") val skid: String,
    @SerializedName("flag") val flag: Int = 1,
    @SerializedName("optcls") val optcls: String = ""
)

data class OrderInsertRequest(
    @SerializedName("items") val items: List<OrderInsertItem>,
    @SerializedName("OrderNumber") val orderNumber: String,
    @SerializedName("TableNo") val tableNo: String = "1",
    @SerializedName("ItemTotal") val itemTotal: String,
    @SerializedName("OutLetId") val outLetId: String,
    @SerializedName("MobileNo") val mobileNo: String,
    @SerializedName("RefNo") val refNo: String = ""
)

data class PaymentRequest(
    @SerializedName("MobileNo") val mobileNo: String,
    @SerializedName("OrderNumber") val orderNumber: String,
    @SerializedName("OrderAmount") val orderAmount: String,
    @SerializedName("ouid") val ouid: String,
    @SerializedName("Otp") val otp: String,
    @SerializedName("p1") val p1: String
)
