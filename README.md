# Kavir Android Apps

This repository builds two separate Android apps with GitHub Actions.

## 1. Kavir Browser
The existing dark Android browser, including WebView browsing, tabs, history, bookmarks, and optional Xray/Worker proxy features.

## 2. Kavir Telegram Bot Manager
A separate local Android app for managing a Telegram bot using the official Telegram Bot API.

Features:
- Validate a bot token with `getMe` and show bot name, username, and ID.
- Encrypt the token locally with AES/GCM backed by Android Keystore.
- Send messages to a chat or channel.
- Edit or delete a message when Telegram permissions and API rules allow it.
- Read currently available updates and show sender/chat identifiers.
- Inspect webhook status with `getWebhookInfo`.

Important limitations:
- Bot API does not expose a complete archive of old chats/messages.
- The app fetches updates manually; it does not run a persistent background polling service.
- `getUpdates` cannot be used while a webhook is active.
- Do not share your bot token or commit it to GitHub. Revoke a leaked token through BotFather.
- Message editing/deletion depends on Telegram's permissions and restrictions.

## Build and download APKs

The GitHub Actions workflow builds both apps. The APKs are uploaded as a workflow artifact and, on pushes to `main`, attached to a GitHub Release.

Expected APK names:
- `KavirBrowser-debug.apk`
- `KavirBotManager-debug.apk`

The source code for the Telegram app is in `botmanager/`; the existing browser remains in `app/`.
