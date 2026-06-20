# تطبيق منارة - Manara

تطبيق أندرويد تعليمي لطلاب التوجيهي (الثانوية العامة)، يحتوي على كتب وامتحانات سابقة وملخصات مع إمكانية التحميل والعرض بدون إنترنت.

## التقنيات المستخدمة

- **لغة البرمجة:** Java
- **قاعدة بيانات محلية:** Room
- **API:** Supabase (PostgREST + Auth + Storage)
- **الشبكات:** Retrofit + OkHttp
- **مشاهد PDF:** AndroidPdfViewer
- **التشفير:** EncryptedSharedPreferences
- **التنقل:** Navigation Component
- **الأمان:** ProGuard/R8، Network Security Config

## متطلبات التشغيل

- Android Studio
- Android SDK 26+
- مشروع Supabase مع الجداول التالية:

### جداول Supabase المطلوبة

```sql
-- جدول المسارات الدراسية
CREATE TABLE public.subjects (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL,
    track TEXT NOT NULL, -- 'scientific', 'literary', 'both'
    order_index INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true
);

-- جدول الملفات
CREATE TABLE public.files (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    subject_id UUID REFERENCES public.subjects(id) ON DELETE CASCADE,
    category TEXT NOT NULL, -- 'books', 'exams', 'summaries'
    name TEXT NOT NULL,
    storage_path TEXT NOT NULL,
    size_bytes BIGINT DEFAULT 0,
    page_count INTEGER DEFAULT 0,
    uploaded_at TIMESTAMPTZ DEFAULT now()
);

-- جدول الملفات المحفوظة للمستخدمين
CREATE TABLE public.saved_files (
    user_id UUID NOT NULL,
    file_id UUID NOT NULL,
    PRIMARY KEY (user_id, file_id)
);

-- جدول الملفات الشخصية
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    name TEXT,
    track TEXT -- 'scientific' OR 'literary'
);

-- RLS: تفعيل الأمان على كل الصفوف
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.files ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.saved_files ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- السياسات: السماح للمستخدمين المصادق عليهم بالقراءة
CREATE POLICY "allow_select_for_authenticated" ON public.subjects
    FOR SELECT USING (auth.role() = 'authenticated');

CREATE POLICY "allow_select_for_authenticated" ON public.files
    FOR SELECT USING (auth.role() = 'authenticated');

CREATE POLICY "allow_select_for_own_user" ON public.saved_files
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "allow_insert_for_own_user" ON public.saved_files
    FOR INSERT WITH CHECK (auth.uid() = user_id);
```

### متغيرات البيئة

أنشئ ملف `.env` في مجلد المشروع:

```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

## التشغيل

1. افتح Android Studio
2. اختر **Open** وحدد مجلد المشروع
3. شغّل التطبيق على جهاز محاكاة أو هاتف فعلي
