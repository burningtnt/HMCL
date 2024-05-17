package net.burningtnt.hmat.game;

import net.burningtnt.hmat.AnalyzeResult;
import net.burningtnt.hmat.Analyzer;
import net.burningtnt.hmat.LogAnalyzable;
import org.jackhuang.hmcl.game.GameJavaVersion;
import org.jackhuang.hmcl.java.JavaManager;
import org.jackhuang.hmcl.java.JavaRuntime;
import org.jackhuang.hmcl.setting.DownloadProviders;
import org.jackhuang.hmcl.setting.JavaVersionType;
import org.jackhuang.hmcl.setting.VersionSetting;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.util.Lang;
import org.jackhuang.hmcl.util.platform.Platform;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jackhuang.hmcl.util.Pair.pair;

public class JREVersionAnalyzer implements Analyzer<LogAnalyzable> {
    private static final Map<Pattern, Function<Matcher, String>> KEYS = Lang.mapOf(
            pair(
                    Pattern.compile("java.lang.IllegalArgumentException: The requested compatibility level JAVA_(?<version>[0-9]*) could not be set. Level is not supported by the active JRE or ASM version"),
                    matcher -> matcher.group("version")
            ), pair(
                    Pattern.compile("Caused by: java.lang.NoSuchMethodError: 'java.lang.Class sun.misc.Unsafe.defineAnonymousClass\\(java.lang.Class, byte\\[], java\\.lang\\.Object\\[]\\)'"),
                    matcher -> "11"
            ), pair(
                    Pattern.compile("java.lang.UnsupportedClassVersionError: .* has been compiled by a more recent version of the Java Runtime \\(class file version (?<target>[0-9]*)\\), this version of the Java Runtime only recognizes class file versions up to (?<current>[0-9]*)"),
                    matcher -> {
                        int classVersionMagic = Integer.parseInt(matcher.group("target"));
                        if (classVersionMagic < 52) {
                            return null;
                        }
                        return String.valueOf(classVersionMagic - 44);
                    }
            ), pair(
                    Pattern.compile("java.lang.IllegalArgumentException: Unsupported class file major version (?<target>[0-9]*)"),
                    matcher -> {
                        int classVersionMagic = Integer.parseInt(matcher.group("target"));
                        if (classVersionMagic < 52) {
                            return null;
                        }
                        return String.valueOf(classVersionMagic - 44);
                    }
            )
    );

    @Override
    public ControlFlow analyze(LogAnalyzable input, List<AnalyzeResult<LogAnalyzable>> results) throws Exception {
        for (String line : input.getLogs()) {
            for (Pattern pattern : KEYS.keySet()) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String javaVersion = KEYS.get(pattern).apply(matcher);
                    int majorLastI = javaVersion.indexOf('.');
                    int javaMajor = Integer.parseInt(majorLastI >= 0 ? javaVersion.substring(0, majorLastI) : javaVersion);
                    results.add(new AnalyzeResult<LogAnalyzable>(this, AnalyzeResult.ResultID.LOG_GAME_JRE_32BIT) {
                        @Override
                        public Task<ControlFlow> getSolver() {
                            return Task.supplyAsync(() -> {
                                for (JavaRuntime jre : JavaManager.getAllJava()) {
                                    if (jre.getParsedVersion() == javaMajor) {
                                        // TODO: Support non-major java version.
                                        VersionSetting vs = input.getRepository().getVersionSetting(input.getVersion().getId());
                                        vs.setJavaVersionType(JavaVersionType.CUSTOM);
                                        vs.setJavaDir(jre.getBinary().toString());
                                        return ControlFlow.CONTINUE;
                                    }
                                }
                                // TODO: GameJavaVersion.get(javaMajor) may be null.
                                JavaManager.installJava(DownloadProviders.getDownloadProvider(), Platform.CURRENT_PLATFORM, GameJavaVersion.get(javaMajor));
                                return ControlFlow.CONTINUE;
                            });
                        }
                    });
                    return ControlFlow.BREAK_OTHER;
                }
            }
        }
        return ControlFlow.CONTINUE;
    }
}
