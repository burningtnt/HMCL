package net.burningtnt.hmat.game;

import net.burningtnt.hmat.AnalyzeResult;
import net.burningtnt.hmat.Analyzer;
import net.burningtnt.hmat.LogAnalyzable;
import net.burningtnt.hmat.solver.Solver;
import net.burningtnt.hmat.solver.SolverConfigurator;
import org.jackhuang.hmcl.game.GameJavaVersion;
import org.jackhuang.hmcl.java.JavaManager;
import org.jackhuang.hmcl.java.JavaRuntime;
import org.jackhuang.hmcl.setting.DownloadProviders;
import org.jackhuang.hmcl.setting.JavaVersionType;
import org.jackhuang.hmcl.setting.VersionSetting;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.util.platform.Platform;
import org.jackhuang.hmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JRE32BitAnalyzer implements Analyzer<LogAnalyzable> {
    private static final String P1_HEAD = "Could not reserve enough space for ";
    private static final String P1_TAIL = "KB object heap";

    private static final String P2_L1_HEAD = "Invalid initial heap size: -Xm";

    @Override
    public ControlFlow analyze(LogAnalyzable input, List<AnalyzeResult<LogAnalyzable>> analyzeResults) throws Exception {
        List<String> logs = input.getLogs();
        if (logs.size() >= 10) {
            return ControlFlow.CONTINUE;
        }

        for (int l = logs.size(), i = 0; i < l; i++) {
            String current = logs.get(i);
            if (current.startsWith(P1_HEAD)) {
                if (current.endsWith(P1_TAIL)) {
                    return apply(input, analyzeResults);
                }
            } else if (current.startsWith(P2_L1_HEAD)) {
                return apply(input, analyzeResults);
            }
        }

        return ControlFlow.CONTINUE;
    }

    @NotNull
    private Analyzer.ControlFlow apply(LogAnalyzable input, List<AnalyzeResult<LogAnalyzable>> results) {
        results.add(new AnalyzeResult<>(this, AnalyzeResult.ResultID.LOG_GAME_JRE_32BIT, new Solver() {
            @Override
            public void configure(SolverConfigurator configurator) {
                configurator.setTask(JavaManager.uninstallJava(input.getLaunchOptions().getJava()).thenComposeAsync(() -> {
                    GameVersionNumber gameVersion = GameVersionNumber.asGameVersion(input.getRepository().getGameVersion(input.getVersion()));
                    JavaRuntime runtime = JavaManager.findSuitableJava(gameVersion, input.getVersion());
                    if (runtime != null) {
                        return Task.supplyAsync(() -> runtime);
                    }
                    GameJavaVersion gameJavaVersion = GameJavaVersion.getMinimumJavaVersion(gameVersion);
                    if (gameJavaVersion == null) {
                        gameJavaVersion = GameJavaVersion.JAVA_8;
                    }

                    return JavaManager.installJava(DownloadProviders.getDownloadProvider(), Platform.CURRENT_PLATFORM, gameJavaVersion);
                }).thenAcceptAsync(Schedulers.javafx(), jre -> {
                    VersionSetting vs = input.getRepository().getVersionSetting(input.getVersion().getId());
                    vs.setJavaVersionType(JavaVersionType.DETECTED);
                    vs.setDefaultJavaPath(jre.getBinary().toString());
                }));
            }

            @Override
            public void callbackSelection(SolverConfigurator configurator, int selectionID) {
                configurator.transferTo(null);
            }
        }));
        return ControlFlow.BREAK_OTHER;
    }
}
