package net.burningtnt.hmat.game;

import net.burningtnt.hmat.AnalyzeResult;
import net.burningtnt.hmat.Analyzer;
import net.burningtnt.hmat.LogAnalyzable;
import net.burningtnt.hmat.solver.Solver;
import net.burningtnt.hmat.solver.SolverConfigurator;
import org.jackhuang.hmcl.mod.ModLoaderType;
import org.jackhuang.hmcl.mod.curse.CurseForgeRemoteModRepository;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

public class ForgeRequiresAnalyzer implements Analyzer<LogAnalyzable> {
    private static final String HEAD = "Missing or unsupported mandatory dependencies:";
    private static final Pattern LINE = Pattern.compile(
            "\tMod ID: '(?<requiresMod>\\w+)', Requested by: '(?<mod>\\w+)', Expected range: '(?<range>([\\[(])[\\w.-]*,[\\w.-]*[])])', Actual version: '(?<version>[\\w.-]*)'");

    @Override
    public ControlFlow analyze(LogAnalyzable input, List<AnalyzeResult<LogAnalyzable>> results) throws Exception {
        if(!input.getAnalyzer().getModLoaders().contains(ModLoaderType.FORGE)) {
            return ControlFlow.CONTINUE;
        }

        List<String> logs = input.getLogs();

        for (int l = logs.size(), i = 0; i < l;) {
            String current = logs.get(i++);
            if(current.contains(HEAD)){
                for(;i < l; i++){
                    Matcher matcher = LINE.matcher(logs.get(i));
                    if(matcher.matches()){
                        String requiresMod = matcher.group(1);
                        String mod = matcher.group(2);
                        String range = matcher.group(3);
                        String version = matcher.group(4);
                        results.add(new AnalyzeResult<>(this, AnalyzeResult.ResultID.FORGE_REQUIRES_NOT_INSTALL, new Solver() {
                            private int BTN_OPEN_DOWNLOAD = -1;

                            @Override
                            public void configure(SolverConfigurator configurator) {
                                configurator.setDescription(i18n("analyzer.result.forge_requires_not_install.step.1", requiresMod, mod, range, version));

                                BTN_OPEN_DOWNLOAD = configurator.putButton(i18n("analyzer.result.forge_requires_not_install.button.mod_download"));
                            }

                            @Override
                            public void callbackSelection(SolverConfigurator configurator, int selectionID) {
                                if (selectionID == BTN_OPEN_DOWNLOAD) {
                                    //TODO: 打开mod下载界面并自动填写mod名称，加载器和游戏版本
                                }
                            }
                        }));
                    }else {
                        break;
                    }
                }
            }
        }
        return ControlFlow.CONTINUE;
    }

//    @NotNull
//    private void apply(LogAnalyzable input, List<AnalyzeResult<LogAnalyzable>> results) {
//        results.add(new AnalyzeResult<>(this, AnalyzeResult.ResultID.LOG_GAME_JRE_32BIT, Solver.ofUninstallJRE(input)));
//    }
}