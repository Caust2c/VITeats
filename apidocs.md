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

### getstudWBalinfo

GET /getstudWBalinfo?rno=<APPLICATION-NUMBER>
Payload = <APPLICATION-NUMBER>

Response:
```agsl
[{"cardno":"<CARD-NUMBER>","bal":<REMAINING-WALLET-BALANCE>,"name":"<USER-NAME>","regno":"<USER-REGISTRATION-NUMBER>","email":"<USER-UNIVERSITY-EMAIL>","sts":0(dunno),"custid":"<APPLICATION-NUMBER>"}]

```
Pretty obvious what this fetches.

### getprefers

unknown GET API
Payload: {DocumentNo:'<NUMBER>'}

Preview:
```agsl
[{cid: "1", cname: "BREAKFAST", opt: 0}, {cid: "2", cname: "LUNCH", opt: 0},…]
0
: 
{cid: "1", cname: "BREAKFAST", opt: 0}
1
: 
{cid: "2", cname: "LUNCH", opt: 0}
2
: 
{cid: "3", cname: "SNACKS", opt: 0}
3
: 
{cid: "4", cname: "DINNER", opt: 0}
```
Could be menu options selected and stuff

### getmenugroup

GET /getmenugroup

Response:
```agsl
[{"grpid":1,"grpname":"VEG"},{"grpid":2,"grpname":"NON VEG"},{"grpid":3,"grpname":"VEGAN"},{"grpid":4,"grpname":""}]

```
Mostly useless or a future feature api.
### getoffers

GET /getoffers
Payload:
```agsl
{DocumentNo:'<NUMBER>',SessionNo:'<NUMBER>',mobno:'<DONT-KNOW-WHAT-THE-FUCK-THIS-IS-BUT-IT-AINT-MOB-NO>',flg:'2',oid:'2',odt:'21-Aug-2026'}
```

Response:
```agsl
0
```
Never have i seen a discount/offer in food park so this is also possibly another useless api.
