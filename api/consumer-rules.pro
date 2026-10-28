# SuperLyricApi 4.0 consumer rules.
#
# v4 的线格式由反射读取 @Wire 字段，调用方的 R8 若裁掉这些字段，
# 编解码会在运行期静默丢数据。

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# 保留所有被 @Wire 标注的字段
-keepclassmembers,allowobfuscation class com.hchen.superlyricapi.** {
    @com.hchen.superlyricapi.wire.Wire <fields>;
}

# 保留注解本身
-keep @interface com.hchen.superlyricapi.wire.Wire

# 保留 AIDL 生成的接口与 Stub
-keep interface com.hchen.superlyricapi.** { *; }
-keep class com.hchen.superlyricapi.**$Stub { *; }
-keep class com.hchen.superlyricapi.**$Stub$Proxy { *; }

# 保留公开入口
-keep class com.hchen.superlyricapi.SuperLyric { public *; }
-keep interface com.hchen.superlyricapi.Publisher { *; }
-keep interface com.hchen.superlyricapi.Receiver { *; }
-keep interface com.hchen.superlyricapi.LyricListener { *; }