# Deutsch B1 Trainer

تطبيق Android بسيط لتعلّم الألمانية مستوى B1.

## المزايا
- درس يومي قصير
- 20 مفردة B1 داخل جمل
- نص قراءة B1 مع أسئلة وحلول
- اختبار تفاعلي من 5 أسئلة
- يعمل دون إنترنت
- GitHub Actions لبناء APK تلقائيًا

## بناء APK عبر GitHub
عند رفع المشروع إلى فرع `main` سيبدأ Workflow اسمه **Build Android APK**.
بعد نجاحه افتح **Actions** ثم آخر تشغيل، ومن قسم **Artifacts** حمّل `German-B1-APK`.

## إعدادات البناء
- Android Gradle Plugin: 8.7.0
- Gradle: 8.9
- Java: 17
- compileSdk / targetSdk: 35
- minSdk: 24
