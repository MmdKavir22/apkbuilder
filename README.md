# Kavir Telegram Bot Manager

This repository's Android build now produces only **Kavir Telegram Bot Manager**. The old browser module is no longer included in Gradle builds.

## What the app can do
- Connect a bot token from BotFather using `getMe`.
- Encrypt and store the token locally using Android Keystore.
- Send messages to a known chat ID or channel username.
- Edit or delete messages where Telegram permissions and Bot API rules allow it.
- Fetch available incoming updates manually and show sender/chat IDs.
- Check webhook status.

## Important Telegram Bot API limitation
This is a bot-management app, not a Telegram user-account client. A bot token cannot sign into Telegram as a human account and cannot reveal all of that account's private chats, groups, channels, or historical messages.

The bot can receive messages from private users who have started it, and messages/updates from groups or channels where it has been added and has the relevant access. Bot API does not provide a general API to enumerate every chat the bot has ever interacted with or fetch a complete chat history. The app's inbox therefore shows only updates that Telegram makes available to the bot.

- Updates are fetched manually in this version; continuous background polling is not enabled.
- `getUpdates` cannot be used while a webhook is active.
- Do not share the token or commit it to GitHub. If exposed, revoke it through BotFather.
- Editing/deleting messages depends on Telegram's permissions and API restrictions.

## Build and download
GitHub Actions builds `KavirTelegramManager-debug.apk`. Download it from the successful workflow run's **Artifacts** section or from the matching GitHub Release.

Source code is in `botmanager/`.
