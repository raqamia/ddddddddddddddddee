# AGENTS.md — Manara

## Project structure

- `app/` — Android app (Java, ViewBinding, Navigation Component)
- `admin/index.html` — Admin panel (vanilla HTML + Supabase JS SDK via CDN)
- `website/index.html` — Landing page (static HTML/CSS)

## Build & run

```bash
# Build debug
./gradlew assembleDebug

# Build release (requires env vars for signing)
KEYSTORE_PATH=... STORE_PASSWORD=... KEY_PASSWORD=... ./gradlew assembleRelease

# Clean
./gradlew clean
```

- Gradle 9.3.1 + AGP 9.1.1. Version catalog at `gradle/libs.versions.toml`.
- **No tests exist** — no `test/` or `androidTest/` directories.

## Architecture

- **Language:** Java only (app code is in Java, Gradle files use Kotlin DSL)
- **DI:** Manual — no Hilt/Dagger. ViewModelFactories used instead.
- **State:** ViewModel + LiveData
- **Navigation:** `nav_graph.xml` with pop-up-to-inclusive on most transitions to flatten the stack
- **Bottom nav:** 3 tabs — Home, Saved, Settings (controlled by `MainActivity` visibility listener)

## Supabase backend

- URL + anon key set as `buildConfigField` in `app/build.gradle.kts:21-22`.
- Read via `BuildConfig.SUPABASE_URL` / `BuildConfig.SUPABASE_ANON_KEY`.
- Retrofit service interface: `data/remote/SupabaseApiService.java`.
- Token refresh handled automatically by `AuthInterceptor` (OkHttp interceptor).
- Admin SQL setup: `admin/SETUP.sql` (creates `is_admin` flag, `notifications` table, RLS policies).
- **Must apply SETUP.sql** before admin panel works.

## Room database

- `AppDatabase.java` version 5 with 5 entities: SubjectEntity, FileEntity, DownloadEntity, SavedFileEntity, RecentEntity.
- Uses SQLCipher encryption with runtime-generated 256-bit passphrase stored in EncryptedSharedPreferences.
- Uses `fallbackToDestructiveMigration()` — schema changes wipe all local data.
- DAOs: SubjectDao, FileDao, DownloadDao, SavedDao, RecentDao.

## Storage & preferences

- **Session tokens:** `EncryptedSharedPreferences` via `SessionManager`
- **User settings:** plain `SharedPreferences` via `AppPreferences`
- **PDF storage bucket:** Supabase Storage bucket `"pdfs"` (private, uses signed URLs)

## Key conventions

- **RTL first:** `android:layoutDirection="rtl"` in theme, all strings in Arabic.
- **Font:** Cairo font bundled in `res/font/cairo`. Applied globally via theme.
- **ProGuard:** enabled in release — keep rules for Gson DTOs, Room entities, Retrofit, Glide.
- **Network security:** cleartext denied globally, only `supabase.co` domain allowed.
- **Dark mode:** `AppCompatDelegate.setDefaultNightMode()` in `ManaraApplication.onCreate()`.
- **No Compose** — all XML layouts.

## Navigation flow

```
Splash (2s animation) → Onboarding (3 slides) → Login/Register
    → TrackSelect → Home (with bottom nav)
```

## Key dependencies (non-obvious)

- PDF viewer: `com.github.mhiew:android-pdf-viewer:3.2.0` (fork of barteksc/AndroidPdfViewer)
- Splash screen: `androidx.core:core-splashscreen` (install before `super.onCreate`)
- Image loading: Glide with KSP annotation processor
- Database encryption: `net.zetetic:android-database-sqlcipher:4.6.1` + `androidx.security:security-crypto:1.1.0`
- SSL pinning: OkHttp CertificatePinner (2 pins — leaf + intermediate CA)
- No Jetpack Compose dependencies (catalog cleaned)
- No test dependencies (no test directories exist)

## Constants

- `TAWJIHI_EXAM_DATE = "2026-06-13"` — used for countdown on home screen
- `PREFS_NAME = "manara_secure_prefs"` — EncryptedSharedPreferences name
- `PDF_BUCKET = "pdfs"` — Supabase Storage bucket
- Error codes map to Arabic messages in `ErrorMessages.java`

## Release signing

- Reads `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` from environment variables.
- Build fails if any env var is missing and a release build is attempted (no silent unsigned APK).
