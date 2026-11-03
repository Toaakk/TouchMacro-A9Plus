# TouchMacro-A9Plus — Değişiklik Geçmişi

Bu dosya, uygulamada yapılan önemli değişiklikleri ve geliştirme aşamalarını takip etmek için oluşturulmuştur.

## 3 Kasım 2026 — Build #13

**Durum:** Derleme başarılı.

### Yapılan değişiklikler

* `MacroEngine.kt` dosyasındaki Kotlin sözdizimi hataları düzeltildi.
* `when` bloğundaki fazladan kapanış parantezi kaldırıldı.
* `WAIT` adımının bekleme mantığı düzenlendi.
* `dispatchGesture()` çağrısı Android ana iş parçacığına taşındı.

### Sonuç

* GitHub Actions derlemesi başarıyla tamamlandı.
* APK oluşturuldu.
* Gerçek cihaz testi henüz yapılmadı.

## Önceki geliştirmeler

### Android SDK

* GitHub Actions SDK kurulum hatası giderildi.

### Android kaynak dosyaları

* `themes.xml` dosyasındaki XML hataları düzeltildi.
* Tema özniteliği sorunu giderildi.
* `accessibility_service_config.xml` dosyasındaki XML bildirimi düzeltildi.

### İlk başarılı APK

* GitHub Actions üzerinden APK oluşturuldu.
* Samsung Galaxy Tab A9+ üzerinde kurulum denendi.
* Google Play Protect önceki APK'nın kurulumunu engelledi.

## Bilinen sorunlar

* Build #13 için gerçek cihaz testi bekleniyor.
* Erişilebilirlik hizmetinin çalışması doğrulanmadı.
* Makro motorunun dokunma ve kaydırma davranışları test edilmedi.
* RELEASE işlemi ayrı bir bırakma mekanizması olarak uygulanmadı.
* Makro duraklatma ve güvenli durdurma mekanizmaları geliştirilmeli.

## Sonraki hedefler

1. Build #13 APK'sını tablette test etmek.
2. Kurulum ve erişilebilirlik hizmetini doğrulamak.
3. Dokunma, uzun basma ve kaydırma testlerini yapmak.
4. Makro motorunun çalışma hatalarını gidermek.
5. FRep2 benzeri gelişmiş özellikleri geliştirmek.

---

**Not:** Derleme başarısı, uygulamanın gerçek cihazda sorunsuz çalıştığı anlamına gelmez.
