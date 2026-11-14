# Mac Pen Remote

An independent Android fork of [Open Screen Sharing](https://github.com/nekohasekai/open-screen-sharing), based on commit `1241a8337625869b912c898e72d9aa71a8301b27`. This fork is not affiliated with the upstream author or Edovia Screens. The original GPL-3.0-or-later license and attribution remain in [LICENSE](LICENSE).

## Pen controls

- Pen tip without the side button: normal left click and drag.
- Hold the primary pen side button, then touch and lift: right click.
- Hold the primary pen side button, then drag: vertical or horizontal scrolling, without opening a context menu.
- Near-screen hover: press and release the side button without moving for a right click; hold it and move the pen above the screen to scroll. This can be disabled separately.
- Releasing the button stops scrolling. A cancelled contact is consumed until pen-up, so it cannot become an accidental left drag.
- Hover exit, pen contact and window focus loss cancel pending hover actions. Lifting the pen after a contact cannot produce an additional hover right click from the same held button.
- A movement threshold prevents hand jitter from turning a right click into scrolling. Cancellation never generates a right click.
- Settings → Control → S Pen side-button scrolling restores the original right-button drag behavior when disabled.
- Settings → Control also offers pen scrolling at 0.5×, 1× or 2× speed and an optional reversed direction, applying to both hover and tip scrolling.
- Tip scrolling uses normalized pressure for a smoothed speed adjustment. Hover scrolling accelerates with movement speed. Both have optional direction locking to reduce sideways drift.
- A 2.5× local magnifier appears after a 350 ms hover pause without the side button and follows slow movements. It uses the existing GPU picture rather than downloading extra frames.
- Optional palm protection cancels existing finger gestures and rejects new fingers while the pen is hovering or touching, plus 150 ms after leaving. It can be disabled for mixed finger/pen input.
- A stationary 650 ms hover button hold opens a quick menu by default. Hold and double-click can be mapped to the menu, keyboard, toolbar, Mission Control or Show Desktop. Double-click is off by default to retain instant single right-clicks; enabling it adds a 300 ms single-click delay.
- Settings → Control → Test pen sensors provides a local test grid for pressure, tilt, orientation, distance and side-button events. It sends no input to the Mac and keeps samples only in memory. Advertised tilt/distance axes are not automatically treated as usable controls.
- Window loss, detach and session replacement cancel queued pen actions, preventing old holds or clicks from affecting a later connection.
- If Samsung intercepts the side button, disable **Open Air command with Pen button** in the device's S Pen settings. The exact name varies by One UI version.

These controls work through ordinary VNC and Apple VNC. Pressure changes scrolling locally; pressure and tilt are not forwarded as drawing tablet input to the Mac. Real S Pen hardware validation is still required. See [Chinese controls and validation notes](docs/s-pen-controls.md).

**Galaxy S25 Ultra:** Samsung removed Bluetooth/BLE and Air Actions from its S Pen. This fork uses Android's near-screen stylus hover/button events; it cannot receive distant button presses or motion gestures. See [Samsung's S25 Ultra S Pen guidance](https://www.samsung.com/us/support/answer/ANS10004602/). Actual hover-button delivery depends on One UI and its Air Command settings; test on the phone before relying on it.

## High Performance recovery

An authenticated connection now waits up to 12 seconds for its first displayed picture. This includes negotiation, incoming video, decoding and presentation. When it times out, or the media stream reports a network/silence failure, the Android client reconnects once using standard VNC and shows a notice. It keeps the saved High Performance preference, so a later session can try it again. A disconnected/replaced session cannot trigger an obsolete timeout. Background sessions wait until the app returns to the foreground before recovering.

This is recovery, not UDP tunneling: it cannot make Apple's High Performance media cross an incompatible TCP-only forwarding gateway. See [network notes](docs/high-performance-forwarding.md).

## Build and validate

Requires the upstream Android toolchain (JDK 17 or later, Android SDK 37, Android NDK) and Go 1.26.8 or later.

```sh
make lib_android
./gradlew :OpenScreenSharingAndroid:assembleDebug :OpenScreenSharingAndroid:testDebugUnitTest :OpenScreenSharingAndroid:lintDebug
```

The app installs as `io.github.zhiisland.macpenremote`, with the name **Mac Pen Remote** and a distinct icon. It can coexist with the original app. Debug APKs are test builds, not production-signed releases. Source and corresponding APKs should be distributed together under the upstream license.
