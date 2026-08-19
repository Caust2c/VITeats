# Proodle Website API Documentation

## Authentication

Base URL:
https://vit-proodle.expertsoftsys.com

### Verify OTP

POST /api/GetOTPChecking

Request:
```
{
"MobileNo": "<mobile>",
"OtpNo": "<otp>"
}
```
## 1. Authentication

### GetOTPChecking

POST /GetOTPChecking

Purpose:
Authenticate / verify the student's login credentials.

Request:
```
{
"MobileNo": "<APPLICATION_NUMBER>",
"OtpNo": "<PIN>"
}
```
Note:
The API field is named MobileNo, but the value observed in the application is the student's application number.

Response:
```
[
{
"OtpSts": "<STATUS>|<VALUE>|<VALUE>|<VALUE>|<NAME>|<VALUE>|<VALUE>",
"prefer": null,
"ouid": null,
"zname": null,
"ctrid": null,
"logid": null,
"bstm": null,
"betm": null
}
]
```
Observed status:
OtpSts began with "duplicate".

The exact meaning of the remaining pipe-delimited fields has not yet been established.

Authentication/session behavior:
The request is sent with an ASP.NET_SessionId cookie.
The exact authentication mechanism after successful verification still needs to be determined.