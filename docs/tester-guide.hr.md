# Vodič za testere

*English version: [tester-guide.md](tester-guide.md)*

Hvala vam što testirate. Sustav ima dva dijela:

- **web administraciju** — web-stranicu na kojoj unosite sve o svom apartmanu;
- **aplikaciju za tablet** — Android aplikaciju koja to prikazuje vašim gostima.

Prvo postavljate web administraciju, zatim tablet. Za prvi apartman računajte na oko sat vremena.

## 1. Prijava u web administraciju

1. Na računalu u pregledniku otvorite **https://tourist-app-staging.web.app**.
2. Za vašu adresu e-pošte otvorili smo račun, ali on još nema lozinku. Upišite e-poštu pa
   kliknite **Zaboravljena lozinka?**
3. Otvorite poruku koju dobijete, slijedite poveznicu i odaberite lozinku.
4. Vratite se u web administraciju i prijavite se e-poštom i novom lozinkom.

Odabir jezika nalazi se na dnu izbornika s lijeve strane (engleski, hrvatski, talijanski,
njemački). Ovaj vodič koristi hrvatske nazive.

Ako vidite poruku **Račun nije aktiviran**, račun s naše strane nije dovršen — javite nam se.

Prije unosa podataka o gostima pročitajte **Obavijest o privatnosti**, do koje vodi
poveznica na stranici za prijavu.

## 2. Postavljanje apartmana

Nadzorna ploča prikazuje iste korake kao popis.

### Kreiranje apartmana

**Apartmani → + Dodaj apartman.** Unesite naziv, adresu, veličinu, kapacitet, naziv i
lozinku WiFi mreže, vrijeme odjave i tekstove za goste.

- Tekstovi poput opisa i poruke dobrodošlice mogu se unijeti na četiri jezika. Engleski
  uvijek ispunite: za jezik koji ostavite praznim prikazuje se engleski tekst.
- **Geografska širina** i **dužina** potrebne su za vremensku prognozu na tabletu. Bez njih
  tablet ne prikazuje vrijeme.
- **Lozinka WiFi mreže** prikazuje se na tabletu kao običan tekst. Koristite mrežu za goste.

Za nastavak otvorite apartman (**Detalji →**). Ima šest kartica: **Pregled**,
**Kontakti i pravila**, **Sobe**, **Prijevoz**, **Mjesta** i **Recenzije**.

Fotografije su javne: može ih otvoriti svatko tko ima poveznicu. Ne učitavajte dokumente
ni osobne iskaznice.

### Sobe i uređaji

Kartica **Sobe → + Dodaj sobu**, upišite naziv (Kuhinja, Spavaća soba…), zatim
**+ Dodaj uređaj** za svaki uređaj oko kojeg bi gostu mogla zatrebati pomoć: naziv, opis,
upute, ikona. Sobu spremite prije učitavanja fotografija uređaja.

### Mjesta

**Mjesta** (u izborniku) **→ + Dodaj mjesto**: naziv, kategorija, opis, savjeti, telefon.

Pod **Povezani apartmani** odaberite svoj apartman i upišite koliko je mjesto udaljeno, u
minutama pješice, autom ili busom. **Mjesto koje nije povezano s apartmanom ne prikazuje se
na tabletu tog apartmana.** Skriveno je i mjesto označeno kao neaktivno. Za dodavanje
fotografija mjesto prvo spremite pa ga ponovno otvorite.

### Prijevoz

1. **Prijevoz** (u izborniku) **→ + Dodaj uslugu** za svakog privatnog prijevoznika, npr.
   taksi ili transfer: naziv, telefon, opis.
2. U apartmanu, kartica **Prijevoz → + Dodaj**. Odaberite **Javni** ili **Info** i upišite
   tekst („Autobusna stanica 200 m niz ulicu”), ili odaberite **Privatni** pa jednu od
   usluga iz 1. koraka.

### Kontakti, hitni brojevi i kućni red

Sve troje nalazi se na kartici **Kontakti i pravila** u apartmanu.

- **Kontakti** — vaši brojevi (vi, čistačica, održavanje).
- **Hitne službe** — pod **Dodijeljena kontaktna grupa** odaberite grupu i kliknite
  **Spremi**. Grupe (policija, hitna pomoć…) zajedničke su svim vlasnicima i održavamo ih
  mi. Stranica **Hitni kontakti** u izborniku prikazuje grupe po državama; možete ih
  pregledati, ali ne i mijenjati — koristite odabir na apartmanu. Ako je popis prazan,
  javite nam.
- **Kućni red** — **+ Dodaj grupu** (npr. „Vrijeme tišine”), zatim pravila u njoj.

## 3. Dodavanje gosta i prijava boravka

1. **Gosti → + Dodaj gosta.** Potrebni su samo ime i jezik; e-pošta i telefon nisu obavezni
   i nikad se ne prikazuju na tabletu.
2. **Boravci → + Novi boravak.** Odaberite apartman, označite goste, postavite datume
   prijave i odjave te po želji poruku dobrodošlice i napomene — gosti mogu pročitati oboje.
   Kliknite **Prijava**.

Tablet goste trenutnog boravka pozdravlja imenom i oni mogu ostaviti recenziju. Kada odu,
na boravku kliknite **Odjava**.

## 4. Instalacija aplikacije na tablet

**Treba vam:** Android tablet s **Androidom 8 ili novijim** i internetskom vezom.

1. Nabavite datoteku aplikacije (APK): gumbom za preuzimanje na stranici **Postavljanje
   tableta** u web administraciji ili putem poveznice koju smo vam poslali. Otvorite je na
   tabletu ili datoteku kopirajte na tablet.
