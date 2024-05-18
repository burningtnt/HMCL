/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2023  huangyuhui <huanghongxun2008@126.com> and contributors
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
package org.jackhuang.hmcl.ui;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import net.burningtnt.hmat.AnalyzableType;
import net.burningtnt.hmat.AnalyzeResult;
import net.burningtnt.hmat.Analyzer;
import net.burningtnt.hmat.LogAnalyzable;
import org.jackhuang.hmcl.download.LibraryAnalyzer;
import org.jackhuang.hmcl.game.HMCLGameRepository;
import org.jackhuang.hmcl.game.LaunchOptions;
import org.jackhuang.hmcl.game.LogExporter;
import org.jackhuang.hmcl.game.Version;
import org.jackhuang.hmcl.launch.ProcessListener;
import org.jackhuang.hmcl.setting.Theme;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.util.Log4jLevel;
import org.jackhuang.hmcl.util.Pair;
import org.jackhuang.hmcl.util.StringUtils;
import org.jackhuang.hmcl.util.logging.Logger;
import org.jackhuang.hmcl.util.platform.CommandBuilder;
import org.jackhuang.hmcl.util.platform.ManagedProcess;
import org.jackhuang.hmcl.util.platform.OperatingSystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.jackhuang.hmcl.setting.ConfigHolder.config;
import static org.jackhuang.hmcl.ui.FXUtils.runInFX;
import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

public class GameCrashWindow extends Stage {
    private final Version version;
    private final LibraryAnalyzer analyzer;
    private final HMCLGameRepository repository;

    private final ManagedProcess managedProcess;
    private final ProcessListener.ExitType exitType;
    private final LaunchOptions launchOptions;

    private final List<Pair<String, Log4jLevel>> logs;

    public GameCrashWindow(ManagedProcess managedProcess, ProcessListener.ExitType exitType, HMCLGameRepository repository, Version version, LaunchOptions launchOptions, List<Pair<String, Log4jLevel>> logs) {
        this.managedProcess = managedProcess;
        this.exitType = exitType;
        this.repository = repository;
        this.version = version;
        this.launchOptions = launchOptions;
        this.logs = logs;
        this.analyzer = LibraryAnalyzer.analyze(version, repository.getGameVersion(version).orElse(null));

        setScene(new Scene(new GameCrashWindowView(), 800, 480));
        getScene().getStylesheets().addAll(Theme.getTheme().getStylesheets(config().getLauncherFontFamily()));
        setTitle(i18n("game.crash.title"));
        FXUtils.setIcon(this);
    }

    private void showLogWindow() {
        LogWindow logWindow = new LogWindow(managedProcess);

        logWindow.logLine(Logger.filterForbiddenToken("Command: " + new CommandBuilder().addAll(managedProcess.getCommands())), Log4jLevel.INFO);
        if (managedProcess.getClasspath() != null)
            logWindow.logLine("ClassPath: " + managedProcess.getClasspath(), Log4jLevel.INFO);
        for (Map.Entry<String, Log4jLevel> entry : logs)
            logWindow.logLine(entry.getKey(), entry.getValue());

        logWindow.showNormal();
    }

    private void exportGameCrashInfo() {
        Path logFile = Paths.get("minecraft-exported-crash-info-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")) + ".zip").toAbsolutePath();

        CompletableFuture.supplyAsync(() ->
                        logs.stream().map(Pair::getKey).collect(Collectors.joining(OperatingSystem.LINE_SEPARATOR)))
                .thenComposeAsync(logs ->
                        LogExporter.exportLogs(logFile, repository, launchOptions.getVersionName(), logs, new CommandBuilder().addAll(managedProcess.getCommands()).toString()))
                .handleAsync((result, exception) -> {
                    Alert alert;

                    if (exception == null) {
                        FXUtils.showFileInExplorer(logFile);
                        alert = new Alert(Alert.AlertType.INFORMATION, i18n("settings.launcher.launcher_log.export.success", logFile));
                    } else {
                        LOG.warning("Failed to export game crash info", exception);
                        alert = new Alert(Alert.AlertType.WARNING, i18n("settings.launcher.launcher_log.export.failed") + "\n" + StringUtils.getStackTrace(exception));
                    }

                    alert.setTitle(i18n("settings.launcher.launcher_log.export"));
                    alert.showAndWait();

                    return null;
                });
    }

    private final class GameCrashWindowView extends VBox {
        GameCrashWindowView() {
            setStyle("-fx-background-color: white");

            VBox titlePane = new VBox();
            {
                Label title = new Label();
                HBox.setHgrow(title, Priority.ALWAYS);

                switch (exitType) {
                    case JVM_ERROR:
                        title.setText(i18n("launch.failed.cannot_create_jvm"));
                        break;
                    case APPLICATION_ERROR:
                        title.setText(i18n("launch.failed.exited_abnormally"));
                        break;
                    case SIGKILL:
                        title.setText(i18n("launch.failed.sigkill"));
                        break;
                }

                titlePane.setAlignment(Pos.CENTER);
                titlePane.getStyleClass().addAll("jfx-tool-bar-second", "depth-1", "padding-8");
                titlePane.getChildren().setAll(title);
            }

            TextFlow notifications = FXUtils.segmentToTextFlow(i18n("game.crash.feedback"), Controllers::onHyperlinkAction);
            {
                notifications.setPadding(new Insets(8));
                notifications.setStyle("-fx-background-color: orange;");
            }

            HBox toolBar = new HBox();
            {
                JFXButton analyzeButton = FXUtils.newRaisedButton(i18n("logwindow.solve_now"));
                analyzeButton.setOnAction(e -> Task.runAsync(() -> {
                    LogAnalyzable analyzable = new LogAnalyzable(version, analyzer, repository, managedProcess, exitType, launchOptions, logs.stream().map(Pair::getKey).collect(Collectors.toList()));
                    List<AnalyzeResult<LogAnalyzable>> results = Analyzer.analyze(AnalyzableType.Log.GAME, analyzable);
                    Analyzer.execute(results);
                }).start());

                JFXButton exportInfoButton = FXUtils.newRaisedButton(i18n("logwindow.export_game_crash_logs"));
                exportInfoButton.setOnMouseClicked(e -> exportGameCrashInfo());

                JFXButton helpButton = FXUtils.newRaisedButton(i18n("help"));
                helpButton.setOnAction(e -> FXUtils.openLink("https://docs.hmcl.net/help.html"));
                runInFX(() -> FXUtils.installFastTooltip(helpButton, i18n("logwindow.help")));

                toolBar.setPadding(new Insets(8));
                toolBar.setSpacing(8);
                toolBar.getStyleClass().add("jfx-tool-bar");
                toolBar.getChildren().setAll(analyzeButton, exportInfoButton, helpButton);
            }

            getChildren().setAll(titlePane, notifications, toolBar);
        }
    }
}
