# Dokumentacija projekta: CareEvac

**Armin Ćoralić**
**Aplikacija:** CareEvac – Sistem za upravljanje evakuacijom u staračkim domovima i bolnicama

---

## Glavne Funkcionalnosti

Aplikacija je podijeljena na module zavisno od dodijeljene uloge korisnika (**Super Admin, Admin, Staff**).

### 1. Autentifikacija i Sigurnost (Auth Module)
Sigurnosni ulaz u aplikaciju sa fokusom na integritet podataka.
- **Validacija unosa:** Implementirana provjera formata emaila i jačine lozinke u realnom vremenu.
- **Verifikacija Emaila:** Obavezna potvrda email adrese prije pristupa sistemu kako bi se spriječili lažni nalozi.
- **Role-Based Redirection:** Sistem automatski prepoznaje ulogu korisnika pri loginu i preusmjerava ga na odgovarajući početni ekran (Admin Panel ili Staff Dashboard).

### 2. Upravljanje Evakuacijom (Emergency Dashboard)
*Dostupno za: Staff, Admin, Super Admin*
- **Status uživo:** Prikaz liste stanara sa jasnim bojama (Zeleno = Evakuisan, Sivo = Čeka).
- **Pametno sortiranje:** Prioritet na listi automatski imaju **nepokretne osobe** i osobe sa posebnim medicinskim potrebama.
- **One-Tap Action:** Evakuacija jednim dodirom na karticu stanara radi maksimalne brzine.
- **Real-time Sync:** Promjene statusa su trenutno vidljive na svim uređajima (npr. ako jedan spasilac označi osobu kao spašenu, to vide svi ostali).

### 3. Upravljanje Stanarima (Resident CRUD)
*Dostupno za: Admin (za svoju instituciju) i Super Admin*
Potpuna kontrola nad bazom podataka štićenika:
- **Dodavanje:** Unos novih stanara sa detaljima (Ime, Soba, Status pokretljivosti, Medicinske napomene).
- **Uređivanje:** Ažuriranje podataka u slučaju promjene sobe ili zdravstvenog stanja.
- **Brisanje:** Uklanjanje stanara iz sistema.
- **Izolacija podataka:** Admin vidi i uređuje isključivo stanare svoje institucije.

### 4. Administracija Sistema (Super Admin Panel)
*Dostupno isključivo za: Super Admin*
- Pregled svih registrovanih korisnika u sistemu.
- Upravljanje institucijama.
- Mogućnost deaktivacije ili brisanja korisničkih naloga.

---

## 🛡️ Uloge u Sistemu (Roles & Permissions)

Implementiran je **RBAC (Role-Based Access Control)** sistem:

| Uloga | Opis i Dozvole |
| :--- | :--- |
| **Super Admin** | Puni pristup cijelom sistemu. Kreira Admine i ostale korisnike, upravlja institucijama, vidi sve podatke. |
| **Admin** | Upravlja svojom institucijom. **Može:** Dodavati/brisati stanare, resetovati evakuaciju. **Ne može:** upravljati korisnicima. |
| **Staff** | Operativno osoblje. **Može:** Vidjeti listu i označiti stanare kao evakuisane. **Ne može:** Dodavati ili brisati stanare. |

---

## Opcionalne funkcionalnosti

U aplikaciju su ugrađene i dodatne mogućnosti koje povećavaju sigurnost:

* **Offline Mode:** Zahvaljujući lokalnom keširanju podataka, aplikacija radi i bez interneta. Podaci se automatski sinhronizuju sa Firebase-om čim se uređaj ponovo poveže.
* **Pametno sortiranje:** Sistem automatski prepoznaje status stanara. Osobe označene kao **"NEPOKRETAN"** automatski skaču na vrh liste jer zahtijevaju najveću pažnju.
* **Real-time Sync:** Ako dva spasioca koriste aplikaciju u isto vrijeme, promjene koje unese jedan (npr. označi nekoga kao spašenog) odmah su vidljive na ekranu drugog.

---

## Uputstva za korištenje

Da biste testirali aplikaciju, možete koristiti sljedeće parametre:

1. **Pokretanje:** Instalirajte aplikaciju na Android uređaj ili emulator.
2. **Demo podaci za prijavu:**
   - **Email:** `careevac@gmail.com`
   - **Lozinka:** `Test123`
3. **Proces testiranja:**
   - Prijavite se na sistem. Nalog ima rolu super admina.
   - Na početnom ekranu odaberite **Pokreni hitnu evakuaciju**.
   - Kliknite na dugme **EVAKUIŠI** pored imena stanara.
   - Koristite dugme **RESET** u zaglavlju ako želite da vratite sve stanare na početni status (nisu evakuisani).
   - Koristite dugme **VRATI** ako želite da vratite određenog stanara iz liste evakuisanih

   - Na početnom ekranu odaberite **ADMINISTRACIJA STANARA**.
   - Kliknite na ikonicu **kante za smeće** ako želite da obrišete nekog od stanara.
   - Kliknite na ikonicu **olovke** ako želite da uređujete podatke o stanaru.
   - Kliknite na floating button **+** u desnom donjem dijelu ekrana ako želite da dodate novog stanara.

   - Na početnom ekranu odaberite **UPRAVLJANJE KORISNICMIA**.
   - Kliknite na ikonicu **kante za smeće** ako želite da obrišete nekog od korisnika.
   - Kliknite na button **DEAKTIVIRAJ** ako želite da deaktivirate nalog nekog od korisnika, bez brisanja podataka o istom.
   - Kliknite na button **PROMIJENIT ROLU** ako želite da promijenite rolu određenog korisnika.
---

## Šta ste naučili

Izrada ove aplikacije mi je omogućila da savladam:

* **Jetpack Compose:** Moderni način pravljenja UI-a kroz kod, bez XML fajlova.
* **MVVM Arhitekturu:** Pravilno razdvajanje logike od dizajna (Model-View-ViewModel).
* **Firebase (Firestore & Auth):** Rad sa bazama podataka u realnom vremenu i upravljanje korisničkim nalozima.
* **Coroutines & Flow:** Rukovanje podacima koji se asinhrono učitavaju sa interneta.

---

## Krizni scenario i uloga korisnika

Na osnovu povratnih informacija sa prezentacije koncepta, uloga korisnika je precizno definisana kako bi aplikacija bila primjenjiva u realnim uslovima:

* **Uloga Koordinatora:** Aplikacija nije namijenjena osoblju koje vrši fizičku evakuaciju (nošenje stanara), već **koordinatoru evakuacije** (npr. glavna sestra ili šef smjene). 
* **Scenario korištenja:** U slučaju požara ili druge opasnosti, koordinator zauzima poziciju na sigurnosnom izlazu ili zbornom mjestu. Kako timovi iznose stanare, koordinator ih jednim dodirom (One-tap) označava kao evakuisane. 
* **Smanjenje kognitivnog opterećenja:** Lista automatski fokusira neevakuisane osobe i prioritetne grupe (nepokretne), omogućavajući koordinatoru da u sekundi odgovori na pitanje: *"Ko je još ostao u zgradi?"* bez potrebe za pretraživanjem ili dodatnom interakcijom.

---

