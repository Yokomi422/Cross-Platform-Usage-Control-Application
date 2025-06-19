# Android Studio トラブルシューティングガイド

## 🔧 Android端末がStudioで認識されない場合の対処法

### ステップ1: 基本確認
端末は正常に認識されています：
```
デバイスID: 00167152S002458
状態: device
```

### ステップ2: Android Studio での対処

#### A. プロジェクトをAndroid Studioで開く
1. Android Studio起動
2. "Open an existing Android Studio project"
3. このディレクトリを選択: `/Users/toaster/Documents/project/Cross-Platform-Usage-Control-Application/platforms/android/`

#### B. Gradle Syncエラーが発生した場合
1. **File > Project Structure > Project**
   - Gradle Version: 8.4
   - Android Gradle Plugin Version: 8.2.0

2. **File > Sync Project with Gradle Files** をクリック

3. エラーが続く場合:
   - **Build > Clean Project**
   - **Build > Rebuild Project**

#### C. デバイスが認識されない場合
1. **View > Tool Windows > Logcat** を開く
2. デバイス選択ドロップダウンで端末を確認
3. 認識されない場合:
   - **Tools > SDK Manager**
   - **SDK Tools** タブで **Google USB Driver** をインストール

### ステップ3: 端末側の設定確認

#### Android端末で以下を確認:
1. **開発者オプション** が有効
2. **USBデバッグ** が有効
3. USB接続時の**ファイル転送モード**を選択
4. **RSAキーフィンガープリント**の確認ダイアログで「常に許可」

### ステップ4: Manual Run方法

Android Studioでビルドして直接インストール:

1. **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. APKファイルが生成されたら:
   ```bash
   # Terminal で実行
   ~/Library/Android/sdk/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### ステップ5: 代替方法 - Command Line Build

```bash
# プロジェクトディレクトリで実行
./gradlew assembleDebug

# APKをインストール
~/Library/Android/sdk/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk

# アプリを起動
~/Library/Android/sdk/platform-tools/adb shell am start -n com.usagecontrol.android/.MainActivity
```

## 🚨 緊急時の対処法

### もしGradleがどうしても動かない場合:

1. **Android Studio内で新しいプロジェクトを作成**
2. **Empty Activity with Compose** を選択
3. **既存のコードをコピー&ペースト**して移植

### デバイス認識の最終手段:

```bash
# adbサーバーを完全リセット
~/Library/Android/sdk/platform-tools/adb kill-server
~/Library/Android/sdk/platform-tools/adb start-server

# デバイスを再確認
~/Library/Android/sdk/platform-tools/adb devices
```

---

## ✅ 現在の状況
- Android端末: ✅ 正常認識 (00167152S002458)
- adbコマンド: ✅ 動作中
- Gradleプロジェクト: ⚠️ 設定中

次のステップ: **Android Studioでプロジェクトを開いてGradle Syncを実行**
