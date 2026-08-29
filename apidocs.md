# Proodle Website API Documentation

> Agent/developer reference for the VIT Proodle website API discovered through browser network inspection.
>
> **Status:** Reverse-engineered documentation. Some fields and behaviors are confirmed by observed requests/responses; others are explicitly marked as unknown or inferred.
>
> **Security:** Never commit real OTPs, session cookies, Cloudflare cookies, credentials, payment references, or unnecessary personal data.

## Note: This document was AI-generated and contains additional instructions for AI agents (rules and such). It is still valid documentation and should be used as a reference when working with the Proodle API.

## Base URL

```text
https://vit-proodle.expertsoftsys.com/api
```

---

# 1. Authentication

## 1.1 GetOTPChecking

**Method:** `POST`

**Endpoint:** `/api/GetOTPChecking`

### Purpose

Authenticate / verify the student's login credentials.

### Request

```json
{
  "MobileNo": "<APPLICATION_NUMBER>",
  "OtpNo": "<OTP_OR_PIN>"
}
```

### Important note

The API field is named `MobileNo`, but the observed application behavior indicates that the student's application number is supplied in this field.

### Observed response

```json
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

Observed `OtpSts` began with `duplicate`.

The remaining pipe-delimited fields have not been established.

### Session behavior

The request was observed with an `ASP.NET_SessionId` cookie. The exact post-login authentication/session mechanism is still unknown.

---

# 2. Student / Account Information

## 2.1 getstudWBalinfo

**Method:** `GET`

**Endpoint:**

```text
/api/getstudWBalinfo?rno=<APPLICATION_NUMBER>
```

### Purpose

Fetch student wallet/account information.

### Response

```json
[
  {
    "cardno": "<CARD_NUMBER>",
    "bal": 2630,
    "name": "<USER_NAME>",
    "regno": "<REGISTRATION_NUMBER>",
    "email": "<UNIVERSITY_EMAIL>",
    "sts": 0,
    "custid": "<APPLICATION_NUMBER>"
  }
]
```

### Fields

| Field | Description | Confidence |
|---|---|---|
| `cardno` | Student card number | Confirmed |
| `bal` | Current wallet balance | Confirmed |
| `name` | Student name | Confirmed |
| `regno` | University registration number | Confirmed |
| `email` | University email | Confirmed |
| `sts` | Status field; exact meaning unknown | Unknown |
| `custid` | Customer/application identifier | Observed |

---

# 3. Meal Preferences

## 3.1 getprefers

**Method:** `POST`

**Endpoint:** `/api/getprefers`

### Purpose

Fetch available meal categories/preferences.

### Response

```json
[
  {"cid": "1", "cname": "BREAKFAST", "opt": 0},
  {"cid": "2", "cname": "LUNCH", "opt": 0},
  {"cid": "3", "cname": "SNACKS", "opt": 0},
  {"cid": "4", "cname": "DINNER", "opt": 0}
]
```

### Fields

| Field | Description | Confidence |
|---|---|---|
| `cid` | Meal/category ID | Confirmed |
| `cname` | Meal category name | Confirmed |
| `opt` | Preference/selection flag | Unknown |

---

# 4. Menu Structure

## 4.1 getmenugroup

**Method:** `POST`

**Endpoint:** `/api/getmenugroup`

### Purpose

Fetch food classification groups.

### Response

```json
[
  {"grpid": 1, "grpname": "VEG"},
  {"grpid": 2, "grpname": "NON VEG"},
  {"grpid": 3, "grpname": "VEGAN"},
  {"grpid": 4, "grpname": ""}
]
```

---

## 4.2 Getcatlistnew

**Method:** `POST`

**Endpoint:** `/api/Getcatlistnew`

### Purpose

Fetch food outlets/categories and their supported food groups.

### Response

```json
[
  {"skid": 1, "skname": "DESIR", "grpid": 1, "grpname": "VEG"},
  {"skid": 1, "skname": "DESIR", "grpid": 2, "grpname": "NON VEG"},
  {"skid": 2, "skname": "TAWAZ", "grpid": 1, "grpname": "VEG"},
  {"skid": 2, "skname": "TAWAZ", "grpid": 2, "grpname": "NON VEG"},
  {"skid": 5, "skname": "GO GRILL", "grpid": 1, "grpname": "VEG"},
  {"skid": 5, "skname": "GO GRILL", "grpid": 2, "grpname": "NON VEG"},
  {"skid": 6, "skname": "FRUITERIA", "grpid": 1, "grpname": "VEG"}
]
```

### Fields

| Field | Description |
|---|---|
| `skid` | Outlet/category ID |
| `skname` | Outlet/category name |
| `grpid` | Food group ID |
| `grpname` | Food group name |

Observed outlets: `DESIR`, `TAWAZ`, `GO GRILL`, `FRUITERIA`.

---

# 5. Menu Items

## 5.1 GetOptionMenuItems

**Method:** `POST`

**Endpoint:** `/api/GetOptionMenuItems`

### Purpose

Fetch menu items, prices, meal periods, outlets, stock, portions and ordering windows.

### Example response item

```json
{
  "meitid": 204,
  "meitdes": "CHICKEN SUKKA",
  "retrt": 120.00,
  "cmbdls": "",
  "type": "P",
  "qty": 1,
  "odtdes": "LUNCH",
  "skuid": 1,
  "odt": "/Date(1738737060000)/",
  "itmsdes": "",
  "fltr": "Y",
  "dispname": "250G DRY",
  "dtstr": "05-Feb-2025",
  "tb": 2,
  "skudes": "DESIR",
  "StockQty": 9,
  "mbq": 25,
  "ord": 1,
  "StartTime": "/Date(1738733460000)/",
  "EndTime": "/Date(1738751400000)/",
  "chk": 0,
  "icat": 2,
  "optex": 0
}
```

### Fields

| Field | Description | Confidence |
|---|---|---|
| `meitid` | Menu item ID | Confirmed |
| `meitdes` | Item name | Confirmed |
| `retrt` | Item price/rate | Observed |
| `cmbdls` | Combination/details field | Unknown |
| `type` | Item type; observed `P` | Observed |
| `qty` | Default quantity | Observed |
| `odtdes` | Meal period | Confirmed |
| `skuid` | Outlet ID | Strongly inferred |
| `odt` | Legacy serialized date | Observed |
| `itmsdes` | Item description field | Unknown |
| `fltr` | Filter/display flag | Unknown |
| `dispname` | Portion/display description | Confirmed |
| `dtstr` | Display date | Confirmed |
| `tb` | Legacy field; observed `2` | Unknown |
| `skudes` | Outlet name | Confirmed |
| `StockQty` | Current stock quantity | Confirmed |
| `mbq` | Quantity-related value/limit | Unknown |
| `ord` | Ordering/status value | Unknown |
| `StartTime` | Ordering start time | Observed |
| `EndTime` | Ordering end time | Observed |
| `chk` | Unknown flag | Unknown |
| `icat` | Item category/group indicator | Observed |
| `optex` | Option-related flag | Unknown |

`StockQty = 0` was observed for unavailable items; positive values indicate stock was present.

---

# 6. Item Availability / Tax

## 6.1 getqoh

**Method:** `GET`

**Endpoint:**

```text
/api/getqoh?pid=<PRODUCT_ID>&SessionNo=<SESSION_NUMBER>&ouid=<OUTLET_ID>
```

### Purpose

Fetch quantity on hand for a product.

### Observed

```text
pid=204, SessionNo=2, ouid=2 -> 9
pid=69,  SessionNo=2, ouid=2 -> 50
```

---

## 6.2 getexgstval

**Method:** `GET`

**Endpoint:**

```text
/api/getexgstval?ouid=<OUTLET_ID>&pids=<PRODUCT_ID>&qtys=<QUANTITY>
```

### Purpose

Fetch an additional GST/tax-related value.

### Observed

```text
/api/getexgstval?ouid=2&pids=204&qtys=1
-> 0.00
```

Exact calculation behavior is unknown.

---

# 7. User / Session State

## 7.1 chkuserstat

**Method:** `POST`

**Endpoint:** `/api/chkuserstat`

### Request

```json
{
  "mobno": "<USER_IDENTIFIER>",
  "logid": "<LOGIN_ID>"
}
```

### Purpose

Likely checks current user/session status.

### Unknown

- Exact meaning of `mobno`
- Exact meaning of `logid`
- Response semantics

---

# 8. Order Creation

## 8.1 GetOrderIdDetails

**Method:** `POST`

**Endpoint:** `/api/GetOrderIdDetails`

### Request

```json
{
  "MobileNo": "<USER_IDENTIFIER>",
  "TableNo": "1"
}
```

### Response

```json
[
  {
    "OrderNo": "<ORDER_NUMBER>"
  }
]
```

### Purpose

Fetch/generate an order number used during order creation.

---

## 8.2 GetOutletListInsert

**Method:** `POST`

**Endpoint:** `/api/GetOutletListInsert`

### Purpose

Build/submit an order from selected menu items.

### Request

```json
{
  "items": [
    {
      "productId": "<OUTLET_ID>_<PRODUCT_ID>_<SESSION_NO>",
      "quantity": 1,
      "ides": "<ITEM_NAME>",
      "rt": 120,
      "amt": 120,
      "tp": "P",
      "odt": "<DATE>",
      "odtdes": "<DAY>",
      "tb": 2,
      "pid": "<PRODUCT_ID>",
      "dtstr": "<DATE>",
      "ldes": "<PORTION_DESCRIPTION>",
      "cal": "",
      "sname": "<MEAL>",
      "skid": "<OUTLET_ID>",
      "flag": 1,
      "optcls": ""
    }
  ],
  "OrderNumber": "<ORDER_NUMBER>",
  "TableNo": "1",
  "ItemTotal": "<TOTAL_AMOUNT>",
  "OutLetId": "<OUTLET_ID>",
  "MobileNo": "<USER_IDENTIFIER>",
  "RefNo": ""
}
```

### Observed `productId`

```text
<SKID>_<PID>_<SESSION_NO>
```

Examples observed:

```text
1_204_2
5_69_2
```

Treat this format as observed implementation behavior, not a guaranteed contract.

### Important relationships

```text
pid         -> product/menu item ID
skid        -> outlet ID
sname       -> meal category
quantity    -> requested quantity
rt          -> unit price
amt         -> line-item total
OrderNumber -> generated order identifier
OutLetId    -> outlet identifier
```

---

# 9. Payment

## 9.1 OnlineWPayment

**Method:** `POST`

**Endpoint:** `/api/OnlineWPayment`

### Purpose

Process/initialize online payment for an order.

### Request

```json
{
  "MobileNo": "<USER_IDENTIFIER>",
  "OrderNumber": "<ORDER_NUMBER>",
  "OrderAmount": "<AMOUNT>",
  "ouid": "<OUTLET_ID>",
  "Otp": "<OTP_OR_PAYMENT_OTP>",
  "p1": "<APPLICATION_NUMBER>"
}
```

### Observed response format

```text
1|<PAYMENT_REFERENCE>
```

Example shape:

```text
1|PH1/26-27/58654
```

The exact semantics of both parts are not yet formally confirmed.

---

# 10. Order History

## 10.1 GetOrderList

**Method:** `GET`

**Endpoint:** `/api/GetOrderList`

### Purpose

Fetch previous orders.

### Response example

```json
[
  {
    "OrderDate": "26/AUG/2026",
    "OrderTime": "12:53 pm",
    "NetAmount": 15.0000,
    "OrderId": "<ORDER_ID>",
    "Status": "Success",
    "CancelStatus": "Cancel",
    "sname": "LUNCH",
    "qrstat": 1,
    "RegNo": "<REGISTRATION_NUMBER>",
    "studname": "<STUDENT_NAME>"
  }
]
```

### Fields

| Field | Description | Confidence |
|---|---|---|
| `OrderDate` | Order date | Confirmed |
| `OrderTime` | Order time | Confirmed |
| `NetAmount` | Final order amount | Confirmed |
| `OrderId` | Order identifier | Confirmed |
| `Status` | Current order status | Confirmed |
| `CancelStatus` | Cancellation-related status/action | Observed |
| `sname` | Meal category | Confirmed |
| `qrstat` | QR-related status flag | Unknown |
| `RegNo` | Student registration number | Confirmed |
| `studname` | Student name | Confirmed |

Observed `Status` values:

```text
Delivered
Success
```

Observed `CancelStatus` value:

```text
Cancel
```

Observed `qrstat` values:

```text
0
1
```

---

# 11. Order QR

## 11.1 orderQR

**Method:** `GET`

**Endpoint:**

```text
/api/orderQR?ordno=<ORDER_NUMBER>
```

### Purpose

Fetch QR data associated with an order.

### Important relationship

`ordno` maps directly to the order identifier observed in order history.

```text
GetOrderList
    |
    +--> OrderId
            |
            v
