package net.burningtnt.hmat;

import org.jackhuang.hmcl.task.Task;

public abstract class AnalyzeResult<T> {
    public final Analyzer<T> analyzer;

    public final ResultID resultID;

    public AnalyzeResult(Analyzer<T> analyzer, ResultID resultID) {
        this.analyzer = analyzer;
        this.resultID = resultID;
    }

    public abstract Task<Analyzer.ControlFlow> getSolver();

    public static <T> AnalyzeResult<T> manual(Analyzer<T> analyzer, ResultID resultID) {
        return new AnalyzeResult<T>(analyzer, resultID) {
            @Override
            public Task<Analyzer.ControlFlow> getSolver() {
                return null;
            }
        };
    }

    public enum ResultID {
        LOG_GAME_CODE_PAGE,
        LOG_GAME_VIRTUAL_MEMORY,
        LOG_GAME_JRE_32BIT,
        LOG_GAME_JRE_VERSION
    }
}
