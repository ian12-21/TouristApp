# Tester guide

*Hrvatska verzija: [tester-guide.hr.md](tester-guide.hr.md)*

Thank you for testing. The system has two parts:

- the **web admin** — a website where you enter everything about your apartment;
- the **tablet app** — an Android app that shows it to your guests.

You set up the web admin first, then the tablet. Allow about an hour for the first apartment.

## 1. Sign in to the web admin

1. Open **https://tourist-app-staging.web.app** in a browser on your computer.
2. We created an account for your email address, but it has no password yet. Type your
   email, then click **Forgot password?**
3. Open the email you receive, follow the link and choose a password.
4. Go back to the web admin and sign in with your email and the new password.

The language selector is at the bottom of the menu on the left (English, Croatian, Italian,
German). This guide uses the English labels.

If you see **Account not activated**, the account is not finished on our side — write to us.

Please read the **Privacy notice** linked on the sign-in page before you enter guest data.

## 2. Set up your apartment

The dashboard shows the same steps as a checklist.

### Create the apartment

**Apartments → + Add apartment.** Fill in the name, address, size, capacity, Wi-Fi name and
password, checkout time and the texts for your guests.

- Texts such as the description and the welcome message can be entered in four languages.
  Always fill in English: a language you leave empty shows the English text.
- **Latitude** and **Longitude** are needed for the weather on the tablet. Without them the
  tablet shows no weather.
- The **Wi-Fi password** is shown on the tablet in plain text. Use a guest network.

Open the apartment (**Details →**) to continue. It has six tabs: **Overview**,
**Contacts & Rules**, **Rooms**, **Transportation**, **Places** and **Reviews**.

Photos are public: anyone who has the link can open them. Do not upload documents or IDs.

### Rooms and appliances

**Rooms** tab **→ + Add room**, give it a name (Kitchen, Bedroom…), then **+ Add appliance**
for each device a guest might need help with: name, description, instructions, icon.
Save the room before uploading appliance photos.

### Places

**Places** (in the menu) **→ + Add place**: name, category, description, tips, phone.

Under **Linked apartments**, select your apartment and enter how far away the place is, in
minutes on foot, by car or by bus. **A place that is not linked to an apartment does not
appear on that apartment's tablet.** A place set to inactive is hidden too. To add photos,
save the place first and open it again.

### Transportation

1. **Transportation** (in the menu) **→ + Add service** for each private provider, such as
   a taxi or a transfer: name, phone, description.
2. In the apartment, **Transportation** tab **→ + Add**. Choose **Public** or **Info** and
   write the text ("Bus stop 200 m down the street"), or choose **Private** and pick one of
   the services from step 1.

### Contacts, emergency numbers and house rules

All three are on the apartment's **Contacts & Rules** tab.

- **Contacts** — your own numbers (you, the cleaner, maintenance).
- **Emergency services** — choose a group under **Assigned contact group** and click
  **Save**. The groups (police, ambulance…) are shared by all owners and maintained by us.
  The **Emergency Contacts** page in the menu lists the groups by country; you can look
  but not change them — use the picker on the apartment. If the list is empty, tell us.
- **House rules** — **+ Add group** (for example "Quiet hours"), then the rules in it.

## 3. Add a guest and check in a stay

1. **Guests → + Add guest.** Only the name and language are needed; email and phone are
   optional and never shown on the tablet.
2. **Stays → + New stay.** Pick the apartment, tick the guests, set the check-in and
   check-out dates, and optionally a welcome message and notes — guests can read both.
   Click **Check in**.

The tablet greets the guests of the current stay by name, and they can leave a review.
When they leave, click **Check out** on the stay.

## 4. Install the tablet app

**You need:** an Android tablet with **Android 8 or newer** and an internet connection.

1. Get the app file (APK): use the **Download** button on the **Tablet setup** page of the
   web admin, or the link we sent you. Open it on the tablet, or copy the file to the tablet.
2. Open the downloaded file. Android asks whether to allow installing apps from this
   source (your browser or file manager) — allow it, then tap **Install**.
3. Open the installed app.

## 5. Pair the tablet with your apartment

1. On first start the app shows a sign-in screen. Sign in with the **same email and
   password** as in the web admin.
2. Tap the apartment this tablet belongs to.

The tablet now shows that apartment. Your sign-in is not kept on the tablet.

**To pick a different apartment later:** press and hold the **home icon in the top-left
corner for 5 seconds**, let go, sign in and tap **Reconfigure apartment**. After three
wrong passwords the dialog closes and will not open again for one minute.

**When you change something in the web admin,** the tablet picks it up the next time the
app comes back to the screen — turn the tablet's screen off and on, or close and reopen the
app. It does not update while you watch.

## 6. Kiosk mode (optional)

Kiosk mode locks the tablet to the app: guests cannot go to the home screen, switch apps
or open the notification shade. The tablet works without it; use it if the tablet stays in
the apartment unattended.

It needs a computer with **ADB** (Android platform tools) and a **factory reset, which
erases everything on the tablet.** Do it before pairing.

1. Factory reset the tablet. During the first-start setup, **skip adding a Google account**.
2. Turn on **Developer options → USB debugging** (Settings → About tablet → tap *Build
   number* seven times to show Developer options) and connect the tablet to the computer.
3. Install the app from the computer:

   ```
   adb install tourist-app.apk
   ```

4. Make the app the device owner:

   ```
   adb shell dpm set-device-owner com.touristapp.staging/com.touristapp.admin.KioskAdminReceiver
   ```

5. Open the app and pair it with your apartment (section 5).
6. Hold the home icon for 5 seconds, sign in and tap **Enable kiosk mode**.

Kiosk mode stays on after a restart.

**Getting back out:** hold the home icon for 5 seconds, sign in and tap **Exit kiosk mode**.
The tablet behaves normally again, and you can enable kiosk mode again the same way. To
undo the setup completely, factory reset the tablet. (The Tablet setup page mentions a
"Remove kiosk completely" option; this version of the app does not have it.)

## 7. What to test

On the web admin:

- [ ] Set your password with **Forgot password?** and sign in
- [ ] Create an apartment with photos, Wi-Fi and checkout time
- [ ] Add at least two rooms with appliances
- [ ] Add a few places in different categories and link them to the apartment
- [ ] Add public and private transport
- [ ] Pick an emergency contact group, add your own contacts and house rules
- [ ] Add guests, check in a stay, later check it out
- [ ] Switch the admin to another language

On the tablet:

- [ ] Install and pair
- [ ] The apartment, rooms, places, transport, emergency numbers and house rules look right
- [ ] The guests of the stay are greeted by name; the checkout date and time are right
- [ ] Switch the language — texts you translated change, the rest shows English
- [ ] Write a review as a guest, then edit it
- [ ] The review appears on the apartment's **Reviews** tab in the web admin
- [ ] Change something in the web admin and check it reaches the tablet
- [ ] Restart the tablet; turn Wi-Fi off and on again
- [ ] If you use kiosk mode: enable it, restart the tablet, exit it

We also want to hear what was confusing, slow or missing, not only what broke.

## 8. Reporting a problem

Write to the person who invited you. It helps a lot if you include:

- your account email and the apartment's name;
- whether it happened in the web admin or on the tablet;
- what you did, what you expected and what happened instead;
- the date and time;
- a screenshot, or a photo of the tablet;
- for the tablet: the model and the Android version.

## 9. Your guests' data

- Enter only the guest details you need.
- Tell your guests that their name appears on the tablet and that reviews are stored and
  shown to later guests.
- To have data deleted — yours or a guest's — write to us. Guests, stays and reviews are
  deleted no later than 30 days after the test ends.