orderQR?ordno=<OrderId>
            |
            v
        QR data
```

The observed QR payload can be Base64-encoded data and may need to be decoded into an image.

Exact response schema still needs to be documented.

---

# 12. Offers

## 12.1 getoffers

**Method:** `POST`

**Endpoint:** `/api/getoffers`

### Request

```json
{
  "DocumentNo": "<DOCUMENT_NUMBER>",
  "SessionNo": "<SESSION_NUMBER>",
  "mobno": "<USER_IDENTIFIER>",
  "flg": "2",
  "oid": "<OUTLET_ID>",
  "odt": "<DATE>"
}
```

### Observed response

```text
0
```

### Purpose

Likely checks/fetches offers or discounts.

No active offers were observed during testing.

Exact semantics remain unknown.

---

# 13. API Relationships

## Student identity

```text
APPLICATION_NUMBER
        |
        +--> GetOTPChecking
        |
        +--> getstudWBalinfo?rno=<APPLICATION_NUMBER>
        |
        +--> p1 in OnlineWPayment
```

## Menu hierarchy

```text
getmenugroup
     |
     +--> Food groups

Getcatlistnew
     |
     +--> Outlets/categories

GetOptionMenuItems
     |
     +--> Menu items
          |
          +--> meitid / pid
          +--> outlet
          +--> meal
          +--> price
          +--> stock
