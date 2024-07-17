package net.burningtnt.hmclprs;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import net.burningtnt.hmclprs.hooks.EntryPoint;
import net.burningtnt.hmclprs.hooks.Final;
import net.burningtnt.hmclprs.hooks.HookContainer;
import org.jackhuang.hmcl.ui.construct.AnnouncementCard;

import javax.swing.*;

@HookContainer
public final class PRCollection {
    private PRCollection() {
    }

    @Final
    private static volatile String defaultVersion;

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.INJECT)
    public static void onApplicationLaunch() {
        if (PRCollectionConstants.SHOULD_DISPLAY_LAUNCH_WARNING && JOptionPane.showConfirmDialog(
                null, PRCollectionConstants.getWarningBody(), PRCollectionConstants.getWarningTitle(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            System.exit(1);
        }
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationName(String name) {
        return name + PRCollectionConstants.PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationFullName(String fullName) {
        return fullName + PRCollectionConstants.PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationVersion(String defaultVersion) {
        PRCollection.defaultVersion = defaultVersion;
        return defaultVersion + PRCollectionConstants.PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationPublishURL(String defaultVersion) {
        return PRCollectionConstants.HOME_PAGE;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onGetApplicationRawVersion(String version) {
        return defaultVersion;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationDefaultUpdateLink(String url) {
        return PRCollectionConstants.UPDATE_LINK;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitDisableSelfIntegrityCheckProperty(String value) {
        return value == null ? "true" : value;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.REDIRECT)
    public static VBox onBuildAnnouncementPane(ObservableList<Node> nodes) {
        VBox pane = new VBox(16);
        if (PRCollectionConstants.SHOULD_DISPLAY_LAUNCH_WARNING) {
            pane.getChildren().add(new AnnouncementCard(PRCollectionConstants.getWarningTitle(), PRCollectionConstants.getWarningBody()));
            nodes.add(pane);
        }
        return pane;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.INJECT)
    public static void onUpdateFrom(Runnable updateRunnable) {
        Platform.runLater(updateRunnable);
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.INJECT)
    public static void importRef(Class<?> clazz) {
    }
}
