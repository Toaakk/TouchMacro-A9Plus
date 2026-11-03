# TouchMacro-A9Plus — Proje Hafızası

**Son güncelleme:** 3 Kasım 2026
**Hedef cihaz:** Samsung Galaxy Tab A9+
**Dil:** Kotlin
**Derleme:** GitHub Actions
**Son başarılı sürüm:** Build #13

## 1. Projenin Amacı

Samsung Galaxy Tab A9+ üzerinde çalışan, FRep2 benzeri gelişmiş dokunma otomasyon uygulaması geliştirmek.

Hedeflenen özellikler:

* Otomatik dokunma, uzun basma ve kaydırma.
* Makro kaydetme ve oynatma.
* Sonsuz ve belirli sayıda tekrar.
* Makro duraklatma ve devam ettirme.
* Ekrandan dokunma noktası seçme.
* Yüzen kontrol paneli.
* Makro profilleri oluşturma ve düzenleme.
* Ekran yönüne uyum ve güvenli durdurma.

Bu özelliklerin tamamı henüz gerçek cihazda doğrulanmamıştır.

## 2. Geliştirme Ortamı

GitHub deposu: Toaakk/TouchMacro-A9Plus

APK derleme dosyası: `.github/workflows/build-apk.yml`

Geliştirme bilgisayarı şirket bilgisayarıdır. Program yükleme yetkisi bulunmadığı için Android Studio veya emülatör kullanılamıyor.

Kodlar GitHub üzerinden düzenleniyor ve APK, GitHub Actions ile oluşturuluyor.

## 3. Mevcut Mimari

### TouchMacroApp.kt

* Room veritabanını başlatır.
* Bildirim kanalı oluşturur.
* Örnek makroları veritabanına ekler.

### TouchMacroAccessibilityService.kt

* Android erişilebilirlik hizmetini yönetir.
* Makro motorunu başlatır.
* Yüzen kontrol panelini yönetir.
* Ekran kapanmasını ve yön değişikliklerini takip eder.

### MacroEngine.kt

* Makro adımlarını çalıştırır.
* TAP, LONG_PRESS, RELEASE, SWIPE ve WAIT türlerini içerir.
* Kotlin Coroutines kullanır.
* Makro ilerlemesini StateFlow ile bildirir.
* Jest gönderimi için Android AccessibilityService API'sini kullanır.

### FloatingOverlayManager.kt

* Yüzen kontrol paneli ve nokta seçici için kullanılır.
* Gerçek cihaz davranışı henüz doğrulanmamıştır.

## 4. Çözülen Derleme Sorunları

1. Android SDK kurulum hatası giderildi.
2. `themes.xml` sözdizimi hatası giderildi.
3. Eksik tema özniteliği sorunu giderildi.
4. `accessibility_service_config.xml` XML bildirimi düzeltildi.
5. `MacroEngine.kt` içindeki fazladan kapanış parantezi kaldırıldı.
6. WAIT bekleme mantığı düzenlendi.
7. `dispatchGesture()` çağrısı ana iş parçacığına taşındı.

**Sonuç:** GitHub Actions Build #13 başarıyla tamamlandı.

## 5. Bilinen Sorunlar ve Kontrol Edilecek Konular

* Google Play Protect önceki APK sürümlerinin kurulumunu engelledi.
* Build #13 henüz tablette test edilmedi.
* RELEASE işlemi TAP ile aynı fonksiyona yönlendiriliyor.
* Makroların çakışma ihtimali incelenmeli.
* Uzun basma ve kaydırma sırasında durdurma davranışı kontrol edilmeli.
* Sabit 5 saniyelik jest zaman aşımı değerlendirilmeli.
* Ekran koordinatlarının doğruluğu test edilmeli.
* Sonsuz döngünün güvenli şekilde durdurulması doğrulanmalı.

## 6. Geliştirme Öncelikleri

### Aşama 1 — Kurulum ve Temel Testler

* Build #13 APK'sını tablette kurmayı dene.
* Uygulamanın açıldığını doğrula.
* Erişilebilirlik hizmetini kontrol et.
* Yüzen paneli test et.
* Tek dokunma, uzun basma ve kaydırmayı test et.

### Aşama 2 — Makro Motoru

* Makro çakışmalarını önle.
* Duraklatma ve devam ettirmeyi iyileştir.
* Güvenli durdurma mekanizmasını geliştir.
* RELEASE davranışını düzenle.
* Döngü ve zamanlama doğruluğunu test et.

### Aşama 3 — Gelişmiş Özellikler

* Makro kayıt ve oynatma.
* Çoklu dokunma desteği.
* Gelişmiş makro düzenleyici.
* Oyunlara özel profiller.
* Özelleştirilebilir yüzen kontrol paneli.

## 7. Çalışma Kuralları

1. Mevcut kaynak kodu incelemeden büyük değişiklik yapma.
2. Her seferinde az sayıda değişiklik yap.
3. Eski ve yeni kodu açıkça göster.
4. Dosya yollarını belirt.
5. GitHub üzerinden uygulanabilir adımlar ver.
6. Her değişiklikten sonra derleme sonucunu kontrol et.
7. Derleme başarısını gerçek cihaz başarısıyla karıştırma.
8. Daha önce çözülen sorunları gereksiz yere tekrar açma.
9. Hata mesajının asıl nedenini incelemeden tahminde bulunma.
10. Kullanıcıya Türkçe ve adım adım rehberlik et.

## 8. Son Durum

**Tarih:** 3 Kasım 2026

**Son başarılı derleme:** Build TouchMacro APK #13

**Mevcut aşama:** Gerçek cihaz testi bekleniyor.

**Sıradaki işlem:** Samsung Galaxy Tab A9+ üzerinde Build #13 APK'sının kurulumunu denemek.

Test sonuçlarına göre sonraki geliştirme adımları belirlenecek.
