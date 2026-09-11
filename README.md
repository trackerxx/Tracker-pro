# Tracker — Native Kotlin (Home Screen)

এটা তোমার Capacitor/HTML app-এর **Home screen**-এর Kotlin + Jetpack Compose version।
পুরো app না বানিয়ে ইচ্ছাকৃতভাবে শুধু এই একটা screen দিয়ে শুরু করা হয়েছে, যাতে ধাপে ধাপে বাকি screen (Logs, Stats, Profile) যোগ করা যায়।

## যা আছে

- **Jetpack Compose UI** — original app-এর সাথে মিলিয়ে বানানো: Welcome header + date, Remaining Balance card (fund switch করার জন্য expandable), Update/recent-activity button, Debts & Receivables overview, Category Breakdown card (week/month toggle সহ)।
- **আসল logic, dummy static UI না:**
  - `HomeViewModel` এ sample funds/transactions/debts আছে, আর balance, spent amount, owe/owed, category breakdown — সব ঐ data থেকে **compute** হয়।
  - Fund এ tap করলে active fund সত্যিই বদলায় → balance, spent, category chart সব নতুন fund অনুযায়ী recalculate হয়।
  - Week/Month toggle সত্যিই transaction date filter করে chart বানায়।
- Brand colors (`--brand-red`, `--brand-green`) original CSS থেকে হুবহু নেওয়া।

## কীভাবে build করবে

### Option A — Android Studio (লোকাল)

1. Android Studio (Koala বা তার পরের version) খুলো।
2. **Open** → এই `TrackerNative` folder select করো।
3. প্রথমবার Gradle sync চাইবে — Android Studio নিজে থেকেই Gradle wrapper জেনারেট/ঠিক করে দেবে (internet লাগবে dependency download এর জন্য)।
4. Emulator বা device এ Run চাপো।

### Option B — GitHub Actions (Android Studio ছাড়াই, তোমার Capacitor app এর মতোই)

`.github/workflows/build-apk.yml` ফাইলটা এই zip-এই দেওয়া আছে — এটা তোমার আগের Capacitor workflow-এর মতোই কাজ করে, কিন্তু এই project এ npm/Capacitor কিছু নেই বলে ধাপ অনেক ছোট।

**করণীয়:**

1. `TrackerNative` folder-এর **ভেতরের সব কিছু** (root এ `settings.gradle.kts`, `app/`, `.github/` ইত্যাদি) দিয়ে একটা নতুন GitHub repo বানাও — অর্থাৎ `TrackerNative` folder নিজেই repo-র root হবে, এর ভেতরে আরেকটা folder এ রাখলে workflow path মিলবে না।
2. Repo তে push করো (`main` অথবা `master` branch এ), অথবা GitHub এ গিয়ে **Actions** ট্যাব থেকে ম্যানুয়ালি **Run workflow** চাপো (`workflow_dispatch` enabled আছে)।
3. Build শেষ হলে **Actions → (সর্বশেষ run) → Artifacts** এ গিয়ে `tracker-debug-apk` download করো — এটাই তোমার debug APK, phone এ install করে test করতে পারবে।

**এই workflow কেন সহজ:**
- এই project এ `gradlew` commit করা নেই (sandbox এ internet না থাকায় সেই wrapper jar বানানো যায়নি), তাই workflow-তে `gradle/actions/setup-gradle` action ব্যবহার করা হয়েছে — এটা runner-এ নিজে থেকেই সঠিক Gradle version (8.7) install করে দেয় আর plain `gradle` command দিয়ে build চালানো যায়। Android SDK GitHub-এর ubuntu runner-এ আগে থেকেই থাকে, তাই আলাদা করে সেটআপ লাগে না।
- চাইলে পরে `gradle wrapper --gradle-version 8.7` চালিয়ে `gradlew`/`gradle-wrapper.jar` generate করে commit করে দিতে পারো — তখন workflow-এ `gradle assembleDebug` এর বদলে চিরাচরিত `./gradlew assembleDebug` ব্যবহার করা যাবে, কাজ একই।

## Project structure

```
app/src/main/java/com/myapp/tracker/
├── MainActivity.kt          # entry point
├── data/
│   ├── Models.kt             # Fund, Transaction, Debt, CategorySlice
│   └── HomeViewModel.kt      # state + calculation logic
└── ui/
    ├── HomeScreen.kt         # সব UI composable
    └── theme/                # colors, typography, MaterialTheme
```

## পরের ধাপ (তোমার পছন্দমতো order এ করতে পারো)

1. **Local persistence** — এখন data শুধু in-memory (sample)। পরের ধাপে `Room` বা `DataStore` দিয়ে আসল save/load যোগ করা যাবে (original app যেমন `localStorage` ব্যবহার করছিল, তার সমতুল্য)।
2. **Logs screen** — Transactions / Debts / Receivables এই তিন subtab।
3. **Stats screen** — বিস্তারিত spending chart + debts/receivables analytics।
4. **Profile screen** + biometric/PIN/pattern lock (original app এ যেমন আছে)।
5. Add-transaction flow, fund transfer/add modal — এখন button গুলো UI-তে আছে কিন্তু action এখনো wire করা হয়নি।

যেকোনো একটা ধাপ বললেই সেটা নিয়ে এগিয়ে যাওয়া যাবে।
