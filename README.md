# كايبكو الشخصي – Android

تطبيق Android شخصي مبني على هيكل ملف Kakeibo_Monthly_Budget_Tracker.xlsx.

## المزايا
- واجهة عربية RTL.
- قاعدة بيانات Room محلية بدون إنترنت.
- لوحة مؤشرات للدخل والادخار والمصروفات والرصد المتبقي.
- تسجيل المصروفات حسب فئات Kakeibo الأربع.
- ميزانية شهرية.
- قائمة أغراض البيت.
- أسئلة التأمل الشهرية الأربعة.
- GitHub Actions لبناء APK تلقائياً.

## البناء
يمكن فتح المشروع في Android Studio أو تشغيل GitHub Actions عبر `workflow_dispatch`.
الناتج: `app/build/outputs/apk/debug/app-debug.apk`.
