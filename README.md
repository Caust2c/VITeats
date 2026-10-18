# VITeats

Android food ordering app built with Kotlin and Jetpack Compose. Backend request and response details are documented in [apidocs.md](apidocs.md).

## Images and budgeting

Menu and cart images use the API product ID to derive the image URL on access, including after Gson deserialization. A food icon appears while loading or when the image request fails.

Monthly spending comes from unique successful or delivered orders dated within the current month in India. Old, failed, pending, cancelled, invalid-date, and future orders are excluded. Changing the budget or topping up the wallet does not change recorded spending.

The daily allowance is the smaller of the wallet balance and remaining monthly budget, divided by the days remaining including today. With no budget set, the wallet provides the allowance. Budget limits support decimals and reset at the start of a new month. Expense refresh failures show a retry action and retain the last loaded totals.

## Ordering and saved history

Settings includes a daily auto-order time in IST, complete/partial favourites selection,
and a per-order hard budget limit (blank/zero disables the limit). Auto-order is opt-in,
uses the encrypted saved PIN, and buys one of each favourite currently available from
the live menu. Complete mode skips the order unless all favourites are available;
partial mode buys available favourites. Orders must contain items from one outlet.

Manual checkout prompts before exceeding the per-order limit and supports an explicit
one-order override. Automatic orders above the limit are skipped. Stock and prices are
checked again before checkout; changed prices require reviewing the cart again.

WorkManager persists daily work across process restarts/reboots. Android can delay
execution; runs more than 15 minutes late are skipped. Each account can claim only one
automatic payment attempt per IST day, and ambiguous payment responses are not retried.
Settings shows the latest automatic-order result. No real payment runs during tests.

Room stores history separately per account, with exact item snapshots/quantities for
orders made through this version. History refreshes preserve the basket and original
QR response string. QR display decodes a copy; it does not rewrite the saved base64.
Tap any order card or View QR to reopen it, including offline when saved. Keeping a QR
does not extend its collection validity. Old orders can be imported from Proodle, but
missing historical quantities and QR responses cannot be reconstructed.

Order cards have a fixed height with a scrollable item list. Reorder adds the saved
quantities to the current cart using current menu prices and stock. It rejects unavailable
items before changing the cart. Imported orders without exact item snapshots cannot
be reordered safely.

## Validation

With the Android SDK and JDK 21 available, run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
