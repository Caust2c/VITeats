# Task Management

- [ ] Fix authentication logic to prevent login with incorrect PIN
	- [/] Review `AuthRepository.kt` and `AuthViewModel.kt`
	- [ ] Update `AuthRepository.kt` to strictly check `OtpSts`
- [ ] Investigate and fix cookie handling in `NetworkModule.kt`
- [ ] Ensure all dynamic values are correctly used across repositories
- [ ] Verify that no hardcoded user-specific values exist in the codebase
