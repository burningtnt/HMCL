package net.burningtnt.hmclprs;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

public final class Constants {
    private Constants() {
    }

    public static final FinalValue<String> DEFAULT_FULL_NAME = new FinalValue<>();

    public static final FinalValue<String> DEFAULT_VERSION = new FinalValue<>();

    public static final String PR_COLLECTION_SUFFIX = " (PR Collection)";

    public static final String HOME_PAGE = "https://github.com/burningtnt/HMCL/pull/9";

    public static final String UPDATE_LINK = "https://hmcl-snapshot-update-73w.pages.dev/redirect/v1/type/pr-collection";

    public static final String[] UPDATE_FALLBACKS = {
            "https://cdn.crashmc.com/https://raw.githubusercontent.com/burningtnt/HMCL-Snapshot-Update/v5/artifacts/v5/uploaders/local-storage.proxy.crashmc/burningtnt/HMCL/prs/gradle.yml.jar.json"
    };

    public static final boolean SHOULD_DISPLAY_LAUNCH_WARNING = shouldDisplayWarningMessage("hmcl.pr.warning", "HMCL_PR_WARNING");

    public static String getWarningTitle() {
        return i18n("prs.title");
    }

    public static String getWarningBody() {
        return i18n("prs.warning", HOME_PAGE);
    }

    private static boolean shouldDisplayWarningMessage(String propertyKey, String envKey) {
        String p1 = System.getProperty(propertyKey);
        if (p1 != null) {
            switch (p1) {
                case "ignore": {
                    return false;
                }
                case "display": {
                    return true;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Property %s should only be 'ignore', 'display', or null.", propertyKey));
                }
            }
        }

        String p2 = System.getenv(envKey);
        if (p2 == null) {
            return true;
        }
        switch (p2) {
            case "ignore": {
                return false;
            }
            case "display": {
                return true;
            }
            default: {
                throw new IllegalArgumentException(String.format("Environmental argument %s should only be 'ignore', 'display', or null.", envKey));
            }
        }
    }
}
