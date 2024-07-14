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

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.stage.FileChooser;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.jackhuang.hmcl.java.JavaManager;
import org.jackhuang.hmcl.java.JavaRuntime;
import org.jackhuang.hmcl.setting.ConfigHolder;
import org.jackhuang.hmcl.setting.DownloadProviders;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.*;
import org.jackhuang.hmcl.ui.construct.MessageDialogPane;
import org.jackhuang.hmcl.util.platform.Architecture;
import org.jackhuang.hmcl.util.platform.OperatingSystem;
import org.jackhuang.hmcl.util.platform.Platform;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/**
 * @author Glavo
 */
public final class JavaManagementPage extends ListPageBase<JavaItem> {

    @SuppressWarnings("FieldCanBeLocal")
    private final ChangeListener<Collection<JavaRuntime>> listener;

    private final Runnable onInstallJava;

    public JavaManagementPage() {
        this.listener = FXUtils.onWeakChangeAndOperate(JavaManager.getAllJavaProperty(), this::loadJava);

        if (Platform.SYSTEM_PLATFORM.equals(OperatingSystem.LINUX, Architecture.LOONGARCH64_OW)) {
            //noinspection HttpUrlsUsage
            onInstallJava = () -> FXUtils.openLink("http://www.loongnix.cn/zh/api/java/");
        } else {
            onInstallJava = JavaDownloadDialog.showDialogAction(DownloadProviders.getDownloadProvider());
        }

        FXUtils.applyDragListener(this, it -> {
            String name = it.getName();
            return name.endsWith(".zip") || name.endsWith(".tar.gz");
        }, files -> {
            File file = files.get(0);
            String fileName = file.getName();

            if (fileName.endsWith(".zip")) {
                onInstallZip(file);
            } else if (fileName.endsWith(".tar.gz")) {
                onInstallTar(file);
            } else {
                throw new AssertionError("Unreachable code");
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new JavaPageSkin(this);
    }

    public void onAddJava() {
        FileChooser chooser = new FileChooser();
        if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS)
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java", "java.exe"));
        chooser.setTitle(i18n("settings.game.java_directory.choose"));
        File file = chooser.showOpenDialog(Controllers.getStage());
        if (file != null) {
            try {
                Path path = file.toPath().toRealPath();
                Task.supplyAsync("Get Java", () -> JavaManager.getJava(path))
                        .whenComplete(Schedulers.javafx(), ((result, exception) -> {
                            if (result != null && JavaManager.isCompatible(result.getPlatform())) {
                                String pathString = path.toString();

                                ConfigHolder.globalConfig().getDisabledJava().remove(pathString);
                                if (ConfigHolder.globalConfig().getUserJava().add(pathString)) {
                                    JavaManager.addJava(result);
                                }
                            } else {
                                Controllers.dialog(i18n("java.add.failed"), i18n("message.error"), MessageDialogPane.MessageType.ERROR);
                            }
                        })).start();
            } catch (IOException ignored) {
            }
        }
    }

    public void onInstallZip(File zipFile) {
        throw new UnsupportedOperationException("TODO");
    }

    public void onInstallTar(File compressedTarFile) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("java-", ".tar");

            try (GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(compressedTarFile.toPath()))) {
                Files.copy(gzipInputStream, tempFile);
            }

            try (TarFile tarFile = new TarFile(tempFile)) {

            }
        } catch (IOException e) {
            throw new UncheckedIOException(e); // TODO
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }

        throw new UnsupportedOperationException("TODO");
    }

    // FXThread
    private void loadJava(Collection<JavaRuntime> javaRuntimes) {
        if (javaRuntimes != null) {
            List<JavaItem> items = new ArrayList<>();
            for (JavaRuntime java : javaRuntimes) {
                items.add(new JavaItem(java));
            }
            this.setItems(FXCollections.observableList(items));
            this.setLoading(false);
        } else
            this.setLoading(true);
    }

    private static final class JavaPageSkin extends ToolbarListPageSkin<JavaManagementPage> {

        JavaPageSkin(JavaManagementPage skinnable) {
            super(skinnable);
        }

        @Override
        protected List<Node> initializeToolbar(JavaManagementPage skinnable) {
            ArrayList<Node> res = new ArrayList<>(4);

            res.add(createToolbarButton2(i18n("button.refresh"), SVG.REFRESH, JavaManager::refresh));
            if (skinnable.onInstallJava != null) {
                res.add(createToolbarButton2(i18n("java.download"), SVG.DOWNLOAD_OUTLINE, skinnable.onInstallJava));
            }
            res.add(createToolbarButton2(i18n("java.add"), SVG.PLUS, skinnable::onAddJava));
            return res;
        }
    }
}
