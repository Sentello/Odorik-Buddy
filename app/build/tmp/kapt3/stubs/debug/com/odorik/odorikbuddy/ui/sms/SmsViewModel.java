package com.odorik.odorikbuddy.ui.sms;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B9\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\b\b\u0001\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000eJ\u0006\u0010\u001d\u001a\u00020\u001eJ*\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$2\u0012\u0010%\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020 0&J*\u0010\'\u001a\u00020\u001e2\u0006\u0010(\u001a\u00020\u00122\u0006\u0010)\u001a\u00020\u00122\b\u0010*\u001a\u0004\u0018\u00010\u00122\b\u0010+\u001a\u0004\u0018\u00010\u0012R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0013\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0019\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0018R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u001b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0018R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/odorik/odorikbuddy/ui/sms/SmsViewModel;", "Landroidx/lifecycle/ViewModel;", "sendSmsUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/SendSmsUseCase;", "getLinesUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetLinesUseCase;", "userRepository", "Lcom/odorik/odorikbuddy/data/repository/UserRepository;", "securePreferences", "Lcom/odorik/odorikbuddy/data/local/SecurePreferences;", "api", "Lcom/odorik/odorikbuddy/data/remote/OdorikApi;", "context", "Landroid/content/Context;", "(Lcom/odorik/odorikbuddy/domain/usecase/SendSmsUseCase;Lcom/odorik/odorikbuddy/domain/usecase/GetLinesUseCase;Lcom/odorik/odorikbuddy/data/repository/UserRepository;Lcom/odorik/odorikbuddy/data/local/SecurePreferences;Lcom/odorik/odorikbuddy/data/remote/OdorikApi;Landroid/content/Context;)V", "_allowedSenders", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "", "_error", "_sendResult", "allowedSenders", "Lkotlinx/coroutines/flow/StateFlow;", "getAllowedSenders", "()Lkotlinx/coroutines/flow/StateFlow;", "error", "getError", "sendResult", "getSendResult", "fetchAllowedSenders", "Lkotlinx/coroutines/Job;", "handleContactSelection", "", "contentResolver", "Landroid/content/ContentResolver;", "contactUri", "Landroid/net/Uri;", "onPhoneNumberSelected", "Lkotlin/Function1;", "sendSms", "recipient", "message", "sender", "delayed", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SmsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.SendSmsUseCase sendSmsUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase getLinesUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.local.SecurePreferences securePreferences = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.remote.OdorikApi api = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<java.lang.String>> _allowedSenders = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> allowedSenders = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _sendResult = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> sendResult = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _error = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> error = null;
    
    @javax.inject.Inject()
    public SmsViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.SendSmsUseCase sendSmsUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase getLinesUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.repository.UserRepository userRepository, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.SecurePreferences securePreferences, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.remote.OdorikApi api, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> getAllowedSenders() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSendResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job fetchAllowedSenders() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job sendSms(@org.jetbrains.annotations.NotNull()
    java.lang.String recipient, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.Nullable()
    java.lang.String sender, @org.jetbrains.annotations.Nullable()
    java.lang.String delayed) {
        return null;
    }
    
    public final void handleContactSelection(@org.jetbrains.annotations.NotNull()
    android.content.ContentResolver contentResolver, @org.jetbrains.annotations.NotNull()
    android.net.Uri contactUri, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onPhoneNumberSelected) {
    }
}