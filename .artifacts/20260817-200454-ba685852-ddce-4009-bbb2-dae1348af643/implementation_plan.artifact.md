# Fix Authentication Logic and Menu Visibility

Address the issue where incorrect PINs still allow login and the menu remains hidden due to session/parameter issues.

## User Review Required

- Confirm that `duplicate` is the ONLY indicator of success in `OtpSts`.
- Confirm the meal time windows and their mapping to `SessionNo`.

## Proposed Changes

### Data and Repository Layer

#### [AuthRepository.kt](file:///C:/Users/hardi/VITeats/app/src/main/java/com/viteats/app/data/repository/AuthRepository.kt)

- Update `login` to strictly check for `duplicate` and return an error response if it fails.
- Ensure `isLoggedIn()` is robust by checking both the registration number and the user identifier.

```kotlin
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
            return response
        } else {
            // Explicitly return an error if PIN validation fails on backend
            return Response.error(401, "{\"error\":\"Invalid PIN\"}".toResponseBody("application/json".toMediaTypeOrNull()))
        }
    }
    return response
}
```

#### [MenuRepository.kt](file:///C:/Users/hardi/VITeats/app/src/main/java/com/viteats/app/data/repository/MenuRepository.kt)

- Refine `getMenuParams` to ensure it never returns null unless it's truly outside meal hours.
- Add more robust logging for debugging.

---

### UI Layer

#### [AuthViewModel.kt](file:///C:/Users/hardi/VITeats/app/src/main/java/com/viteats/app/ui/auth/AuthViewModel.kt)

- Update the success condition to rely solely on the repository's response validation.

## Verification Plan

### Manual Verification
1. **Authentication**:
    - Enter correct application number but INCORRECT PIN. Verify the app stays on the login screen and shows an error.
    - Enter correct application number and CORRECT PIN. Verify successful navigation to Home.
2. **Menu**:
    - Verify that menu items appear during valid meal windows (Breakfast, Lunch, Snacks, Dinner).
    - Check Logcat for "MenuRepository" tags to confirm dynamic date and session calculation.