```

## Availability

```text
GetOptionMenuItems
        |
        +--> pid / meitid
        |
        v
getqoh
        |
        v
quantity on hand
```

## Order creation

```text
Menu selection
      |
      v
GetOrderIdDetails
      |
      v
OrderNumber
      |
      v
GetOutletListInsert
      |
      v
OnlineWPayment
```

## Order history / QR

```text
GetOrderList
      |
      +--> OrderId
              |
              v
      orderQR?ordno=<OrderId>
```

---

# 14. API Inventory

| Endpoint | Method | Purpose | Status |
|---|--------|---|---|
| `/GetOTPChecking` | POST   | Authentication | Partially understood |
| `/getstudWBalinfo` | GET    | Student/wallet info | Understood |
| `/getprefers` | POST   | Meal preferences | Partially understood |
| `/getmenugroup` | POST   | Food groups | Understood |
| `/Getcatlistnew` | POST   | Food outlets/categories | Understood |
| `/GetOptionMenuItems` | POST   | Menu items | Mostly understood |
| `/getqoh` | GET    | Product stock | Understood |
| `/getexgstval` | GET    | GST/tax-related value | Partially understood |
| `/chkuserstat` | POST   | User/session status | Unknown |
| `/GetOrderIdDetails` | POST   | Order number | Partially understood |
| `/GetOutletListInsert` | POST   | Build/submit order | Mostly understood |
| `/OnlineWPayment` | POST   | Payment | Partially understood |
| `/GetOrderList` | GET    | Order history | Understood |
| `/orderQR` | GET    | Order QR | Mostly understood |
| `/getoffers` | POST   | Offers/discounts | Unknown |

---

# 15. Current End-to-End Understanding

```text
Authentication
     |
     v
