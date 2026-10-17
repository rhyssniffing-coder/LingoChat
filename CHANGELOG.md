# Changelog

## 1.1.0

Multi-version support — the mod now builds for every Minecraft version from 1.21 through 1.21.11. Grab the jar matching your exact version from the release page.

- one jar per Minecraft version (1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11)
- internal split for the chat/text API changes Mojang made mid-1.21 (click events, hover events, keybinding categories) — handled automatically, same features everywhere
- CI builds and uploads every version's jar on each push

## 1.0.0

First release.

- every chat message gets a clickable `translate` link under it, with `original` to flip back
- pick your target language from 30 options
- settings screen on `U`: auto-translate your outgoing messages, auto-translate incoming chat, language choices
- repeated translations are cached, so common messages swap instantly
- works on multiplayer servers, nothing needed on the server side
