package net.burningtnt.hmat.solver;

import javafx.scene.image.Image;
import org.jackhuang.hmcl.task.Task;

public interface SolverConfigurator {
    void setImage(Image image);

    void setDescription(String description);

    void setTask(Task<?> task);

    /**
     * @return Unique Selection ID
     */
    int putButton(String text);

    /**
     * Transfer to another Solver.
     * @param solver Another solver. null if no further solver is provided.
     */
    void transferTo(Solver solver);
}
