# Tamboui FZF Selector Demo

A small terminal UI app built with Tamboui that mimics a lightweight FZF-style selector.

This project is mostly educational: it demonstrates a clear MVC split and practical key bindings in Tamboui.

## Features

- Incremental filtering over a sample dataset
- Keyboard-first navigation (`Up` and `Down`)
- Select with `Enter`
- Clear filter with `Esc`
- Delete backward with `Ctrl+H`

## Tech

- Java 17
- [Tamboui Toolkit](https://github.com/tamboui/tamboui)
- JBang script-style execution

## Run

```bash
jbang TamboFzfSelectorApp.java
```

## JBang Wrapper

This repository includes the JBang wrapper (`jbang`, `jbang.cmd`, `jbang.ps1`), so contributors can run with a repo-local JBang launcher.

Wrapper was installed with:

```bash
jbang wrapper install
```

Run using the wrapper:

```bash
# macOS/Linux
./jbang TamboFzfSelectorApp.java

# Windows (cmd)
jbang.cmd TamboFzfSelectorApp.java

# Windows (PowerShell)
.\jbang.ps1 TamboFzfSelectorApp.java
```

Exit code behavior:

- `0` when an item is selected (prints selected value)
- `1` when the app is exited without selecting

## Why this repo exists

I used this project to understand Tamboui bindings and event handling in a practical, minimal app.