GetOTPChecking
     |
     v
Student information
     |
     v
getstudWBalinfo
     |
     v
Menu structure
     |
     +--> getmenugroup
     +--> Getcatlistnew
     |
     v
Menu items
     |
     v
GetOptionMenuItems
     |
     v
Stock / tax checks
     |
     +--> getqoh
     +--> getexgstval
     |
     v
Generate order number
     |
     v
GetOrderIdDetails
     |
     v
Build order
     |
     v
GetOutletListInsert
     |
     v
Payment
     |
     v
OnlineWPayment
     |
     v
Order history
     |
     v
GetOrderList
     |
     v
QR
     |
     v
orderQR
```

---

# 16. Agent Development Notes

When implementing an API client from this document:

1. Treat endpoint names and observed request/response structures as authoritative.
2. Do not invent meanings for fields marked `Unknown`.
3. Preserve unknown fields in raw response models where practical.
4. Use typed models for confirmed fields.
5. Keep API-specific models separate from UI/domain models.
6. Never hardcode real student information.
7. Never hardcode OTPs, session cookies, Cloudflare cookies, or payment references.
8. Authentication/session handling is incomplete and must be treated as a TODO.
9. When discovering a new endpoint, document:
    - HTTP method
    - endpoint
    - parameters/request body
    - observed response
    - field descriptions
    - confidence
    - relationships to other endpoints
10. Do not upgrade an `Unknown`, `Observed`, or `Inferred` claim to `Confirmed` without evidence.

---

# 17. Open Questions / TODO

## Authentication

- [ ] Determine successful `GetOTPChecking` response.
- [ ] Determine exactly how login state is established.
- [ ] Determine whether authentication depends entirely on `ASP.NET_SessionId`.
- [ ] Determine what `logid` represents.
- [ ] Determine what `ouid` represents.
- [ ] Determine which cookies/headers are required by subsequent requests.

## Menu

- [ ] Determine parameters sent with `GetOptionMenuItems`, if any.
- [ ] Determine `mbq`.
- [ ] Determine `ord`.
- [ ] Determine `icat`.
- [ ] Determine `optex`.
- [ ] Determine `tb`.
- [ ] Determine `fltr`.
- [ ] Determine `chk`.

## Ordering

- [ ] Determine exact meaning of `TableNo`.
- [ ] Determine exact meaning of `MobileNo` in order APIs.
- [ ] Confirm `productId` construction.
- [ ] Determine whether `GetOrderIdDetails` creates or retrieves an order.
- [ ] Determine whether `GetOutletListInsert` stages or finalizes an order.
- [ ] Determine `flag`.
- [ ] Determine `optcls`.

## Payment

- [ ] Determine complete payment flow after `OnlineWPayment`.
- [ ] Determine exact response status semantics.
- [ ] Determine payment-reference semantics.
- [ ] Identify any follow-up payment endpoints.

## Orders / QR

- [ ] Determine all possible `Status` values.
- [ ] Determine all possible `CancelStatus` values.
- [ ] Determine exact meaning of `qrstat`.
- [ ] Document exact `orderQR` response format.
- [ ] Confirm Base64/image format.
- [ ] Document QR decoding process.

## Miscellaneous

- [ ] Determine `chkuserstat` response.
- [ ] Determine `getprefers.opt`.
- [ ] Determine whether `getoffers` is active.
- [ ] Determine `DocumentNo`.
- [ ] Determine `SessionNo`.
- [ ] Identify remaining APIs for cart, cancellation, scheduled orders, notifications, etc.

---

# 18. Security / Privacy

Never commit:

```text
ASP.NET_SessionId
cf_clearance
OTP values
passwords/PINs
real payment references
authentication tokens
unnecessary personal information
```

Use placeholders:

```text
<APPLICATION_NUMBER>
<REGISTRATION_NUMBER>
<USER_IDENTIFIER>
<OTP>
<SESSION_ID>
<CLOUDFLARE_COOKIE>
<ORDER_NUMBER>
<PAYMENT_REFERENCE>
```

Sanitize captured network data before committing it to the repository.

---

# 19. Documentation Convention

For every newly discovered endpoint:

```md
## endpointName

**Method:** `GET` / `POST`

**Endpoint:**

```text
/api/endpointName
```

### Purpose

What the endpoint appears to do.

### Request

```json
{
  "field": "<VALUE>"
}
```

### Response

```json
{
  "field": "<VALUE>"
}
```

### Fields

| Field | Description | Confidence |
|---|---|---|
| `field` | Description | Confirmed / Observed / Inferred / Unknown |

### Notes

Anything that still needs verification.
```

---

# 20. Confidence Legend

- **Confirmed** — directly supported by repeated observation or an obvious API relationship.
- **Observed** — value/behavior was seen, but exact semantic meaning is not proven.
- **Inferred** — likely meaning based on surrounding API behavior, but not directly confirmed.
- **Unknown** — meaning has not been established.

Do not treat `Observed`, `Inferred`, or `Unknown` fields as confirmed API contracts.
