# Dokumentacija projekta: CareEvac

**Armin Coralic**
**Aplikacija:** CareEvac – Sistem za upravljanje evakuacijom u starackim domovima i bolnicama

---

### a. Glavne funkcionalnosti / Ekrani

Aplikacija je dizajnirana da bude brza i pouzdana u kriznim situacijama. Tri ključna ekrana su:

1. **Početni ekran (Home Screen)**
   - **Opis:** Centralno čvorište aplikacije sa jasnim vizuelnim instrukcijama.
   - **Funkcije:** Sadrži veliko crveno dugme za pokretanje evakuacije i opciju za administraciju. Prikazuje ime prijavljenog korisnika i omogućava sigurnu odjavu.

2. **Ekran za hitnu evakuaciju (Emergency Screen)**
   - **Opis:** Glavni alat za spasioce. Prikazuje listu stanara sortiranu po prioritetu.
   - **Funkcije:** Sadrži tabove za brzo filtriranje (**Prioriteti**, **Svi**, **Evakuisani**). Klikom na dugme `EVAKUIŠI`, status osobe se trenutno mijenja u bazi, kartica postaje zelena, a brojač evakuisanih se ažurira.

3. **Prijava i Registracija (Login & Sign Up)**
   - **Opis:** Sigurnosni sloj aplikacije.
   - **Funkcije:** Koristi **Firebase Authentication**. Omogućava osoblju da kreira naloge i sigurno se prijavi kako bi pristupili osjetljivim podacima o stanarima.

---

### b. Opcionalne funkcionalnosti

U aplikaciju su ugrađene i dodatne mogućnosti koje povećavaju sigurnost:

* **Offline Mode:** Zahvaljujući lokalnom keširanju podataka, aplikacija radi i bez interneta. Podaci se automatski sinhronizuju sa Firebase-om čim se uređaj ponovo poveže.
* **Pametno sortiranje:** Sistem automatski prepoznaje status stanara. Osobe označene kao **"NEPOKRETAN"** automatski skaču na vrh liste jer zahtijevaju najveću pažnju.
* **Real-time Sync:** Ako dva spasioca koriste aplikaciju u isto vrijeme, promjene koje unese jedan (npr. označi nekoga kao spašenog) odmah su vidljive na ekranu drugog.

---

### c. Uputstva za korištenje

Da biste testirali aplikaciju, možete koristiti sljedeće parametre:

1. **Pokretanje:** Instalirajte aplikaciju na Android uređaj ili emulator.
2. **Demo podaci za prijavu:**
   - **Email:** `test@careevac.com`
   - **Lozinka:** `Test123`
3. **Proces testiranja:**
   - Prijavite se na sistem.
   - Na početnom ekranu odaberite **Pokreni hitnu evakuaciju**.
   - Kliknite na dugme **EVAKUIŠI** pored imena stanara.
   - Koristite dugme **RESET** u zaglavlju ako želite da vratite sve stanare na početni status (nisu evakuisani).

---

### d. Šta ste naučili

Izrada ove aplikacije mi je omogućila da savladam:

* **Jetpack Compose:** Moderni način pravljenja UI-a kroz kod, bez XML fajlova.
* **MVVM Arhitekturu:** Pravilno razdvajanje logike od dizajna (Model-View-ViewModel).
* **Firebase (Firestore & Auth):** Rad sa bazama podataka u realnom vremenu i upravljanje korisničkim nalozima.
* **Coroutines & Flow:** Rukovanje podacima koji se asinhrono učitavaju sa interneta.
