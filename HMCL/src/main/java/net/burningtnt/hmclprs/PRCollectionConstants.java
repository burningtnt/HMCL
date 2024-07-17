package net.burningtnt.hmclprs;

import net.burningtnt.hmclprs.hooks.EntryPoint;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

final class PRCollectionConstants {
    private PRCollectionConstants() {
    }

    static final String PR_COLLECTION_SUFFIX = " (PR Collection)";

    static final String HOME_PAGE = "https://github.com/burningtnt/HMCL/pull/9";

    static final String UPDATE_LINK = "https://hmcl-snapshot-update-73w.pages.dev/redirect/v1/type/pr-collection";

    static final boolean SHOULD_DISPLAY_LAUNCH_WARNING = shouldDisplayWarningMessage("hmcl.pr.warning", "HMCL_PR_WARNING");

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME)
    static String getWarningTitle() {
        return i18n("prs.title");
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME)
    static String getWarningBody() {
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
