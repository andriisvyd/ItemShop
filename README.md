# Item Shop

Android app that turns your Instagram posts into shop items. Pure Compose,
clean architecture, KMP-friendly.

User story: log in with Instagram → see your products (empty on first
run) → tap **+** → pick one of your Instagram posts → mapped onto a
product draft (image, parsed title, parsed price) → edit → save →
back on the list with the new product.

## Project layout

- `:domain` — pure Kotlin/JVM. Models, repository contracts, use cases.
  No Android, no Room, no Ktor. KMP-ready in shape.
- `:data` — Android library. Room (products), Ktor (Instagram OAuth +
  Graph), DataStore (token storage). Concrete impls are `internal` to
  this module; only the `Module` instances and a couple of
  configuration types are public.
- `:app` — application module. Compose UI, navigation, ViewModels,
  Android plumbing (Custom Tabs, OAuth redirect activity), Koin
  composition root.

Each layer/feature owns its own Koin modules. `ItemShopApplication`
is the single composition root and aggregates everything in
`startKoin { … }`.

## Setup (fresh checkout)

1. Fill in `secrets.properties` at the repo root (gitignored):
   ```properties
   instagram.clientId=...
   instagram.clientSecret=...
   ```
   Values come from the Meta dashboard → *API setup with Instagram
   Login*.

2. Make sure your Instagram account is added as an **Instagram
   Tester** (Meta dashboard → *App Roles → Roles*) and accept the
   invite from inside the Instagram app/web before signing in. The
   app stays in development mode; only invited testers can log in.

3. The OAuth redirect URI is hardcoded to
   `https://item-shop.pages.dev/oauth/callback`. The corresponding
   `assetlinks.json` is hosted on Cloudflare Pages and pinned to the
   debug-keystore SHA-256. If you build with a different signing
   key (release, a teammate's machine), update both
   `https://item-shop.pages.dev/.well-known/assetlinks.json` and
   re-run `adb shell pm set-app-links --package com.svyd.itemshop 0
   all && adb shell pm verify-app-links --re-verify
   com.svyd.itemshop`.

## Caption conventions

Used by `BuildProductDraftFromPostUseCase` to pre-fill the edit
screen:

- **Price**: `🏷️<amount><currency>` anywhere in the caption, e.g.
  `🏷️515₴`, `🏷️5.99€`.
- **Title**: first line of the caption — unless the first line is a
  status marker, in which case the second line is taken instead.
- **Status markers** (case-insensitive, exact match on a trimmed
  line): `Бронька`, `Забрали`. Currently only used as a heuristic
  for the title fallback rule; not stored on the product. To be
  normalised to emoji markers in a follow-up.

## Known limitations

- **Instagram CDN URLs expire.** Cover images are stored as URLs
  only. Eventually they 404 and Coil shows broken thumbnails. Local
  image caching is in the deferred list.
- **App secret embedded.** Per Meta's threat model, requests signed
  with `client_secret` are treated as if authorised only by a
  client token, which restricts a few privileged API calls (none
  that we currently use).
- **Dev mode.** Only invited testers can sign in. Going public
  needs Tech Provider verification and per-scope App Review.
- **No edit mode.** Tapping a product opens a read-only details
  screen. Editing existing products is a v1+ enhancement (see
  below).

## Deferred from v1 (intentional, do later)

- **Carousel children** — currently store only the cover image of a
  carousel post. Need to call `/<media-id>/children` and persist
  all child URLs.
- **Posts pagination** — currently fetches the first ~25 items.
  Need a `paging.next` cursor and infinite scroll.
- **Local image storage** — download cover images to local files
  instead of holding Instagram CDN URLs (which expire).
- **Posts caching** — currently fresh-fetches every Posts screen
  open. Cache to Room with a TTL once expiring URLs are fixed.
- **OAuth error UX** — `OAuthRedirectActivity` swallows token
  exchange errors. The login loader spins forever on failure
  instead of surfacing a snackbar / message.
- **Sign-out error UX** — same shape as above; `SignOutUseCase`
  failures are silent.
- **Locale-aware decimals** — price always renders with `.` as
  the decimal separator regardless of locale.
- **Image editing in the edit form** — cover image is read-only,
  inherited from the source post.

## v1+ enhancements

- **3-field price** (amount + cents + currency) — once EUR / other
  currencies appear; current 2-field UI assumes ₴ and rare cents.
- **Emoji status markers** — replace literal `Бронька` / `Забрали`
  detection with explicit emoji prefixes, like the price marker.
- **Real splash screen** via `androidx.core:core-splashscreen` for
  smoother cold start.
- **Friendly OAuth callback page** at
  `https://item-shop.pages.dev/oauth/callback` (purely cosmetic;
  the App Link intercepts before the page renders, but currently
  Cloudflare's 404 briefly flashes during the hand-off).
- **Edit existing products**: add an "Edit" affordance on the
  details screen that re-enters the EditProduct flow against the
  same id. Currently details are read-only; the EditProduct flow
  is reserved for product creation from a post.
- **Tech Provider verification + App Review** when going public.

## Architecture decisions worth remembering

- **Per-layer Koin modules**, single `startKoin` in `:app`. Internal
  classes stay internal because their DI bindings live next to
  them, not in `:app`.
- **Domain stays pure-Kotlin** with `koin-core` (api). No Android,
  no DI framework that's Android-only. Drops straight into a KMP
  `commonMain` if/when we go that route.
- **Type-safe Compose Navigation 2.8 routes** as `@Serializable`
  data classes (`navigation/Routes.kt`).
- **Product id == Instagram media id**. No locally generated IDs.
  `ProductDraft.id` is non-null. A product is, by design, a 1:1
  extension of one post.
- **AGP 9.x built-in Kotlin** — Android modules don't apply
  `kotlin-android`; pure-JVM modules (`:domain`) still apply
  `kotlin.jvm`. `android.disallowKotlinSourceSets=false` is set
  until KSP/Room ship a Built-in-Kotlin-aware release.
