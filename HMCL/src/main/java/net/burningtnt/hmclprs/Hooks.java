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
        return "https://burningtnt.github.io/HMCL-Snapshot-Update/generated/0ffbe60ac74fb7a5514e0bf7c4680e9210aeaa97/930a1e56062c761a9d46884436e0c3b8f1bc0d4f.jar.json";
    }

    public static String onInitDisableSelfIntegrityCheckProperty(String value) {
        return value == null ? "true" : value;
    }

    public static boolean onGetIsDevelopmentVersionCondition(String value) {
        return value.contains("@");
    }
}
