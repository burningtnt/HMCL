/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2024 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.jackhuang.hmcl.ui.main;

import com.jfoenix.controls.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.jackhuang.hmcl.download.DownloadProvider;
import org.jackhuang.hmcl.download.java.JavaDistribution;
import org.jackhuang.hmcl.download.java.JavaPackageType;
import org.jackhuang.hmcl.download.java.JavaRemoteVersion;
import org.jackhuang.hmcl.download.java.disco.DiscoFetchJavaListTask;
import org.jackhuang.hmcl.download.java.disco.DiscoJavaDistribution;
import org.jackhuang.hmcl.download.java.disco.DiscoJavaRemoteVersion;
import org.jackhuang.hmcl.download.java.mojang.MojangJavaDistribution;
import org.jackhuang.hmcl.download.java.mojang.MojangJavaRemoteVersion;
import org.jackhuang.hmcl.game.GameJavaVersion;
import org.jackhuang.hmcl.setting.DownloadProviders;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.construct.DialogCloseEvent;
import org.jackhuang.hmcl.ui.construct.SpinnerPane;
import org.jackhuang.hmcl.util.Pair;
import org.jackhuang.hmcl.util.platform.Platform;

import java.util.*;

import static org.jackhuang.hmcl.ui.FXUtils.onEscPressed;
import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/**
 * @author Glavo
 */
public final class JavaDownloadDialog extends JFXDialogLayout {

    private static boolean isLTS(int major) {
        if (major <= 8) {
            return true;
        }

        if (major < 21) {
            return major == 11 || major == 17;
        }

        return major % 4 == 1;
    }

    private final JFXComboBox<JavaDistribution<?>> distributionBox;
    private final JFXComboBox<JavaRemoteVersion> remoteVersionBox;
    private final JFXComboBox<JavaPackageType> packageTypeBox;
    private final Label warningLabel = new Label();

    private final JFXButton downloadButton;
    private final StackPane downloadButtonPane = new StackPane();

    private final DownloadProvider downloadProvider = DownloadProviders.getDownloadProvider();

    private final ObjectProperty<DiscoJavaVersionList> currentDiscoJavaVersionList = new SimpleObjectProperty<>();

    private final Map<Pair<DiscoJavaDistribution, JavaPackageType>, DiscoJavaVersionList> javaVersionLists = new HashMap<>();

    private boolean changingDistribution = false;

    public JavaDownloadDialog(List<JavaDistribution<?>> distributions) {
        assert !distributions.isEmpty();

        this.distributionBox = new JFXComboBox<>();
        this.distributionBox.setConverter(FXUtils.stringConverter(JavaDistribution::getDisplayName));

        this.remoteVersionBox = new JFXComboBox<>();
        this.remoteVersionBox.setConverter(FXUtils.stringConverter(JavaRemoteVersion::getDistributionVersion));

        this.packageTypeBox = new JFXComboBox<>();

        this.downloadButton = new JFXButton(i18n("download"));
        downloadButton.setOnAction(e -> onDownload());
        downloadButton.getStyleClass().add("dialog-accept");
        downloadButton.disableProperty().bind(Bindings.isNull(remoteVersionBox.getSelectionModel().selectedItemProperty()));
        downloadButtonPane.getChildren().setAll(downloadButton);

        JFXButton cancelButton = new JFXButton(i18n("button.cancel"));
        cancelButton.setOnAction(e -> fireEvent(new DialogCloseEvent()));
        cancelButton.getStyleClass().add("dialog-cancel");
        onEscPressed(this, cancelButton::fire);

        GridPane body = new GridPane();
        body.getColumnConstraints().setAll(new ColumnConstraints(), FXUtils.getColumnHgrowing());
        body.setVgap(8);
        body.setHgap(16);

        distributionBox.setItems(FXCollections.observableList(distributions));
        if (distributions.get(0) instanceof MojangJavaDistribution) {
            distributionBox.getSelectionModel().select(0);
        }

        ChangeListener<DiscoJavaVersionList.Status> updateStatusListener = (observable, oldValue, newValue) -> updateStatus(newValue);
        this.currentDiscoJavaVersionList.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.status.removeListener(updateStatusListener);
            }

