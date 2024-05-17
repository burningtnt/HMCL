package net.burningtnt.hmat.game;

import net.burningtnt.hmat.AnalyzeResult;
import net.burningtnt.hmat.Analyzer;
import net.burningtnt.hmat.LogAnalyzable;
import org.jackhuang.hmcl.util.StringUtils;
import org.jackhuang.hmcl.util.platform.OperatingSystem;
import org.jackhuang.hmcl.util.platform.win.RegistryUtils;

import java.util.List;

public class CodePageAnalyzer implements Analyzer<LogAnalyzable> {
    private static final String[] KEYS = {
            "java.lang.ClassNotFoundException",
            "\u0020\u627e\u4e0d\u5230\u6216\u65e0\u6cd5\u52a0\u8f7d\u4e3b\u7c7b\u0020"
    };

    @Override
    public ControlFlow analyze(LogAnalyzable input, List<AnalyzeResult<LogAnalyzable>> results) throws Exception {
        if (OperatingSystem.CURRENT_OS != OperatingSystem.WINDOWS || !StringUtils.containsChinese(input.getRepository().getBaseDirectory().toString())) {
            return ControlFlow.CONTINUE;
        }

        List<String> logs = input.getLogs();
        if (logs.size() >= 10) {
            return ControlFlow.CONTINUE;
        }

        for (String key : KEYS) {
            for (int i = 0;i < logs.size();i++) {
                if (logs.get(i).contains(key)) {
                    RegistryUtils.QueryResult result = RegistryUtils.query(
                            RegistryUtils.Type.CURRENT_USER,
                            "SYSTEM\\CurrentControlSet\\Control\\Nls\\CodePage", "ACP"
                    ).run();
                    if (result == null) {
                        result = RegistryUtils.query(
                                RegistryUtils.Type.LOCAL_MACHINE,
                                "SYSTEM\\CurrentControlSet\\Control\\Nls\\CodePage", "ACP"
                        ).run();
                    }

                    if (result == null || ("REG_SZ".equals(result.getType()) && "936".equals(result.getValue()))) {
                        return ControlFlow.CONTINUE;
                    }

                    results.add(AnalyzeResult.manual(this, AnalyzeResult.ResultID.LOG_GAME_CODE_PAGE));
                    return ControlFlow.BREAK_OTHER;
                }
            }
        }

        return ControlFlow.CONTINUE;
    }

    private static boolean equals(List<String> a, String[] b) {
        int l = b.length;
        if (a.size() != l) {
            return false;
        }

        for (int i = 0; i < l; i++) {
            if (!a.get(i).equals(b[i])) {
                return false;
            }
        }

        return true;
    }
}
