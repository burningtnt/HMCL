package net.burningtnt.hmclprs;

final class DeveloperFlags {
    private DeveloperFlags() {
    }

    static boolean SHOULD_DISPLAY_LAUNCH_WARNING = shouldDisplayWarningMessage("hmcl.pr.warning", "HMCL_PR_WARNING");

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
                    throw new IllegalArgumentException(String.format("Property %s should only be 'ignore', 'show', or null.", propertyKey));
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
                throw new IllegalArgumentException(String.format("Environmental argument %s should only be 'ignore', 'show', or null.", envKey));
            }
        }
    }
}
