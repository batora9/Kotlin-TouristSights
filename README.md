# Kotlin-TouristSights

Kotlinで作ったツーリスト情報アプリの拡張。『はじめてのAndroidプログラミング　第5版』に紹介されているサンプルアプリを拡張したものです。

(主に埼玉大学生)本レポジトリ内のコードをコピーすることは禁止です。

THIS REPOSITORY IS USED FOR MY WORK TO MANAGE. I DON'T RECOMMEND YOU TO COPY ANY PART OF THIS CODE. 

Copyright 2025 batora All rights reserved.

## 環境変数の設定

このアプリは、[Google Maps API](https://developers.google.com/maps?hl=ja)を使用しています。このAPIを利用するためには、ローカル環境でAPIキーを設定する必要があります。

1. 事前にGoogle Maps PlatformからAPIキーを取得します。
2. `.env.example`を`.env`にリネームしてコピーします。
3. `GOOGLE_MAPS_API_KEY`にあなたが取得したAPIキーを入力します。

## 機能

- 観光地名または説明のキーワード検索
- 観光地の種類によるフィルター
- Google Maps APIを利用して観光地の位置情報をマップに表示
- 観光地の追加
  - カメラを起動して撮影した写真を使えるようにする
  - 位置情報による経緯度の自動取得
- 観光地の削除（非表示）
