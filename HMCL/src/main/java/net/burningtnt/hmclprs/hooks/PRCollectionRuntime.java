package net.burningtnt.hmclprs.hooks;

import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import net.burningtnt.hmclprs.Constants;
import net.burningtnt.hmclprs.patch.Inject;
import net.burningtnt.hmclprs.patch.Redirect;
import net.burningtnt.hmclprs.patch.ValueMutation;
import org.jackhuang.hmcl.game.DefaultGameRepository;
import org.jackhuang.hmcl.setting.ConfigHolder;
import org.jackhuang.hmcl.task.FileDownloadTask;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.animation.ContainerAnimations;
import org.jackhuang.hmcl.ui.animation.TransitionPane;
import org.jackhuang.hmcl.upgrade.RemoteVersion;
import org.jackhuang.hmcl.util.Lang;
import org.jackhuang.hmcl.util.Pair;
import org.jackhuang.hmcl.util.io.Zipper;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PRCollectionRuntime {
    @Inject
    public static void onApplicationLaunch() {
        ObservableMap<String, Object> shownTips = ConfigHolder.config().getShownTips();
        if (Constants.SHOULD_DISPLAY_LAUNCH_WARNING && shownTips.get("prs-warning") == null) {
            Controllers.dialog(Constants.getWarningBody(), Constants.getWarningTitle());
            shownTips.put("prs-warning", true);
        }
    }

    private PRCollectionRuntime() {
    }

    @Redirect
    public static String onGetApplicationRawVersion() {
        return Constants.DEFAULT_VERSION.getValue();
    }

    @ValueMutation
    public static String onInitDisableSelfIntegrityCheckProperty(String value) {
        return value == null ? "true" : value;
    }

    @Redirect
    public static TransitionPane onBuildAnnouncementPane(ObservableList<Node> nodes) {
        if (!Constants.SHOULD_DISPLAY_LAUNCH_WARNING) {
            return null;
        }

        VBox card = new VBox();

        BorderPane title = new BorderPane();
        title.getStyleClass().add("title");
        title.setLeft(new Label(Constants.getWarningTitle()));

        TextFlow body = FXUtils.segmentToTextFlow(Constants.getWarningBody(), Controllers::onHyperlinkAction);
        body.setLineSpacing(4);

        card.getChildren().setAll(title, body);
        card.setSpacing(16);
        card.getStyleClass().addAll("card", "announcement");

        VBox box = new VBox(16);
        box.getChildren().add(card);

        TransitionPane pane = new TransitionPane();
        pane.setContent(box, ContainerAnimations.NONE);

        nodes.add(pane);
        return pane;
    }

    @Redirect
    public static List<String> prepareFallbackURLs(RemoteVersion rv) {
        if (rv.getChannel() == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(Constants.UPDATE_FALLBACKS).parallel().map(s -> {
            try {
                RemoteVersion r = RemoteVersion.fetch(null, s);
                FileDownloadTask.IntegrityCheck r1 = r.getIntegrityCheck(), r2 = rv.getIntegrityCheck();
                if (!Objects.equals(r1.getAlgorithm(), r2.getAlgorithm()) || !Objects.equals(r1.getChecksum(), r2.getChecksum())) {
                    return null;
                }

                return r.getUrl();
            } catch (IOException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Redirect
    public static List<URI> onGetRemoteVersionUpdateLinks(RemoteVersion rv) {
        return Stream.concat(
                Stream.of(rv.getUrl()),
                rv.prc$fallbackURL.stream()
        ).map(URI::create).collect(Collectors.toList());
    }

    @Inject
    public static void onCreateUpgradeDialog() {
        throw new UnsupportedOperationException("UpdateDialog has been deprecated");
    }

    @Inject
    public static void onCreateGameCrashReport(DefaultGameRepository repository, String versionID, Zipper zipper) throws IOException {
        zipper.putTextFile(Lang.mapOf(
                Pair.pair(
                        "launcher",
                        repository.getVersion(versionID).getPatches().stream().anyMatch(v -> "game".equals(v.getId())) ? "HMCL" : "OTHERS"
                )
        ).entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n")), ".pr-collection.log");
    }
}
