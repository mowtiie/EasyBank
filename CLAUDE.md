# CLAUDE.md

## Project

EasyBank: a simulated mobile banking app where customers check balances, deposit and withdraw, transfer funds, pay bills and read their transaction history and alerts.

Core flow: **Log in → Pick an account → Transfer or pay a bill → Transaction recorded + notification**

This is a **simulation**. There is no backend, no payment gateway, no email/SMS, no real push notifications. Everything must look and behave correctly on screen during a demo. All app data is stored in local SQLite so it persists across launches.

Source of requirements: the spec the user provides (STI College Balayan Codefest Training AY 2023-2024, Problem #4: Mobile Banking App). Read it fully before building. Where the spec is vague, pick the simplest simulated behavior and note it here.

Status: build order steps 1 to 4 written (all 11 screens). Step 5 (emulator run and fixes) not done. Last verified build: `assembleDebug` and `lintDebug` succeeded. Not yet run on an emulator.

## Prime directive: build the fastest way possible

- Speed beats elegance. Pick the shortest path that works in a live demo.
- **No structures.** No architecture patterns (MVVM, MVP, repositories, use cases, DI), no layers, no feature packages, no interfaces or abstract classes, no custom model/POJO classes. Exception: a single shared `GenericAdapter<T>` with a nested `Binder` interface.
- One Activity per screen, one shared `Data.java` holding the constants, the `USER_*` fields and static helper methods that read and write the database.
- Read rows from a `Cursor` into `String[]` or `ArrayList<String>`. No custom model/POJO classes.
- Hardcode seed data and insert it once, when the database is first created. Never fetch or sync anything. All records live in the SQLite tables listed under Database and persist across launches. Only the logged-in user (`Data.USER_*`) lives in memory.
- Duplicate code freely if it is faster than abstracting it.
- No tests, no logging frameworks, no error handling beyond field validation.
- Do not add libraries or touch `build.gradle` dependencies. Enabling `buildFeatures { viewBinding = true }` is allowed.

## Build order

Build in this order. The core flow must work end to end before anything is polished.

1. `DbHelper` (tables and seed), `Data`, `LoginActivity`, `SignUpActivity`, `HomeActivity` (accounts and balances)
2. `TransferActivity` (own accounts and other users), `BillersActivity`, `PayBillActivity`, with transaction and notification writes
3. `TransactionsActivity`, `CashActivity` (deposit/withdraw), `NotificationsActivity`
4. `ProfileActivity` (edit details, change password, logout), `ForgotPasswordActivity`
5. UI polish, then run the whole flow on an emulator and fix what turns up.

## Stack

- Java, XML layouts, AppCompat and Material 3, single `app` module. minSdk 24, targetSdk 36.
- No Fragments, no Navigation component, no Jetpack Compose. No permissions, no internet.
- Views via view binding. Use the generated `*Binding` classes, never `findViewById`.
- Lists: `RecyclerView` with the one shared `GenericAdapter<T>` and shared row layout `item_row.xml`. Never write a per-screen adapter.
- Database: pure SQLite (`SQLiteOpenHelper`, `ContentValues`, `Cursor`). No Room, no ORM, no DAO layer.
- Dropdowns: `TextInputLayout` (ExposedDropdownMenu style) with `MaterialAutoCompleteTextView`. Never `Spinner`.
- Messages: `Snackbar` for validation and errors on a screen that stays open. `Toast` only for simulated notifications and messages shown as a screen closes. `MaterialAlertDialogBuilder` (never plain `AlertDialog.Builder`) for dialogs, `BottomSheetDialog` for add/edit sheets.

## Code layout

Package `com.mowtiie.easybank`, three flat folders. Do not nest them or add per-feature packages.

- `activities/`: every Activity.
- `data/`: `Data.java` (constants, `USER_*`, database helper methods) and `DbHelper.java` (tables and seed).
- `adapters/`: `GenericAdapter.java`.

Navigation is `startActivity(new Intent(...))` with `putExtra`. After a flow finishes, start the final screen with `NEW_TASK | CLEAR_TASK`. Logout clears the task and returns to Login.

## Database

- Every record the app creates or edits is a row in a SQLite table, written immediately and reloaded from the database in `onResume`.
- `DbHelper` has a `DATABASE_VERSION` constant. `onCreate` creates every table and then calls `seedDemo`. `onUpgrade` drops all tables and calls `onCreate`. Bump the version whenever a table changes during development.
- Tables: `users` (id, email, password, name, phone), `accounts` (id, user_id, number, type, balance), `transactions` (id, account_id, type, amount, date, description, counterparty), `notifications` (id, user_id, title, message, date, is_read).
- Enum-like values are stored as the text of their `ALL_CAPS` constants. Dates are stored as the display text from the picker. Transactions and notifications created by the app store the current time as display text (`MMM dd, yyyy hh:mm a`) and are listed newest first with `ORDER BY id DESC`.
- Balances and amounts are whole-peso `int` values.
- Any write that touches more than one row (transfers, bill payments) runs inside `beginTransaction` / `setTransactionSuccessful` / `endTransaction` so balances never go out of sync.

## UI rules

- Material 3 components, not plain framework widgets: `MaterialToolbar`, `MaterialTextView`, `MaterialButton`, `MaterialCardView`, `MaterialDivider`, `TextInputLayout` with `TextInputEditText`, `MaterialSwitch`, `TabLayout`, `Chip`/`ChipGroup`, progress indicators.
- Every screen except Login has a `MaterialToolbar` with a title and a back arrow (content description set), except root screens.
- Toolbar actions use menu resources via `app:menu` and `setOnMenuItemClickListener` (no `setSupportActionBar`).
- Empty lists use a shared `view_empty.xml` through `<include>`.
- Money uses one helper (`Data.money(int)`), never a raw currency prefix. Amount input fields show the peso sign as `tilAmount.setPrefixText(Data.CURRENCY)`, never a literal in the layout.
- Theme: generated by Material Theme Builder (`themes.xml`, `colors.xml`, `theme_overlays.xml`). Do not hand-edit. Colors come only from `?attr/color*` and `?attr/textAppearance*`. No hex values or custom colors.
- Page background and toolbars `colorSurfaceContainer`, sticky bars `colorSurfaceContainerHigh`, brand accents `colorPrimary`, secondary actions tonal, one filled button per screen.
- Insets: `EdgeToEdge.enable(this)` right after `super.onCreate` in every Activity, `fitsSystemWindows="true"` on every layout root. Auth screens sit in a `ScrollView` with `fillViewport="true"`.
- Spacing: screen padding 16dp (24dp on auth), gaps 8dp between fields and cards, 16dp between sections. Lists use `clipToPadding="false"` with 8dp bottom padding.
- Dividers only above a sticky bottom bar or between distinct sections, never under a toolbar or between a heading and a list.
- Chips are for interactive items. Static facts go in text.
- Destructive actions offer a Snackbar Undo.
- Accessibility: content descriptions on icons, 48dp touch targets, decorative emoji hidden from screen readers.

## Forms

- Every `TextInputLayout` has helper text with the format when it matters.
- Validate with inline `setError` on the wrong field, not a generic Snackbar. Clear the error on the next attempt.
- Password fields use the toggle end icon, 6+ characters. Emails use `Patterns.EMAIL_ADDRESS`.
- Dates are picked with `MaterialDatePicker` / `MaterialTimePicker` into a read-only field.

## Naming and style

- Constants `ALL_CAPS_WITH_UNDERSCORES`.
- View ID prefixes: `til`, `et`, `dd`, `tv`, `btn`, `rv`, `chip`, `sw`, `card`, `ll`.
- 4-space indent, `binding.xxx` access, lambdas for click listeners. Match surrounding code.
- Comments only when the reason is not obvious.

## Account and notifications defaults

- Sign up, login, logout, profile edit and password reset are fake but validated. Accounts live in the SQLite `users` table, and the logged-in user is copied into `Data.USER_*`.
- One role only (customer), so there is no role column, no role tab on sign up and no role-based home screen. Login always opens `HomeActivity`, and Logout is only on Profile.
- Create one demo account automatically (`demo@demo.com`, password `demo1234`), plus a second user (`other@demo.com`, password `demo1234`) so transfers to another user can be demoed.
- Seed demo data in one `seedDemo(SQLiteDatabase)` method called from `DbHelper.onCreate`, so it runs once on first install and no screen is empty in a demo. Never reseed on login or launch.
- Light and dark mode are both supported through the generated theme. Do not hardcode colors.
- Notifications are in-app only (Toast, Snackbar, list). Never use real Android notifications or channels.
- Payments, if any, always succeed after a short fake processing overlay.

## App-specific rules

**1. Registration and authentication**
- Sign up fields: full name, email, phone, password, confirm password. Email must be unique and valid, phone is 11 digits starting with `09`, password 6+ characters.
- Sign up also creates one `SAVINGS` account with a 0 balance and a 10-digit account number, and a welcome notification.
- Login by email and password against the `users` table. Forgot password asks for email and a new password (no email is sent).

**2. Account management (Profile)**
- Profile shows name, email, phone and the user's accounts with their numbers.
- "Open account" button opens a bottom sheet to pick one of `Data.ACCOUNT_TYPES` (`SAVINGS`, `CHECKING`, `PAYROLL`, `TIME_DEPOSIT`, `STUDENT`). It creates a 0 balance account with a unique 10-digit number and an "Account opened" notification.
- Edit name, email and phone with the same validation as sign up. Email stays unique.
- Change password needs the current password, then new password and confirmation. Success updates `users` and `Data.USER_*`.
- Logout only here, with a confirmation dialog.

**3. Home (accounts)**
- Shows every account of the user as a card (type, number, balance) and the total balance.
- Quick actions: Deposit/Withdraw, Transfer, Pay Bills, History. Toolbar menu opens Notifications and Profile.
- Shows the 5 most recent transactions across all accounts.
- Demo user has a `SAVINGS` and a `CHECKING` account so own-account transfers work.

**4. Transaction history**
- Lists every transaction of the user, newest first, each row showing type, amount, date and description. Outgoing amounts are shown as debits, incoming as credits.
- A `ChipGroup` filters by type (All, Deposit, Withdrawal, Transfer, Bill). Account dropdown filters by account.
- Types are the constants `TYPE_DEPOSIT`, `TYPE_WITHDRAWAL`, `TYPE_TRANSFER_IN`, `TYPE_TRANSFER_OUT`, `TYPE_BILL`.
- Tapping a row opens a `BottomSheetDialog` with the full details.

**5. Deposit and withdraw (`CashActivity`)**
- `TabLayout` with Deposit and Withdraw, account dropdown, amount field. Both are simulated cash movements.
- Withdraw fails with an inline error if the amount is more than the balance. Amount must be 1 to 50000 per transaction (`MAX_AMOUNT`).
- Success writes a transaction and updates the balance.

**6. Fund transfer**
- `TabLayout` with Own accounts and Other user.
- Own accounts: from and to dropdowns, which cannot be the same account.
- Other user: from dropdown and a 10-digit destination account number. Resolve it against `accounts`, show the owner's name on a confirmation dialog, and fail with an inline error if the number does not exist or is one of the user's own.
- Amount rules and insufficient-funds check as in Deposit/Withdraw. A transfer writes a `TYPE_TRANSFER_OUT` row for the sender and a `TYPE_TRANSFER_IN` row for the receiver, and updates both balances in one database transaction.
- Success goes through the fake processing overlay, then a result screen state shown on the same Activity.

**7. Bill payments**
- Billers are hardcoded in `Data`, not stored in a table, grouped by category constants `CATEGORY_UTILITY`, `CATEGORY_CREDIT_CARD`, `CATEGORY_OTHER`. A `ChipGroup` on `BillersActivity` filters the list by category.
- Utility: Meralco, Manila Water, PLDT. Credit card: BDO, BPI, Metrobank. Other: Globe, Smart, Tuition (STI).
- `PayBillActivity` fields: account dropdown, reference/account number (6 to 12 digits), amount. Fails with an inline error on insufficient funds.
- Success writes a `TYPE_BILL` transaction with the biller name as description and a success notification. No real biller lookup, the reference number is only checked for format.

**8. Notifications and alerts**
- All in-app: a `Toast` at the moment of the event plus a row in `notifications`, listed newest first in `NotificationsActivity`. Unread rows are marked and become read when the screen opens. Home toolbar shows the unread count in the menu item title.
- Events: deposit, withdrawal, transfer sent and received, bill paid, profile and password changes.
- Low balance: after any debit, if the account balance is below `LOW_BALANCE_LIMIT` (500), add a low balance notification.
- Failed actions (insufficient funds, unknown account number) show the inline error and also add a failure notification.
- A transfer to another user also adds a notification for the receiving user.

## Screens (one Activity each)

- `LoginActivity` (launcher)
- `SignUpActivity`
- `ForgotPasswordActivity`
- `HomeActivity` (root)
- `CashActivity`
- `TransferActivity`
- `BillersActivity`
- `PayBillActivity`
- `TransactionsActivity`
- `NotificationsActivity`
- `ProfileActivity`

## Manifest

- Only `LoginActivity` (launcher) is `exported="true"`. Every other Activity is `exported="false"`. Register every new Activity.

## Release

- `release` uses `isMinifyEnabled = true` and `isShrinkResources = true`. Add keep rules to `proguard-rules.pro` only when a release crash needs one.
- Signing reads `keystore.properties` in the project root (see `keystore.properties.example`). Never commit `keystore.properties`, `*.jks` or `*.keystore`.
- Verify with `apksigner verify --print-certs --verbose <apk>`.

## Commands

- Build: `./gradlew assembleDebug`
- Release: `./gradlew assembleRelease`
- Lint: `./gradlew lintDebug`
- Run: Android Studio's Run button on an emulator.

## Commit rules

- Never list Claude as an author or contributor. No `Co-Authored-By` line mentioning Claude or Anthropic. No "Generated with Claude Code" footer in commits or PR descriptions.
- Do not change `git config user.name` or `user.email`.
- Short, plain, imperative messages, for example `Add transfer screen`.
- Do not commit unless asked.

## Working agreements

- Keep changes small and in scope. Do not refactor or restyle code you were not asked to touch.
- After a change, run the build and report failures with the output.
- Update this file when a rule changes or a screen is added.
