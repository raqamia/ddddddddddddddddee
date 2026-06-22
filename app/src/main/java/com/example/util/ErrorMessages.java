package com.example.util;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessages {
    private static final Map<String, String> MESSAGES = new HashMap<>();

    static {
        // أخطاء المصادقة
        MESSAGES.put("invalid_grant",                "البريد الإلكتروني أو كلمة المرور غير صحيحة");
        MESSAGES.put("user_already_exists",          "هذا البريد الإلكتروني مسجّل مسبقاً");
        MESSAGES.put("email_not_confirmed",          "يرجى تأكيد بريدك الإلكتروني أولاً");
        MESSAGES.put("email_confirmation_required",  "تم إنشاء حسابك! راجع بريدك الإلكتروني لتفعيل الحساب");
        MESSAGES.put("weak_password",                "كلمة المرور يجب أن تكون ٨ أحرف على الأقل");
        MESSAGES.put("token_expired",                "انتهت جلستك، يرجى تسجيل الدخول مجدداً");

        // أخطاء الشبكة والملفات
        MESSAGES.put("network_error",                "تحقق من اتصالك بالإنترنت");
        MESSAGES.put("file_not_found",               "الملف غير موجود على الجهاز");
        MESSAGES.put("pdf_render_error",             "حدث خطأ أثناء فتح الملف");
        MESSAGES.put("download_failed",              "فشل تحميل الملف");
        MESSAGES.put("signed_url_failed",            "فشل الحصول على رابط التحميل");

        // افتراضي
        MESSAGES.put("default",                      "حدث خطأ، حاول مرة أخرى");
    }

    public static String get(String code) {
        return MESSAGES.getOrDefault(code, MESSAGES.get("default"));
    }
}