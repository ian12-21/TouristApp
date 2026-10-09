# Firestore Schema Contract

**This document is the single source of truth for the Firestore data shape.**

Two codebases read and write these documents:

| Repo | Role | Model file |
|---|---|---|
| `tourist-admin` (Angular) | Owner-facing admin web app. **Writes** almost everything. | `src/app/core/models/models.ts` |
| `TouristApp` (Kotlin/Compose) | Guest-facing kiosk tablet. **Reads** everything, writes only reviews. | `app/src/main/java/com/touristapp/data/model/Models.kt` |

Firestore is schemaless, so nothing enforces agreement between them. A field renamed
in one repo does not fail to compile in the other — it silently reads as `null`/`""`
at runtime, in production, on a tablet mounted on someone's wall.

**Rule: change this file first, then both repos.** If a PR changes a document shape
without touching `SCHEMA.md`, that PR is incomplete.

Keep this file identical in both repos.

---

## 1. Conventions

### 1.1 Field naming

`camelCase` for all field names.

> ⚠️ **Known exception — do not "fix" casually.** `apartments.transportation[].transportation_id`
> is snake_case. It is written by the admin app (`TransportationItem.transportation_id`)
> and read by the tablet (`map["transportation_id"]`). Renaming it requires a coordinated
> change in **both** repos **plus** a data migration of existing `apartments` documents.
> See §5.1.

### 1.2 Localized fields

Any field marked **`Localized`** below is stored as a map, never a bare string:

```json
{ "en": "Welcome!", "hr": "Dobrodošli!", "it": "Benvenuti!", "de": "Willkommen!" }
```

- `en` is **required** and is the fallback for every other language.
- `hr`, `it`, `de` are optional. Missing or blank ⇒ fall back to `en`.
- Supported codes are exactly `en | hr | it | de`.

**Legacy tolerance:** some older documents store a plain `string` instead of a map.
Both repos accept this and treat it as `{ en: <value> }`. Do not write this format for
new data.

| Repo | Read helper | Write helper |
|---|---|---|
| Angular | `localizeValue(val, lang)` in `core/utils/localize.ts` | `toLocalizedString(val)` |
| Kotlin | `localize(raw, lang)` in `core/i18n/Localization.kt` | n/a (read-only) |

Kotlin models declare localized fields as `String` with `@get:Exclude`, because
Firestore's `toObject()` would otherwise try to coerce the map into a `String` and throw.
They are resolved manually in `TouristRepositoryImpl`.

### 1.3 Timestamps

`Timestamp` = Firestore native timestamp. Written server-side via `serverTimestamp()`
from the admin app. Kotlin reads them as nullable (`Timestamp?`) because a document
read back immediately after a write can briefly have `null` in that field.

### 1.4 Document IDs

The `id` field is **not stored in the document**. Both repos inject the Firestore
document ID into the model after reading (`idField: 'id'` in Angular, `.copy(id = doc.id)`
in Kotlin). Never write an `id` field into a document body.

---

## 2. Data layout & access model

**Every owner's data lives under their own subtree** (multi-owner):

```
owners/{ownerId}                         profile doc — ID is the owner's Auth uid,
                                         created when we provision the owner (§3.8)
owners/{ownerId}/apartments/{apartmentId}       (+ rooms subcollection)
owners/{ownerId}/places/{placeId}
owners/{ownerId}/stays/{stayId}
owners/{ownerId}/guests/{guestId}
owners/{ownerId}/transportation/{serviceId}
owners/{ownerId}/reviews/{reviewId}
emergency_contacts/{groupId}             shared by all owners (NOT per-owner)
admins/{uid}                             superadmin allowlist, console-only
```

No client can create `owners/{ownerId}` or `admins/{uid}` — no rule grants the write.
Owners are provisioned from outside the app; the procedure is in the `tourist-admin`
README ("Owner provisioning").

The collection paths in §3 are relative to `owners/{ownerId}/`, except
`emergency_contacts`. Storage mirrors this: `owners/{ownerId}/<folder>/...`.

