package com.odorik.odorikbuddy.ui.main;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u000f\u0010\u0011\u0012B\u001f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u0082\u0001\u0004\u0013\u0014\u0015\u0016\u00a8\u0006\u0017"}, d2 = {"Lcom/odorik/odorikbuddy/ui/main/BottomNavItem;", "", "route", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "titleRes", "", "(Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;I)V", "getIcon", "()Landroidx/compose/ui/graphics/vector/ImageVector;", "getRoute", "()Ljava/lang/String;", "getTitleRes", "()I", "Calls", "Dashboard", "Settings", "Sms", "Lcom/odorik/odorikbuddy/ui/main/BottomNavItem$Calls;", "Lcom/odorik/odorikbuddy/ui/main/BottomNavItem$Dashboard;", "Lcom/odorik/odorikbuddy/ui/main/BottomNavItem$Settings;", "Lcom/odorik/odorikbuddy/ui/main/BottomNavItem$Sms;", "app_debug"})
public abstract class BottomNavItem {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.ui.graphics.vector.ImageVector icon = null;
    private final int titleRes = 0;
    
    private BottomNavItem(java.lang.String route, androidx.compose.ui.graphics.vector.ImageVector icon, int titleRes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.ui.graphics.vector.ImageVector getIcon() {
        return null;
    }
    
    public final int getTitleRes() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/odorik/odorikbuddy/ui/main/BottomNavItem$Calls;", "Lcom/odorik/odorikbuddy/ui/main/BottomNavItem;", "()V", "app_debug"})
    public static final class Calls extends com.odorik.odorikbuddy.ui.main.BottomNavItem {
        @org.jetbrains.annotations.NotNull()
        public static final com.odorik.odorikbuddy.ui.main.BottomNavItem.Calls INSTANCE = null;
        
        private Calls() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/odorik/odorikbuddy/ui/main/BottomNavItem$Dashboard;", "Lcom/odorik/odorikbuddy/ui/main/BottomNavItem;", "()V", "app_debug"})
    public static final class Dashboard extends com.odorik.odorikbuddy.ui.main.BottomNavItem {
        @org.jetbrains.annotations.NotNull()
        public static final com.odorik.odorikbuddy.ui.main.BottomNavItem.Dashboard INSTANCE = null;
        
        private Dashboard() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/odorik/odorikbuddy/ui/main/BottomNavItem$Settings;", "Lcom/odorik/odorikbuddy/ui/main/BottomNavItem;", "()V", "app_debug"})
    public static final class Settings extends com.odorik.odorikbuddy.ui.main.BottomNavItem {
        @org.jetbrains.annotations.NotNull()
        public static final com.odorik.odorikbuddy.ui.main.BottomNavItem.Settings INSTANCE = null;
        
        private Settings() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/odorik/odorikbuddy/ui/main/BottomNavItem$Sms;", "Lcom/odorik/odorikbuddy/ui/main/BottomNavItem;", "()V", "app_debug"})
    public static final class Sms extends com.odorik.odorikbuddy.ui.main.BottomNavItem {
        @org.jetbrains.annotations.NotNull()
        public static final com.odorik.odorikbuddy.ui.main.BottomNavItem.Sms INSTANCE = null;
        
        private Sms() {
        }
    }
}