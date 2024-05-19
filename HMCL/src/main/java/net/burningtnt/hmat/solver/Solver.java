package net.burningtnt.hmat.solver;

public interface Solver {
    int BTN_NEXT = 0;

    void configure(SolverConfigurator configurator);

    /**
     * @param selectionID BTN_NEXT if user click 'Next'. Others if user click selection buttons.
     */
    void callbackSelection(SolverConfigurator configurator, int selectionID);
}
