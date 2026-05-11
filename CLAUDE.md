# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
flutter run                              # Run on connected device
flutter build apk --debug                # Debug APK
flutter build apk --release              # Release APK
flutter clean && flutter pub get         # Clean and restore dependencies
flutter analyze                          # Dart lint/analyze
flutter test                             # Run tests
flutter test test/widget_test.dart       # Run single test file
```

### Debugging on device

```bash
adb logcat | grep ApolloCardReader       # Filter SDK logs
adb logcat | grep "b \|c "               # SpectraTech AIDL service logs
```

## Architecture

This is a Flutter app that integrates with **SpectraTech Apollo Card Reader SDK** for payment card reading on Android POS terminals.

### Communication Pattern

```
Flutter (Dart)  ←→  MethodChannel/EventChannel  ←→  Android (Kotlin)  ←→  SpectraTech SDK (AAR)
```

- **MethodChannel** `com.apollo.cardreader/payment` — Flutter → Android calls (`startTransaction`, `stopTransaction`, `sendPin`, `sendConfirmation`)
- **EventChannel** `com.apollo.cardreader/events` — Android → Flutter event stream (card detection, EMV data, errors, status updates)

### Key Files

- `lib/main.dart` — Flutter UI with event listener and transaction controls
- `android/app/src/main/kotlin/com/apollo/apollo_card_reader/MainActivity.kt` — Native Android activity implementing `TransactionFlowController.TransactionFlowDelegate` for SDK callbacks

### SpectraTech SDK (AAR files in `android/app/libs/`)

Three AAR libraries loaded via `fileTree` in `build.gradle.kts`:

- **apollo-cardreader-sdk** v0.04.39 — Main card reader SDK
- **level3** v1.03.01 — UI/support library
- **printercontrollers** v0.00.37 — Receipt printing

### SDK Class Hierarchy

```
SPDeviceController (device connection/info)
  └── PeddllAppController (PED service binding)
      └── BaseCardController (card detection, CTL, TLV decode)
          └── TransactionFlowController (full EMV transaction flow)
```

### Card Reading Flow (Two-Phase)

1. **Phase 1**: `detectCardInteraction()` with `SWIPE_OR_INSERT` mode and 3-second hardware stabilization delay after connection
2. **Phase 2**: On `INSERTED_CARD` detection, `startTransactionFlow()` is called with card already present to initiate EMV processing

### Delegate Callback Sequence (TransactionFlowDelegate)

The SDK calls these delegate methods in order during a transaction:

```
onControllerConnected → onCardInteractionDetecting → onCardInteractionDetected
  → onSelectAIDRequested (auto-select index 0)
  → onSetAmountRequest → onPinEntryRequested (if needed)
  → onConfirmationRequested (TLV data, tag 5A = PAN)
  → onOnlineProcessRequested → onBatchDataReceived
  → onTransactionStatusReceived
```

Error/abort paths: `onError`, `onDetectCardInteractionAborted`, `onMessageReceived(TRANSACTION_TERMINATED)`

### SDK Pitfalls

- `CHKCRD_TIMEOUT` value must be `String` (`"60"`), not `Integer` — SDK throws `ClassCastException` otherwise
- `startTransactionFlow()` fails immediately (`NOT_READY` → `TERMINATED`) if called without a card present in the reader, even after the 3-second hardware delay. The EMV kernel appears uninitialized without a card.
- `detectCardInteraction()` works for card detection but the session terminates before `getEmvCardData()` can return data for chip cards. For MSR (swiped) cards, track data is returned in the detection hashtable.
- `startTransactionFlow()` requires `TRANSACTIONTYPE` (mandatory — fails with `CONDITION_NOT_SATISFIED` if missing), `AMOUNT`, and `CURRENCYCODE` (4-digit padded string, e.g. `"0840"` for USD).
- CTL LED events (`NOT_READY`, `PROCESSING`, etc.) only fire AFTER `startTransactionFlow()` is called, not before.
- No SDK documentation is available; the API surface was discovered by decompiling the AAR files.

## Platform Requirements

- Android 7.1.1+ (API 25 min), target API 34
- Java 17
- USB-connected Apollo POS terminal (msm8909 hardware)
- SDK binds to `com.spectratech.mpos.peddll.PeddllAidlService` via AIDL

## Notes

- UI text and log messages are in Spanish
- The project is a demo/integration test app, not production code
- No iOS target — Android only
