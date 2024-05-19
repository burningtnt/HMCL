package net.burningtnt.hmat.game;

import net.burningtnt.hmat.AnalyzeResult;
import net.burningtnt.hmat.Analyzer;
import net.burningtnt.hmat.LogAnalyzable;
import net.burningtnt.hmat.solver.Solver;
import net.burningtnt.hmat.solver.SolverConfigurator;
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
                    return apply(analyzeResults);
                }
            } else if (current.startsWith(P2_L1_HEAD)) {
                return apply(analyzeResults);
            }
        }

        return ControlFlow.CONTINUE;
    }

    @NotNull
    private Analyzer.ControlFlow apply(List<AnalyzeResult<LogAnalyzable>> analyzeResults) {
        analyzeResults.add(new AnalyzeResult<>(this, AnalyzeResult.ResultID.LOG_GAME_JRE_32BIT, new Solver() {
            @Override
            public void configure(SolverConfigurator configurator) {
                // TODO
            }

            @Override
            public void callbackSelection(SolverConfigurator configurator, int selectionID) {
                // TODO
            }
        }));
        return ControlFlow.BREAK_OTHER;
    }
}
