# Dino Home · Android

Приложение телефона: настройка экрана, календари и музыка через домашнюю веб-консоль. Без Accessibility и привилегированных разрешений.

## Вход

1. Укажите свой `https://home.example.com`.
2. На авторизованном телефоне или в мини-приложении бота: «Ещё → Код для моего телефона».
3. Введите одноразовый код в новом приложении.

Google открывается во внешнем браузере. Кнопка «Сервер» меняет адрес и очищает локальную веб-авторизацию. Пульт доступен только при `TV_REMOTE_ENABLED=true`.

## Сборка

JDK 17, Android SDK 37 (сборка против 37, targetSdk остаётся 35); телефон Android 8/API 26+.

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
pre-commit install
pre-commit run --all-files
```

Windows: `gradlew.bat`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

Перед распространением настройте release-подпись вне Git. На реальном телефоне проверьте вход, выбор фото и возврат из браузера.

Готовый APK — в [релизах](https://github.com/dinosaur-tv/android-app/releases/latest); [как установить](https://github.com/dinosaur-tv/.github/blob/main/docs/INSTALL.md).

Исходники — [MIT](LICENSE), Gradle Wrapper — Apache-2.0.
