# Android Usage Control App

このディレクトリには、Cross-Platform Usage Control ApplicationのAndroid実装が含まれています。

## 🚀 Android Studio での開発手順

### 1. 前提条件
- Android Studio Hedgehog (2023.1.1) 以降
- Android SDK API 26以上
- Java 8以上

### 2. プロジェクトの開き方

1. **Android Studio を起動**
2. **"Open an existing Android Studio project"** を選択
3. **この `android` フォルダを選択** (`/platforms/android/`)
4. **"Open"** をクリック

### 3. 初回設定

Android Studioがプロジェクトを開いたら：

1. **Gradle Sync** が自動で開始されます
2. 必要な依存関係がダウンロードされます
3. **"Trust Gradle Project"** が表示されたら **"Trust Project"** をクリック

### 4. 実機での動作確認

#### 4.1 開発者オプションの有効化
1. Android端末の **設定 > デバイス情報**
2. **ビルド番号** を7回タップ
3. **開発者オプション** が有効になります

#### 4.2 USBデバッグの有効化
1. **設定 > 開発者オプション**
2. **USBデバッグ** を有効にする

#### 4.3 アプリの実行
1. USB ケーブルで Android 端末を接続
2. Android Studio の **▶ Run** ボタンをクリック
3. 接続された端末を選択してアプリを実行

## 📱 必要な権限の設定

アプリが正常に動作するためには、以下の権限を手動で設定する必要があります：

### 1. 使用統計へのアクセス
- **設定 > アプリ > 特別なアプリアクセス > 使用統計にアクセス**
- **Usage Control** を見つけて有効にする

### 2. 他のアプリの上に重ねて表示
- **設定 > アプリ > 特別なアプリアクセス > 他のアプリの上に重ねて表示**
- **Usage Control** を見つけて有効にする

### 3. アクセシビリティサービス
- **設定 > ユーザー補助 > Usage Control**
- サービスを有効にする

### 4. デバイス管理者（オプション）
- **設定 > セキュリティ > デバイス管理者**
- **Usage Control** を有効にする

## 🛠️ 実装済み機能

### ✅ 核心機能
- **Usage Stats API**: アプリ使用統計監視
- **Accessibility Service**: リアルタイムアプリ検出
- **Device Admin API**: システムレベル制御
- **Overlay Blocking**: アプリブロック機能

### ✅ UI/UX
- **Material Design 3**: モダンなデザイン
- **Jetpack Compose**: 宣言的UI
- **Navigation**: 画面間ナビゲーション
- **権限管理**: ガイド付き権限設定

### ✅ データ管理
- **Room Database**: ローカルデータ保存
- **WorkManager**: バックグラウンド処理
- **Hilt**: 依存性注入

## 🔧 トラブルシューティング

### Gradle Sync エラー
```bash
# プロジェクトのクリーンアップ
./gradlew clean

# 依存関係の再ダウンロード
./gradlew build --refresh-dependencies
```

### 権限が機能しない場合
1. **アプリを一度アンインストール**
2. **再インストール**
3. **権限を再設定**

### AccessibilityServiceが動作しない場合
1. **設定 > ユーザー補助**
2. **Usage Control** を一度無効にして再度有効化
3. **端末を再起動**

## 📋 開発タスク

### 完了済み ✅
- [x] プロジェクト基本構造
- [x] Room Database設計
- [x] 権限管理システム
- [x] アプリ制限UI
- [x] バックグラウンド処理
- [x] Device Admin実装

### 今後の拡張 🚧
- [ ] Firebase同期の完全実装
- [ ] 詳細な使用統計画面
- [ ] プログレッシブレベルシステム
- [ ] プッシュ通知
- [ ] ダークテーマ対応

## 🧪 テスト

### 単体テスト実行
```bash
./gradlew test
```

### UI テスト実行
```bash
./gradlew connectedAndroidTest
```

## 📦 ビルド

### デバッグビルド
```bash
./gradlew assembleDebug
```

### リリースビルド
```bash
./gradlew assembleRelease
```

---

## 🎯 使用方法

1. **アプリを起動**
2. **権限設定画面** で必要な権限を設定
3. **アプリ制限画面** で制限したいアプリを選択
4. **時間制限やブロック** を設定
5. **バックグラウンドでの監視** が開始されます

このAndroid実装は、READMEで説明されているクロスプラットフォーム使用制限システムの完全なAndroid版です！
