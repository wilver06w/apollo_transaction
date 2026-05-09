# Apollo Card Reader - Flutter

Proyecto Flutter de demostración para integración con lectores de tarjetas Apollo/Spectra.

## Características

- Lectura de tarjetas EMV (chip)
- Lectura de tarjetas contactless (NFC/tap)
- Lectura de bandas magnéticas
- Captura de datos de tarjeta (PAN, fecha de expiración, etc.)
- Interfaz visual simple para pruebas

## Requisitos

- Dispositivo Android con Android 7.1.1 (API 25) o superior
- Lector de tarjetas Apollo/Spectra (S1 o compatible)
- Flutter SDK 3.19 o superior
- Android SDK

## Configuración

### 1. Dependencias del SDK

El proyecto incluye los archivos AAR del SDK de Apollo en:
```
android/app/libs/
├── com.spectratech.apollo-cardreader-sdk-0.04.39-*.aar
├── printercontrollers-0.00.37-apollotappos.aar
└── level3-1.03.01-*.aar
```

### 2. Permisos Android

Los permisos necesarios están configurados en `AndroidManifest.xml`:
- `USB_PERMISSION` - Para comunicación con dispositivos USB
- `usb.host` feature - Para usar el USB Host API

### 3. Canales de Flutter

**MethodChannel:** `com.apollo.cardreader/payment`
- `startTransaction` - Inicia la lectura de tarjeta
- `stopTransaction` - Detiene la transacción actual
- `sendPin` - Envía el PIN del tarjetahabiente
- `sendConfirmation` - Confirma la transacción

**EventChannel:** `com.apollo.cardreader/events`
Eventos emitidos durante el proceso:
- `connected` - Controlador conectado
- `disconnected` - Controlador desconectado
- `detecting` - Detectando tarjeta
- `cardDetected` - Tarjeta detectada
- `emvCardData` - Datos EMV recibidos (incluye PAN)
- `confirmationRequested` - Confirmación requerida
- `transactionStatus` - Estado final de la transacción
- `error` - Error occurred

## Uso

### Ejecutar la aplicación:

```bash
cd apollo_card_reader
flutter run
```

### Flujo de la aplicación:

1. Ingresa el monto de la transacción
2. Presiona "Iniciar Lectura"
3. Presenta la tarjeta:
   - **Banda magnética:** Desliza la tarjeta
   - **Chip:** Inserta la tarjeta en el lector
   - **NFC:** Acerca la tarjeta al lector (tap)
4. La aplicación mostrará los datos capturados
5. Confirma la transacción cuando sea solicitado
6. Revisa los logs para ver el proceso detallado

## Arquitectura

### Android (Kotlin)
- `MainActivity.kt` - Implementa `TransactionFlowController.TransactionFlowDelegate`
- Maneja la comunicación con el SDK de Apollo
- Usa MethodChannel y EventChannel para comunicarse con Flutter

### Flutter (Dart)
- `main.dart` - Interfaz de usuario y lógica de la aplicación
- Escucha eventos del código nativo
- Muestra estado y logs en tiempo real

## Estructura del Proyecto

```
apollo_card_reader/
├── lib/
│   └── main.dart              # Código Flutter
├── android/
│   ├── app/
│   │   ├── libs/              # AAR files del SDK
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       └── kotlin/.../MainActivity.kt
│   └── build.gradle.kts
└── pubspec.yaml
```

## Solución de Problemas

### No se detecta el lector:
- Verifica que el lector esté conectado vía USB
- Confirma que los permisos USB estén concedidos
- Revisa el logcat con: `adb logcat | grep ApolloCardReader`

### Error de compilación:
- Asegúrate de tener el NDK de Android configurado
- Verifica que los archivos AAR estén en `android/app/libs/`
- Limpia y rebuild: `flutter clean && flutter pub get`

### La app se cierra:
- Revisa el logcat para errores del SDK
- Verifica que el dispositivo tenga Android 7.1.1 o superior

## Referencias

- SDK de Apollo/Spectra v1.64
- Documentación de Flutter Platform Channels
- Especificación EMV Contactless

## Licencia

Este proyecto es solo para demostración y pruebas de integración.
# apollo_transaction
