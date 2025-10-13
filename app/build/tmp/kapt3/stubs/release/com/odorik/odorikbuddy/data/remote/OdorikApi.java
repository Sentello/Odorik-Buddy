package com.odorik.odorikbuddy.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001JF\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00042\b\b\u0001\u0010\u0006\u001a\u00020\u00042\b\b\u0001\u0010\u0007\u001a\u00020\u00042\b\b\u0001\u0010\b\u001a\u00020\u00042\b\b\u0001\u0010\t\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\nJ(\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00042\b\b\u0001\u0010\u0006\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\fJ\"\u0010\r\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u00042\b\b\u0001\u0010\u0006\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\fJ2\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00042\b\b\u0001\u0010\u0006\u001a\u00020\u00042\b\b\u0001\u0010\t\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0010J.\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u00120\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00042\b\b\u0001\u0010\u0006\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\fJT\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00042\b\b\u0001\u0010\u0006\u001a\u00020\u00042\b\b\u0001\u0010\b\u001a\u00020\u00042\b\b\u0001\u0010\u0015\u001a\u00020\u00042\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u00042\n\b\u0003\u0010\u0017\u001a\u0004\u0018\u00010\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0018\u00a8\u0006\u0019"}, d2 = {"Lcom/odorik/odorikbuddy/data/remote/OdorikApi;", "", "call", "Lretrofit2/Response;", "", "user", "password", "caller", "recipient", "line", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllowedSenders", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCredit", "getLineInfo", "Lcom/odorik/odorikbuddy/data/model/LineInfo;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLines", "", "Lcom/odorik/odorikbuddy/data/model/Line;", "sendSms", "message", "sender", "delayed", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public abstract interface OdorikApi {
    
    @retrofit2.http.GET(value = "sms/allowed_sender")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllowedSenders(@retrofit2.http.Query(value = "user")
    @org.jetbrains.annotations.NotNull()
    java.lang.String user, @retrofit2.http.Query(value = "password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.String>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "sms")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendSms(@retrofit2.http.Field(value = "user")
    @org.jetbrains.annotations.NotNull()
    java.lang.String user, @retrofit2.http.Field(value = "password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String password, @retrofit2.http.Field(value = "recipient")
    @org.jetbrains.annotations.NotNull()
    java.lang.String recipient, @retrofit2.http.Field(value = "message")
    @org.jetbrains.annotations.NotNull()
    java.lang.String message, @retrofit2.http.Field(value = "sender")
    @org.jetbrains.annotations.Nullable()
    java.lang.String sender, @retrofit2.http.Field(value = "delayed")
    @org.jetbrains.annotations.Nullable()
    java.lang.String delayed, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.String>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "callback")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object call(@retrofit2.http.Field(value = "user")
    @org.jetbrains.annotations.NotNull()
    java.lang.String user, @retrofit2.http.Field(value = "password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String password, @retrofit2.http.Field(value = "caller")
    @org.jetbrains.annotations.NotNull()
    java.lang.String caller, @retrofit2.http.Field(value = "recipient")
    @org.jetbrains.annotations.NotNull()
    java.lang.String recipient, @retrofit2.http.Field(value = "line")
    @org.jetbrains.annotations.NotNull()
    java.lang.String line, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.String>> $completion);
    
    @retrofit2.http.GET(value = "balance")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCredit(@retrofit2.http.Query(value = "user")
    @org.jetbrains.annotations.NotNull()
    java.lang.String user, @retrofit2.http.Query(value = "password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    @retrofit2.http.GET(value = "lines.json")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLines(@retrofit2.http.Query(value = "user")
    @org.jetbrains.annotations.NotNull()
    java.lang.String user, @retrofit2.http.Query(value = "password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.odorik.odorikbuddy.data.model.Line>>> $completion);
    
    @retrofit2.http.GET(value = "line_info.json")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLineInfo(@retrofit2.http.Query(value = "user")
    @org.jetbrains.annotations.NotNull()
    java.lang.String user, @retrofit2.http.Query(value = "password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String password, @retrofit2.http.Query(value = "line")
    @org.jetbrains.annotations.NotNull()
    java.lang.String line, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.odorik.odorikbuddy.data.model.LineInfo>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}