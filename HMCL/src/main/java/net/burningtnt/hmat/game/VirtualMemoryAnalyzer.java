package net.burningtnt.hmat.game;

import net.burningtnt.hmat.AnalyzeResult;
import net.burningtnt.hmat.Analyzer;
import net.burningtnt.hmat.LogAnalyzable;
import net.burningtnt.hmat.solver.Solver;
import net.burningtnt.hmat.solver.SolverConfigurator;

import java.util.List;

/**
 * OpenJDK 64-Bit Server VM warning: INFO: os::commit_memory(0x0000000718000000, 167772160, 0) failed; error='页面文件太小，无法完成操作。' (DOS error/errno=1455)
 * #
 * # There is insufficient memory for the Java Runtime Environment to continue.
 * # Native memory allocation (mmap) failed to map 167772160 bytes for G1 virtual space
 * # An error report file with more information is saved as:
 * # <...file path...>
 */
public class VirtualMemoryAnalyzer implements Analyzer<LogAnalyzable> {
    private static final String KEY = "error='\\u9875\\u9762\\u6587\\u4ef6\\u592a\\u5c0f\\uff0c\\u65e0\\u6cd5\\u5b8c\\u6210\\u64cd\\u4f5c\\u3002'";

    @Override
    public ControlFlow analyze(LogAnalyzable input, List<AnalyzeResult<LogAnalyzable>> results) {
        List<String> logs = input.getLogs();
        int l = logs.size();

        for (int i = Math.max(0, l - 10); i < l; i++) {
            if (logs.get(i).contains(KEY)) {
                results.add(new AnalyzeResult<>(this, AnalyzeResult.ResultID.LOG_GAME_VIRTUAL_MEMORY, new Solver() {
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

        return ControlFlow.CONTINUE;
    }
}
