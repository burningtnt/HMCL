package net.burningtnt.hmat;

import org.jackhuang.hmcl.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public interface Analyzer<T> {
    ControlFlow analyze(T input, List<AnalyzeResult<T>> results) throws Exception;

    enum ControlFlow {
        BREAK_OTHER, CONTINUE
    }

    static <T> List<AnalyzeResult<T>> analyze(AnalyzableType<T> type, T input) {
        List<AnalyzeResult<T>> results = new ArrayList<>();

        ControlFlow flow;
        for (Analyzer<T> analyzer : type.getAnalyzers()) {
            try {
                flow = analyzer.analyze(input, results);
            } catch (Exception e) {
                Logger.LOG.warning("Cannot invoke analyzer " + analyzer.getClass().getName() + " for input " + input + ".", e);
                continue;
            }

            if (flow == ControlFlow.BREAK_OTHER) {
                return results;
            }
        }

        return results;
    }
}
