package net.burningtnt.hmclprs;

import javafx.application.Platform;
import net.burningtnt.hmclprs.hooks.EntryPoint;
import net.burningtnt.hmclprs.hooks.Final;
import net.burningtnt.hmclprs.hooks.HookContainer;

import javax.swing.*;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

@HookContainer
public final class PRCollection {
    private PRCollection() {
    }

    private static final String PR_COLLECTION_SUFFIX = " (PR Collection)";

    private static final String UPDATE_LINK = "https://hmcl-snapshot-update-73w.pages.dev/redirect/v1/type/pr-collection";

    @Final
    private static volatile String defaultVersion;

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.INJECT)
    public static void onApplicationLaunch() {
        if (DeveloperFlags.SHOULD_DISPLAY_LAUNCH_WARNING && JOptionPane.showConfirmDialog(
                null, i18n("prs.warning", "https://github.com/burningtnt/HMCL/pull/9"), i18n("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            System.exit(1);
        }
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationName(String name) {
        return name + PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationFullName(String fullName) {
        return fullName + PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationVersion(String defaultVersion) {
        PRCollection.defaultVersion = defaultVersion;
        return defaultVersion + PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onGetApplicationRawVersion(String version) {
        return defaultVersion;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationDefaultUpdateLink(String url) {
        return UPDATE_LINK;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitDisableSelfIntegrityCheckProperty(String value) {
        return value == null ? "true" : value;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.INJECT)
    public static boolean onShouldDisplayAnnouncementPane() {
        return DeveloperFlags.SHOULD_DISPLAY_LAUNCH_WARNING;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.INJECT)
    public static void onUpdateFrom(Runnable updateRunnable) {
        Platform.runLater(updateRunnable);
    }
}
