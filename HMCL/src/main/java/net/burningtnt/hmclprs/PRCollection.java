package net.burningtnt.hmclprs;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import net.burningtnt.hmclprs.hooks.EntryPoint;
import net.burningtnt.hmclprs.hooks.Final;
import net.burningtnt.hmclprs.hooks.HookContainer;
import org.jackhuang.hmcl.ui.FXUtils;

import javax.swing.*;

@HookContainer
public final class PRCollection {
    private PRCollection() {
    }

    @Final
    private static volatile String defaultFullName;

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
        defaultFullName = fullName;
        return fullName + PRCollectionConstants.PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitApplicationVersion(String version) {
        defaultVersion = version;
        return version + PRCollectionConstants.PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.REDIRECT)
    public static String onInitApplicationTitle() {
        return defaultFullName + " v" + defaultVersion + PRCollectionConstants.PR_COLLECTION_SUFFIX;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.REDIRECT)
    public static String onInitApplicationPublishURL() {
        return PRCollectionConstants.HOME_PAGE;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.BOOTSTRAP, type = EntryPoint.Type.REDIRECT)
    public static String onInitApplicationDefaultUpdateLink() {
        return PRCollectionConstants.UPDATE_LINK;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.REDIRECT)
    public static String onGetApplicationRawVersion() {
        return defaultVersion;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.VALUE_MUTATION)
    public static String onInitDisableSelfIntegrityCheckProperty(String value) {
        return value == null ? "true" : value;
    }

    @EntryPoint(when = EntryPoint.LifeCycle.RUNTIME, type = EntryPoint.Type.REDIRECT)
    public static VBox onBuildAnnouncementPane(ObservableList<Node> nodes) {
        VBox pane = new VBox(16);

        VBox content = new VBox(14);
        content.setStyle("-fx-border-color: -fx-base-darker-color; -fx-border-radius: 5px; -fx-border-width: 4px;");
        {
            ImageView view = new ImageView(FXUtils.newBuiltinImage("/assets/img/teacon-banner.png"));
            view.setPreserveRatio(true);
            view.fitWidthProperty().bind(content.widthProperty().subtract(10));
            Rectangle clip = new Rectangle();
            FXUtils.onChange(view.layoutBoundsProperty(), layout -> {
                clip.setX(layout.getMinX());
                clip.setY(layout.getMinY());
                clip.setWidth(layout.getWidth());
                clip.setHeight(layout.getHeight());
            });
            clip.setArcWidth(5);
            clip.setArcHeight(5);
            view.setClip(clip);

            content.setMinWidth(0);
            content.setAlignment(Pos.CENTER);
            content.getChildren().add(view);

            content.setOnMouseClicked(e -> FXUtils.openLink("https://www.teacon.cn/"));

            Platform.runLater(content::requestLayout); // Java 8 is Java 8.
        }
        pane.getChildren().add(content);

        nodes.add(pane);
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