            if (newValue != null) {
                newValue.status.addListener(updateStatusListener);
                updateStatus(newValue.status.get());
            } else {
                updateStatus(null);
            }
        });

        packageTypeBox.getSelectionModel().selectedItemProperty().addListener(ignored -> updateVersions());
        FXUtils.onChangeAndOperate(distributionBox.getSelectionModel().selectedItemProperty(), value -> {
            if (value instanceof DiscoJavaDistribution) {
                changingDistribution = true;
                packageTypeBox.setItems(FXCollections.observableList(new ArrayList<>(value.getSupportedPackageTypes())));
                packageTypeBox.getSelectionModel().select(0);
                changingDistribution = false;
                updateVersions();

                body.getChildren().clear();
                body.addRow(0, new Label(i18n("java.download.distribution")), distributionBox);
                body.addRow(1, new Label(i18n("java.download.version")), remoteVersionBox);
                body.addRow(2, new Label(i18n("java.download.packageType")), packageTypeBox);
            } else {
                packageTypeBox.setItems(null);
                updateVersions();

                body.getChildren().clear();
                body.addRow(0, new Label(i18n("java.download.distribution")), distributionBox);
                if (value == null) {
                    remoteVersionBox.setItems(null);
                } else if (value instanceof MojangJavaDistribution) {
                    body.addRow(1, new Label(i18n("java.download.version")), remoteVersionBox);

                    ArrayList<JavaRemoteVersion> remoteVersions = new ArrayList<>();
                    for (GameJavaVersion gameJavaVersion : GameJavaVersion.getSupportedVersions(Platform.SYSTEM_PLATFORM)) {
                        remoteVersions.add(0, new MojangJavaRemoteVersion(gameJavaVersion));
                    }
                    assert !remoteVersions.isEmpty();
                    remoteVersionBox.setItems(FXCollections.observableList(remoteVersions));
                    remoteVersionBox.getSelectionModel().select(0);
                } else throw new AssertionError("Unknown distribution type: " + value.getClass());
            }

        });

        setHeading(new Label(i18n("java.download")));
        setBody(body);
        setActions(warningLabel, downloadButtonPane, cancelButton);
    }

    private void updateStatus(DiscoJavaVersionList.Status status) {
        if (status == DiscoJavaVersionList.Status.LOADING) {
            downloadButtonPane.getChildren().setAll(new JFXSpinner());
            remoteVersionBox.setDisable(true);
            warningLabel.setText(null);
        } else {
            downloadButtonPane.getChildren().setAll(downloadButton);
            if (status == DiscoJavaVersionList.Status.SUCCESS || status == null) {
                remoteVersionBox.setDisable(false);
                warningLabel.setText(null);
            } else if (status == DiscoJavaVersionList.Status.FAILED) {
                remoteVersionBox.setDisable(true);
                warningLabel.setText(i18n("java.download.load_list.failed"));
            }
        }
    }

    private void onDownload() {
        throw new UnsupportedOperationException("TODO");
    }

    private void updateVersions() {
        if (changingDistribution) return;

        JavaDistribution<?> distribution = distributionBox.getSelectionModel().getSelectedItem();
        if (!(distribution instanceof DiscoJavaDistribution)) {
            this.currentDiscoJavaVersionList.set(null);
            return;
        }

        DiscoJavaDistribution discoJavaDistribution = (DiscoJavaDistribution) distribution;
        JavaPackageType packageType = packageTypeBox.getSelectionModel().getSelectedItem();

        DiscoJavaVersionList list = javaVersionLists.computeIfAbsent(Pair.pair(discoJavaDistribution, packageType), pair -> {
            DiscoJavaVersionList res = new DiscoJavaVersionList();
            new DiscoFetchJavaListTask(downloadProvider, discoJavaDistribution, Platform.SYSTEM_PLATFORM, packageType).setExecutor(Schedulers.io()).thenApplyAsync(versions -> {
                if (versions.isEmpty()) return Collections.<JavaRemoteVersion>emptyList();

                int lastLTS = -1;
                for (int v : versions.keySet()) {
                    if (isLTS(v)) {
                        lastLTS = v;
                    }
                }

                ArrayList<JavaRemoteVersion> remoteVersions = new ArrayList<>();
                for (Map.Entry<Integer, DiscoJavaRemoteVersion> entry : versions.entrySet()) {
                    int v = entry.getKey();
                    if (v >= lastLTS || isLTS(v) || v == 16) {
                        remoteVersions.add(entry.getValue());
                    }
                }
                Collections.reverse(remoteVersions);
                return remoteVersions;
            }).whenComplete(Schedulers.javafx(), ((result, exception) -> {
                if (exception == null) {
                    res.status.set(DiscoJavaVersionList.Status.SUCCESS);
                    res.versions.setAll(result);
                } else {
                    LOG.warning("Failed to load java list", exception);
                    res.status.set(DiscoJavaVersionList.Status.FAILED);
                }
            })).start();
            return res;
        });
        this.currentDiscoJavaVersionList.set(list);
        this.remoteVersionBox.setItems(list.versions);
    }

    private static final class DiscoJavaVersionList {
        enum Status {
            LOADING, SUCCESS, FAILED
        }

        final ObservableList<JavaRemoteVersion> versions = FXCollections.observableArrayList();
        final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.LOADING);
    }
}
