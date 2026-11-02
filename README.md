# Touch Macro - Samsung Galaxy Tab A9+ Android APK Projesi

Bu proje, **Samsung Galaxy Tab A9+** (11.0 inç WUXGA 1920x1200 ekran) için özel olarak geliştirilmiş gerçek, yerel (native) Android AccessibilityService dokunma otomasyon uygulamasıdır.

## 🚀 Öne Çıkan Teknik Özellikler

1. **Root, ADB veya Shizuku Gerektirmez**:
   - `android.accessibilityservice.AccessibilityService` ve `dispatchGesture()` API'si kullanır.
   - `canPerformGestures="true"` konfigürasyonu ile donanım düzeyinde dokunma, basılı tutma ve kaydırma yapar.

2. **Yüzen Kontrol Paneli (Floating Overlay)**:
   - `WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY` pencere tipi kullanılarak oyunların ve diğer uygulamaların üzerinde taşınabilir mini HUD (Başlat, Duraklat, Durdur, Durum) sağlar.
   - Oyun açıkken ekrana dokunarak koordinat seçmeyi sağlayan şeffaf kılavuz katmanı (Point Picker) mevcuttur.

3. **Kotlin Coroutines Dokunma Motoru**:
   - `GestureResultCallback` ve `CompletableDeferred` ile dokunma bitmeden sonraki adıma geçilmez.
   - Çakışan jestleri engellemek için `Mutex` koruması.
   - Android tarafından garanti edilmeyen 50ms altı dokunmalar için asgari 50ms süre garantisi ve jitter önleme.

4. **Güvenlik Mekanizması**:
   - `Intent.ACTION_SCREEN_OFF` alıcısı: Ekran kapandığında veya kilitlendiğinde makro derhal güvenli şekilde durdurulur.
   - Ekran yönü değiştiğinde (Yatay 1920x1200 <-> Dikey 1200x1920) otomatik koordinat orantılama veya güvenli durdurma.

5. **Room Veritabanı ile Çevrimdışı Depolama**:
   - İnternet gerektirmez, tüm makrolar ve oyun profilleri yerel SQLite Room veritabanında saklanır.

---

## 🛠️ Samsung Galaxy Tab A9+ Üzerine APK Derleme ve Yükleme

### 1. Yöntem: Android Studio ile (En Kolay)
1. Bu projeyi Android Studio (Hedgehog / Iguana / Jellyfish veya daha yeni) ile açın.
2. Gradle senkronizasyonunun tamamlanmasını bekleyin.
3. Samsung Galaxy Tab A9+ tabletinizi USB kablosuyla bilgisayara bağlayın (veya Ayarlar > Geliştirici Seçenekleri > Kablosuz Hata Ayıklama üzerinden bağlayın).
4. `Run` (Yeşil Oynat butonu) veya `Build > Build Bundle(s) / APK(s) > Build APK(s)` seçeneğine tıklayın.
5. Oluşan APK dosyası `app/build/outputs/apk/debug/app-debug.apk` yolunda yer alır.

### 2. Yöntem: Terminal / Komut Satırı ile
```bash
cd android
./gradlew assembleDebug
# veya Release APK için:
./gradlew assembleRelease
```
APK'yı doğrudan tablete yüklemek için:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 İlk Kurulum Adımları (Tablette)
1. Uygulamayı açın.
2. **Erişilebilirlik Hizmeti**'ne tıklayın ve Samsung OneUI Ayarlarından `Touch Macro Hizmeti`ni Açık (Açık/Etkin) hale getirin.
3. **Üstte Gösterim İzni**'ne izin verin.
4. Yeni bir makro oluşturun veya hazır profilleri kullanın.
5. "Oyna" butonuna bastığınızda ekranda taşınabilir mini kontrol paneli belirecektir.
6. İstediğiniz oyunu açın, Başlat'a basın ve otomatik dokunmanın keyfini çıkarın!