Who builds the `{ownerId}`:
- **Admin web app** — `AuthService.ownerPath()` (the signed-in owner's uid).
- **Tablet** — `AppPreferences.getOwnerId()`, saved when the owner pairs the tablet.

Two kinds of authenticated client, enforced by `firestore.rules`:

- **Owner** — email/password sign-in. Read + write **only inside `owners/{theirUid}/`**.
  `isTenant(ownerId)` = signed-in uid equals `{ownerId}` in the path AND the sign-in
  provider is `password` AND `owners/{ownerId}` exists (so a self-registered account is
  not an owner).
- **Tablet** — anonymous sign-in (`signInAnonymously()`). Read-only, except reviews, and
  review writes are accepted **only** from the `anonymous` provider (§3.7).
- **Superadmin** (us) — an account with an `admins/{uid}` doc. The only writer of the
  shared `emergency_contacts` collection, and may read any owner profile.

There is deliberately **no catch-all `match /{document=**}` rule**. Every collection is
listed explicitly in `firestore.rules`; anything not listed is denied by default.

> **When you add a collection, you must add a rule for it or it will be unreadable.**

---

## 3. Collections

### 3.1 `apartments/{apartmentId}`

The central document. Read by both apps; written only by the owner.

| Field | Type | Notes |
|---|---|---|
| `name` | `string` | Display name. |
| `address` | `string` | |
| `description` | **Localized** | |
| `coordinates` | `{ lat: number, lng: number }` | Used for the weather lookup. Missing ⇒ tablet skips weather entirely. |
| `photos` | `string[]` | Storage download URLs. |
| `size` | `string` | Free text, e.g. `"65 m²"`. |
| `capacity` | `number` | Defaults to `4` on create. |
| `renovationYear` | `number` | |
| `wifiName` | `string` | |
| `wifiPassword` | `string` | Shown in plain text on the tablet by design. |
| `checkoutTime` | `string` | Free text, e.g. `"10:00"`. |
| `checkoutInstructions` | **Localized** | |
| `welcomeMessage` | **Localized** | |
| `houseRules` | `HouseRuleGroup[]` | See §4.1. |
| `countryCode` | `string` | The country the numbers are for: ISO 3166-1 alpha-2, upper case (`HR`). Required by the rules. The admin shows its flag and localized name; the tablet does not read it. |
| `contacts` | `Contact[]` | See §4.2. Names are `LocalizedString`s, translated by the superadmin. |
| `transportation` | `TransportationItem[]` | See §4.3. |
| `currentStayId` | `string \| null` | FK → `stays`. `null` when vacant. |
| `emergencyContactGroupId` | `string \| null` | FK → `emergency_contacts`. |
| `updatedAt` | `Timestamp` | Server-set on every write. |

**Rules:** `get` if signed in; `list` and write owner-only (a tablet fetches its apartment
by ID and never enumerates). The `rooms` subcollection is read if signed in, write if owner.

Owner writes are shape-checked (`validApartment()`): every field above must have the type
listed when present (**Localized** = map, or the legacy string), `capacity` /
`renovationYear` may be `null`, and `coordinates` must hold numeric `lat` / `lng`. A new
apartment must carry `name`, `photos`, `houseRules`, `contacts`, `transportation`,
`currentStayId` and `updatedAt`; an update cannot remove them. The `rooms` body is not
validated.

#### `apartments/{apartmentId}/rooms/{roomName}`

Subcollection. **The document ID is the room's display name** (e.g. `Kitchen`), not a
generated ID.

The document body is a **map of appliance name → `Appliance`**, with the appliance name
as the key. There is no wrapper field:

```json
// apartments/abc123/rooms/Kitchen
{
  "Dishwasher": { "description": {...}, "instructions": {...}, "images": [], "icon": "dishwasher" },
  "Oven":       { "description": {...}, "instructions": {...}, "images": [], "icon": "oven" }
}
```

Both repos defensively skip any top-level value that isn't an object, so adding a scalar
field here later will not crash — but it will be silently ignored.

`Appliance`: `description` **Localized**, `instructions` **Localized**, `images` `string[]`, `icon` `string`.

---

### 3.2 `places/{placeId}`

Points of interest near an apartment.

| Field | Type | Notes |
|---|---|---|
| `name` | `string` | Not localized — proper nouns. |
| `category` | `string` enum | See §4.4. |
| `description` | **Localized** | |
| `address` | `string` | |
| `images` | `string[]` | |
| `thumbImageUrl` | `string` | Card thumbnail. |
| `tips` | **Localized** | |
| `phone` | `string` | |
| `isActive` | `boolean` | Inactive places are hidden from the tablet. |
| `apartments` | `ApartmentLink[]` | Per-apartment distance. See §4.5. |
| `apartmentIds` | `string[]` | **Denormalized** — see below. |
| `createdAt` | `Timestamp` | ⚠️ Written by admin; **not present in the Kotlin model**. Harmless, but the tablet cannot sort by it. |

> **Denormalization contract:** `apartmentIds` must always contain exactly the
> `apartmentId` values present in `apartments[]`. It exists solely so the tablet can run
> `whereArrayContains("apartmentIds", id)` — Firestore cannot query inside an array of
> objects. **If you write `apartments[]`, you must rewrite `apartmentIds` in the same
> operation** or the place silently disappears from the tablet.

**Rules:** read if signed in; write if owner. Owner writes are shape-checked
(`validPlace()`): field types as above, `category` must be one of §4.4, and `apartments`
and `apartmentIds` must have the same length. A new place must carry `name`, `category`,
`isActive`, `apartments` and `apartmentIds`.

---

### 3.3 `stays/{stayId}`

A group of guests occupying an apartment for a date range.

| Field | Type | Notes |
|---|---|---|
| `guestIds` | `string[]` | FK → `guests`. Order is meaningful — the tablet renders guests in this order. |
| `guestNames` | `Record<guestId, string>` | **Denormalized** — see below. |
| `apartmentId` | `string` | FK → `apartments`. |
| `checkIn` | `Timestamp` | |
| `checkOut` | `Timestamp` | |
| `welcomeMessage` | **Localized** | Overrides the apartment's. |
| `notes` | **Localized** | |
| `status` | `'active' \| 'upcoming' \| 'completed'` | |
| `createdAt` | `Timestamp` | |

> **Denormalization contract — this one is load-bearing for privacy.** The `guests`
> collection is owner-only (it holds email and phone). The tablet is anonymous and
> **cannot read it at all**. It gets display names exclusively from `stays.guestNames`.
>
> When a guest is renamed, the admin app must propagate the new name into every stay
> referencing them — `StayService.syncGuestName()` does this via a batched dot-path
> update. **Skip that and the tablet shows a stale name forever.**

**Invariant:** `apartments/{apartmentId}.currentStayId` and `stays/{stayId}.apartmentId`
point at each other. Both `checkIn()` (transaction) and `checkOut()` (batch) in
`StayService` update the pair atomically. Never set one without the other.

**Overlap rule:** two stays for the same apartment with status `active` or `upcoming`
may not overlap in date range. Enforced client-side inside `StayService.checkIn()`'s
transaction — **not** enforced by security rules.

**Rules:** `get` by the owner, or by any signed-in client **while `status` is `active`**
(the tablet only reads the current stay; past stays stay private); `list` and write
owner-only — an enumerable `stays` collection would be a roster of every guest who ever
stayed. Owner writes are
shape-checked (`validStay()`): field types as above, `guestIds` non-empty, `status` one of
the three values. A new stay must carry `guestIds`, `guestNames`, `apartmentId`,
`checkIn`, `checkOut` and `status`, and its `apartmentId` must be an apartment of the same
owner (re-checked on update only if `apartmentId` changes).

> **`status` and `apartments.currentStayId` gate reviews.** A tablet can write a review
> only while the stay is `active` *and* is its apartment's `currentStayId` (§3.7). Check-out
> flips both, which closes the stay's reviews for editing.

---

### 3.4 `guests/{guestId}`

**Private.** Owner-only read *and* write. Never exposed to tablets.

| Field | Type | Notes |
|---|---|---|
| `name` | `string` | Mirrored into `stays.guestNames`. |
| `email` | `string` | PII. |
| `phone` | `string` | PII. |
| `language` | `string` | One of `en \| hr \| it \| de`. |
| `active` | `boolean` | |
| `createdAt` | `Timestamp` | |

---

### 3.5 `transportation/{serviceId}`

Private transfer providers (taxi, shuttle).

**Current format:**

| Field | Type | Notes |
|---|---|---|
| `name` | `string` | |
| `phone` | `string` | |
| `description` | **Localized** | |
| `thumbImageUrl` | `string?` | Optional. |

> ⚠️ **Legacy format still in the wild.** Older documents nest everything under the
> service name as the key: `{ "Ivan's Taxi": { phone, description } }`. Both repos detect
> this by checking for the presence of a top-level `name` field and fall back to reading
> `entries.first()`. Documents migrate to the flat format on next save from the admin app.
> **Do not delete the legacy branch until you have confirmed zero legacy documents remain.**

**Rules:** read if signed in; write if owner.

---

### 3.6 `emergency_contacts/{groupId}`

A named group of emergency numbers, referenced by `apartments.emergencyContactGroupId`.
**Shared reference data:** one top-level collection for all owners, not under
`owners/{ownerId}/`. An owner picks a group for an apartment but cannot change the groups.

| Field | Type | Notes |
|---|---|---|
| `countryCode` | `string` | The country the numbers are for: ISO 3166-1 alpha-2, upper case (`HR`). Required by the rules. The admin shows its flag and localized name; the tablet does not read it. |
| `contacts` | `Contact[]` | See §4.2. Names are `LocalizedString`s, translated by the superadmin. |

> ⚠️ **Legacy tolerance:** the phone field may be `phone` **or** `number`. Both repos read
> `phone ?? number`. Always write `phone`.

The collection was called `emergency_contacts_croatia` before groups carried a country;
documents under the old name are not read by either app.

**Rules:** read if signed in; write superadmin only (`admins/{uid}`), and a write must
carry a valid `countryCode` and a `contacts` list. An account may `get` its own
`admins/{uid}` entry — that is how the admin decides whether to show the edit controls;
nobody can list or write the allowlist. An owner's own extra numbers belong in
`apartments.contacts`, not here.

---

### 3.7 `reviews/{reviewId}`

**The only collection tablets write to.** Created by anonymous guests.

**The document ID is `{stayId}_{guestId}`** — one review per guest per stay. Rules reject
any other ID on create, so `add()` cannot be used. Reviews written before this rule keep
their random IDs.

| Field | Type | Notes |
|---|---|---|
| `apartmentId` | `string` | FK → `apartments`. **Rule-enforced:** must be the stay's `apartmentId`. |
| `stayId` | `string` | FK → `stays`. **Rule-enforced:** must exist, be `active`, and be the apartment's `currentStayId`. |
| `guestId` | `string` | FK → `guests` (ID only — the tablet never reads that collection). **Rule-enforced:** must be in the stay's `guestIds`. |
| `guestName` | `string` | Denormalized from `stays.guestNames`. **Rule-enforced** on create: must equal `stay.guestNames[guestId]`. |
| `authorUid` | `string` | Firebase uid of the tablet that created the review. Set by `TouristRepositoryImpl.createReview`, **never** by the UI. Immutable, and the sole gate on who may edit the document later. Absent on reviews predating this field — those are owner-editable only. |
| `cleanliness` | `number` | 1–10. |
| `location` | `number` | 1–10. |
| `comfort` | `number` | 1–10. |
| `valueForMoney` | `number` | 1–10. |
| `facilities` | `number` | 1–10. |
| `communication` | `number` | 1–10. |
| `wifi` | `number` | 1–10. |
| `overallScore` | `number` | Mean of the seven scores, rounded to one decimal. **Rule-enforced** against the scores (±0.05). |
| `comment` | `string` | **Rule-enforced max 500 chars.** |
| `doodleBase64` | `string?` | Guest signature drawing, stored inline as base64 — *not* in Storage, because tablets are anonymous and Storage denies anonymous writes. |
| `createdAt` | `Timestamp` | `FieldValue.serverTimestamp()`. **Rule-enforced:** must be the server time; immutable. |
| `updatedAt` | `Timestamp` | `FieldValue.serverTimestamp()` on every write. **Rule-enforced:** must be the server time. |

> 🔒 **This collection's shape is enforced by security rules, not just convention.**
> `firestore.rules` has a `validReview()` function with `hasOnly([...])` listing every
> permitted field. **Adding a field to this table without adding it to that list will make
> every write fail with `PERMISSION_DENIED`.** Rules must never trust client validation.

**Rules:** read if signed in. Writes other than delete are accepted only from an
**anonymous** sign-in:

- **create** — payload passes `validReview()`, `authorUid == request.auth.uid`, the document
  ID is `{stayId}_{guestId}`, both timestamps are the server time, the stay is the
  apartment's current active stay, and the guest is on that stay under that name.
- **update** — the caller's uid matches the stored `authorUid`, the stay is still current,
  `updatedAt` is the server time, and only the seven scores, `overallScore`, `comment`,
  `doodleBase64` and `updatedAt` change. Everything else is immutable.
- **delete** — owner only. **Owners cannot edit a review**; moderation means deleting it.

> ⚠️ **`isSignedIn()` is not authorization on this collection.** Tablets authenticate with
> `signInAnonymously()`, which hands a fresh uid to anyone holding the API key (extractable
> from the .apk), so "is signed in" is free to obtain. `authorUid` is what actually binds a
> review to a writer. Note it identifies the **tablet**, not the guest: guests sharing a
> kiosk can technically edit each other's reviews, which is accepted because the UI only
> surfaces the review matching the current `guestId` + `stayId`. A review without
> `authorUid` (written before the field existed) cannot be edited by anyone.

### 3.8 `owners/{ownerId}` (profile)

Not under the owner subtree — this is the root doc the subtree hangs from. Its
**existence** is what makes an Auth account an owner (`isTenant()`); the admin app reads
it once on entry (`OwnerService.load`) and shows an "account not activated" page when
it is missing. The tablet never reads it.

| Field | Type | Notes |
|---|---|---|
| `name` | `string` | Shown in the admin sidebar. |
| `email` | `string` | Copy of the Auth email, for display. |
| `createdAt` | `Timestamp` | Optional on docs created by hand. |

**Rules:** `get` by that owner (or a superadmin); `list` superadmin only; no client
writes. A missing doc therefore reads as `permission-denied`, not as "not found".

---

## 4. Shared shapes

### 4.1 `HouseRuleGroup`
`title` **Localized**, `rules` **Localized[]** (an array whose *elements* are each localized maps).

### 4.2 `Contact`
`name` **Localized**, `phone` `string`.

### 4.3 `TransportationItem`
`type` `'public' | 'private' | 'info'`, `description` **Localized**,
`transportation_id` `string` (snake_case — see §1.1; empty for `public`/`info`, FK → `transportation` for `private`).

### 4.4 `PlaceCategory`
`beach | restaurant | cafe | store | pharmacy | attraction | nightlife | activity`

Stored as a plain string. Adding a value requires updating the Angular union type, the
Kotlin `PlaceCategory` enum, **and** the tablet's category icons — an unknown category
will not render.

### 4.5 `ApartmentLink`
`apartmentId` `string`, `distance` `number` (**total minutes**, not km),
`distanceType` `'walk' | 'car' | 'bus'`.

---

## 5. Known drift

Tracked deliberately. Each item is a real inconsistency, not a bug to fix blindly.

### 5.1 `transportation_id` casing
Snake_case in an otherwise camelCase schema. Fixing requires: Angular model + template +
component, Kotlin repository mapping, and a backfill over every `apartments` document
rewriting the `transportation` array. Low value, non-trivial risk. Left alone intentionally.

### 5.2 `Place.createdAt` missing from the Kotlin model
Admin writes it; the tablet's `Place` data class omits it. Currently harmless — the tablet
never sorts places by creation date. Add the field if that changes.

### 5.3 Two legacy read paths
`transportation` (nested format) and `emergency_contacts` (`number` vs `phone`).
Both are handled in both repos. Each is safe to delete only after verifying no remaining
documents use the old shape.

### 5.4 Schema validation on write is shallow
`reviews` is fully validated by security rules. `apartments`, `places` and `stays` get a
basic check — required keys on create and top-level field types (§3.1–3.3) — so an admin
bug can no longer store, say, a string where the tablet expects a list. Not checked: the
*contents* of arrays and maps (`houseRules`, `contacts`, `transportation`, `apartments[]`,
localized maps), the `rooms` subcollection, `guests` and `transportation`. A malformed
element inside one of those still reaches the tablet.

---

## 6. Checklist: adding a field

1. Update this file.
2. Update `models.ts` (Angular).
3. Update `Models.kt` (Kotlin) — add `@get:Exclude` if the field is **Localized**, and
   resolve it manually in `TouristRepositoryImpl`.
4. If the field is on `reviews`, add it to `validReview()`'s `hasOnly([...])` in
   `firestore.rules` **and deploy the rules**, or writes will start failing. If the tablet
   may edit it, add it to the `affectedKeys()` list on `allow update` too. If it is on
   `apartments`, `places` or `stays`, add a type check to that collection's `valid…()`
   function.
5. If it's a new collection, add an explicit `match` block to `firestore.rules`.
6. Decide what existing documents without the field should do. Both repos default to
   `""` / `emptyList()` / `null` — confirm that is actually acceptable for this field.
