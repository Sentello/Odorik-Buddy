package com.odorik.odorikbuddy.ui.navigation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0005\u001a\u00020\u0006R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/odorik/odorikbuddy/ui/navigation/NavigationViewModel;", "Landroidx/lifecycle/ViewModel;", "userRepository", "Lcom/odorik/odorikbuddy/data/repository/UserRepository;", "(Lcom/odorik/odorikbuddy/data/repository/UserRepository;)V", "getStartDestination", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class NavigationViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.repository.UserRepository userRepository = null;
    
    @javax.inject.Inject()
    public NavigationViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.repository.UserRepository userRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStartDestination() {
        return null;
    }
}