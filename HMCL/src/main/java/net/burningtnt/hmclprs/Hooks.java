package net.burningtnt.hmclprs;

import javax.swing.*;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

@EntryPoint.Container
public final class Hooks {
    private Hooks() {
    }

    private static final String PR_COLLECTION_SUFFIX = " (PR Collection)";

    private static final String UPDATE_LINK = "https://hmcl-snapshot-update-73w.pages.dev/redirect/v1/type/pr-collection";

    @EntryPoint.Final
    private static volatile String defaultVersion;

    @EntryPoint(EntryPoint.LifeCycle.BOOTSTRAP)
    public static void onApplicationLaunch() {
        if ("ignore".equals(System.getenv("HMCL_PR_WARNING")) || "ignore".equals(System.getProperty("hmcl.pr.warning"))) {
            return;
        }

        if (JOptionPane.showConfirmDialog(
                null, i18n("prs.warning"), i18n("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            System.exit(1);
        }
    }

    @EntryPoint(EntryPoint.LifeCycle.BOOTSTRAP)
    public static String onInitApplicationName(String name) {
        return name + PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(EntryPoint.LifeCycle.BOOTSTRAP)
    public static String onInitApplicationFullName(String fullName) {
        return fullName + PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(EntryPoint.LifeCycle.BOOTSTRAP)
    public static String onInitApplicationVersion(String defaultVersion) {
        Hooks.defaultVersion = defaultVersion;
        return defaultVersion + Hooks.PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(EntryPoint.LifeCycle.RUNTIME)
    public static String onGetApplicationRawVersion(String version) {
        return defaultVersion;
    }

    @EntryPoint(EntryPoint.LifeCycle.BOOTSTRAP)
    public static String onInitApplicationDefaultUpdateLink(String url) {
        return UPDATE_LINK;
    }

    @EntryPoint(EntryPoint.LifeCycle.RUNTIME)
    public static String onInitDisableSelfIntegrityCheckProperty(String value) {
        return value == null ? "true" : value;
    }
}
