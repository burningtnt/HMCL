package net.burningtnt.hmclprs;

import javax.swing.*;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

public final class Hooks {
    private Hooks() {
    }

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

    public static String onInitApplicationName(String name) {
        return name + " (PR Collection)";
    }

    public static String onInitApplicationFullName(String fullName) {
        return fullName + " (PR Collection)";
    }

    public static String onInitApplicationVersion(String version) {
        return version + " (PR Collection)";
    }

    public static String onGetApplicationRawVersion(String version) {
        return version.endsWith(" (PR Collection)") ? version.substring(0, version.length() - " (PR Collection)".length()) : version;
    }

    public static String onInitApplicationDefaultUpdateLink(String url) {
        return "https://hmcl-snapshot-update-73w.pages.dev/redirect/v1/type/pr-collection";
    }

    public static String onInitDisableSelfIntegrityCheckProperty(String value) {
        return value == null ? "true" : value;
    }
}