2. Otvorite preuzetu datoteku. Android pita smije li se dopustiti instalacija aplikacija iz
   tog izvora (vašeg preglednika ili upravitelja datoteka) — dopustite, zatim dodirnite
   **Instaliraj**.
3. Otvorite instaliranu aplikaciju.

## 5. Uparivanje tableta s apartmanom

1. Pri prvom pokretanju aplikacija prikazuje zaslon za prijavu. Prijavite se **istom
   e-poštom i lozinkom** kao u web administraciji.
2. Dodirnite apartman kojem ovaj tablet pripada.

Tablet sada prikazuje taj apartman. Vaša prijava ne ostaje spremljena na tabletu.

**Za odabir drugog apartmana kasnije:** držite **ikonu kućice u gornjem lijevom kutu
5 sekundi**, pustite, prijavite se i dodirnite **Reconfigure apartment** (ovaj je dijalog
uvijek na engleskom). Nakon tri pogrešne lozinke dijalog se zatvara i minutu se ne može
ponovno otvoriti.

**Kada nešto promijenite u web administraciji,** tablet to preuzima sljedeći put kad se
aplikacija vrati na zaslon — ugasite i upalite zaslon tableta ili zatvorite pa ponovno
otvorite aplikaciju. Ne osvježava se dok gledate.

## 6. Kiosk način (neobavezno)

Kiosk način zaključava tablet na aplikaciju: gosti ne mogu otići na početni zaslon,
prebaciti se u drugu aplikaciju ni otvoriti obavijesti. Tablet radi i bez toga; koristite
ga ako tablet ostaje u apartmanu bez nadzora.

Potrebno je računalo s **ADB-om** (Android platform tools) i **vraćanje na tvorničke
postavke, koje briše sve s tableta.** Napravite to prije uparivanja.

1. Vratite tablet na tvorničke postavke. Pri početnom postavljanju **preskočite dodavanje
   Google računa**.
2. Uključite **Opcije za razvojne programere → USB otklanjanje pogrešaka** (Postavke →
   O tabletu → sedam puta dodirnite *Broj međuverzije* da se opcije pojave) i spojite tablet
   na računalo.
3. Instalirajte aplikaciju s računala:

   ```
   adb install tourist-app.apk
   ```

4. Postavite aplikaciju kao vlasnika uređaja:

   ```
   adb shell dpm set-device-owner com.touristapp.staging/com.touristapp.admin.KioskAdminReceiver
   ```

5. Otvorite aplikaciju i uparite je s apartmanom (5. poglavlje).
6. Držite ikonu kućice 5 sekundi, prijavite se i dodirnite **Enable kiosk mode**.

Kiosk način ostaje uključen i nakon ponovnog pokretanja.

**Izlaz:** držite ikonu kućice 5 sekundi, prijavite se i dodirnite **Exit kiosk mode**.
Tablet se opet ponaša uobičajeno, a kiosk način možete ponovno uključiti na isti način. Za
potpuno poništavanje postavljanja vratite tablet na tvorničke postavke. (Stranica
Postavljanje tableta spominje opciju „Remove kiosk completely”; ova verzija aplikacije je
nema.)

## 7. Što testirati

U web administraciji:

- [ ] Postavite lozinku putem **Zaboravljena lozinka?** i prijavite se
- [ ] Kreirajte apartman s fotografijama, WiFi podacima i vremenom odjave
- [ ] Dodajte barem dvije sobe s uređajima
- [ ] Dodajte nekoliko mjesta u različitim kategorijama i povežite ih s apartmanom
- [ ] Dodajte javni i privatni prijevoz
- [ ] Odaberite grupu hitnih kontakata, dodajte svoje kontakte i kućni red
- [ ] Dodajte goste, prijavite boravak, kasnije ga odjavite
- [ ] Prebacite administraciju na drugi jezik

Na tabletu:

- [ ] Instalirajte i uparite
- [ ] Apartman, sobe, mjesta, prijevoz, hitni brojevi i kućni red izgledaju ispravno
- [ ] Gosti boravka pozdravljeni su imenom; datum i vrijeme odjave su točni
- [ ] Promijenite jezik — tekstovi koje ste preveli se mijenjaju, ostali su na engleskom
- [ ] Napišite recenziju kao gost, zatim je uredite
- [ ] Recenzija se pojavljuje na kartici **Recenzije** apartmana u web administraciji
- [ ] Promijenite nešto u web administraciji i provjerite stiže li na tablet
- [ ] Ponovno pokrenite tablet; isključite pa uključite Wi-Fi
- [ ] Ako koristite kiosk način: uključite ga, ponovno pokrenite tablet, izađite iz njega

Zanima nas i što je bilo zbunjujuće, sporo ili što nedostaje, a ne samo što se pokvarilo.

## 8. Prijava problema

Pišite osobi koja vas je pozvala. Puno pomaže ako navedete:

- e-poštu svog računa i naziv apartmana;
- je li se dogodilo u web administraciji ili na tabletu;
- što ste napravili, što ste očekivali i što se umjesto toga dogodilo;
- datum i vrijeme;
- snimku zaslona ili fotografiju tableta;
- za tablet: model i verziju Androida.

## 9. Podaci vaših gostiju

- Unosite samo one podatke o gostima koji vam trebaju.
- Recite gostima da se njihovo ime prikazuje na tabletu te da se recenzije pohranjuju i
  prikazuju kasnijim gostima.
- Za brisanje podataka — vaših ili gostovih — pišite nam. Gosti, boravci i recenzije brišu
  se najkasnije 30 dana nakon završetka testiranja.
