# Kavir Browser

مرورگر اندرویدی Kavir Browser با رابط تاریک و حالت اختیاری Cloudflare Worker Proxy.

## امکانات

- رابط مدرن و تاریک
- WebView با JavaScript و DOM Storage
- نوار آدرس و جستجو
- Back / Forward / Refresh / Home
- تب‌های پایه
- ذخیره تاریخچه در SQLite
- تنظیمات Worker Proxy
- پشتیبانی RTL و فارسی
- ساخت APK با GitHub Actions
- قرار گرفتن APK در GitHub Releases بعد از هر Build

## Cloudflare Worker

کد Worker در فایل `worker/worker.js` قرار دارد.

در Cloudflare:

1. وارد Workers & Pages شوید.
2. یک Worker بسازید.
3. محتوای `worker/worker.js` را در ادیتور Worker قرار دهید.
4. Deploy کنید.
5. آدرس `workers.dev` را کپی کنید.
6. در Kavir Browser به Settings → Proxy Settings بروید.
7. حالت Worker را فعال و URL را وارد کنید.

برای امنیت بیشتر می‌توانید یک Secret با نام `SECRET_TOKEN` در Cloudflare بسازید و همان مقدار را داخل برنامه وارد کنید.

> این Worker یک HTTP gateway است و VPN کامل یا VPN سراسری دستگاه نیست. بعضی سایت‌ها، WebSocketها، DRM، استریم‌ها و درخواست‌های خاص ممکن است از طریق یک proxy عمومی به‌طور کامل کار نکنند.

## Build

Workflow گیت‌هاب با Java 17 و Android SDK 34 پروژه را Build می‌کند.

با هر اجرای موفق، فایل APK علاوه بر Artifact در بخش **Releases** نیز منتشر می‌شود:

`Kavir Browser Build <run number>`

فایل APK داخل Release با نام:

`KavirBrowser-debug.apk`

قرار می‌گیرد.

## ساخت محلی

در محیطی که Gradle نصب است:

```bash
gradle assembleDebug
```

APK در:

`app/build/outputs/apk/debug/app-debug.apk`

ساخته می‌شود.
