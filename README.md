<p align="center">
  <img src="banner.png" alt="LingoChat" width="720">
</p>

<p align="center">
  <a href="https://fabricmc.net/"><img src="https://img.shields.io/badge/Fabric-1.21.11-4a9fd8" alt="Fabric 1.21.11"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-44c4ac" alt="MIT license"></a>
  <a href="https://github.com/rhyssniffing-coder/LingoChat/releases"><img src="https://img.shields.io/github/v/release/rhyssniffing-coder/LingoChat?include_prereleases&label=download" alt="Download"></a>
</p>

A client-side Fabric mod that translates chat. Every message gets a small gray *translate* link underneath it — open chat, click the link, and the message swaps in place to your language with a little *original* link so you can flip back. There's also a setting to automatically translate whatever you type before it gets sent.

**Download: [latest release](https://github.com/rhyssniffing-coder/LingoChat/releases/latest)** (needs [Fabric API](https://modrinth.com/mod/fabric-api))

## Using it

- **Translate a message**: open chat (`T`), then click the `translate` line under the message you want. It swaps over after a split second. Click `original` to flip back.
- **Settings**: press `U` to open them (rebindable in Controls, under the LingoChat category). From there you pick the language incoming messages translate into, whether your own messages get auto-translated and into what language, and whether incoming chat should just translate automatically without clicking anything.

Translations go through Google Translate's public endpoint, so it needs an internet connection. Anything you send is translated before it leaves your computer, and repeated messages are cached so common stuff like "ok" is instant.

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for 1.21.11
2. Drop `LingoChat.jar` and [Fabric API](https://modrinth.com/mod/fabric-api) into your `mods` folder

Client-side only — you don't need it on the server, and it works fine on multiplayer servers.

## Building it yourself

```
./gradlew build
```

The jar ends up in `build/libs`. Every push also builds automatically via GitHub Actions if you'd rather grab the artifact from there.

## Limits worth knowing

- Only regular chat messages get auto-translated, not commands.
- If a translation fails (no internet, endpoint down, whatever), your message goes out exactly as you typed it.
- Minecraft servers cap chat at 256 characters, so really long translations get cut off.
- Settings are saved to `config/lingochat.json`.

See [CHANGELOG.md](CHANGELOG.md) for what's new.

MIT licensed.
