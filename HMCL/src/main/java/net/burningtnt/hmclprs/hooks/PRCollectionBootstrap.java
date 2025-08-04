package net.burningtnt.hmclprs.hooks;

import net.burningtnt.hmclprs.Constants;
import net.burningtnt.hmclprs.patch.Inject;
import net.burningtnt.hmclprs.patch.Redirect;
import net.burningtnt.hmclprs.patch.ValueMutation;

public final class PRCollectionBootstrap {
    private PRCollectionBootstrap() {
    }

    @ValueMutation
    public static String onInitApplicationName(String name) {
        return name + Constants.PR_COLLECTION_SUFFIX;
    }

    @ValueMutation
    public static String onInitApplicationFullName(String fullName) {
        Constants.DEFAULT_FULL_NAME.setValue(fullName);
        return fullName + Constants.PR_COLLECTION_SUFFIX;
    }

    @ValueMutation
    public static String onInitApplicationVersion(String version) {
        Constants.DEFAULT_VERSION.setValue(version);
        return version + Constants.PR_COLLECTION_SUFFIX;
    }

    @Redirect
    public static String onInitApplicationTitle() {
        return Constants.DEFAULT_FULL_NAME.getValue() + " v" + Constants.DEFAULT_VERSION.getValue() + Constants.PR_COLLECTION_SUFFIX;
    }

    @Redirect
    public static String onInitApplicationPublishURL() {
        return Constants.HOME_PAGE;
    }

    @Redirect
    public static String onInitApplicationDefaultUpdateLink() {
        return Constants.UPDATE_LINK;
    }

    @Inject
    public static void importRef(Class<?>... clazz) {
    }
}
