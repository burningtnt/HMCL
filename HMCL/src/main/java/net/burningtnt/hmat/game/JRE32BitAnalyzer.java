package net.burningtnt.hmat.game;

import net.burningtnt.hmat.AnalyzeResult;
import net.burningtnt.hmat.Analyzer;
import net.burningtnt.hmat.LogAnalyzable;

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
                    analyzeResults.add(AnalyzeResult.manual(this, AnalyzeResult.ResultID.LOG_GAME_JRE_32BIT));
                    return ControlFlow.BREAK_OTHER;
                }
            } else if (current.startsWith(P2_L1_HEAD)) {
                analyzeResults.add(AnalyzeResult.manual(this, AnalyzeResult.ResultID.LOG_GAME_JRE_32BIT));
                return ControlFlow.BREAK_OTHER;
            }
        }

        return ControlFlow.CONTINUE;
    }
}
