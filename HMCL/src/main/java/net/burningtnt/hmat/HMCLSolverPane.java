package net.burningtnt.hmat;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.burningtnt.hmat.solver.Solver;
import net.burningtnt.hmat.solver.SolverConfigurator;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.FXUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

public final class HMCLSolverPane<T> extends StackPane {
    private final Iterator<AnalyzeResult<T>> results;

    private final VBox solverContainer = new VBox(8);

    private final JFXButton next = FXUtils.newRaisedButton(i18n("wizard.next"));

    private final Label info = new Label();

    private final HMCLSolverController controller = new HMCLSolverController();

    private final IntegerProperty state = new SimpleIntegerProperty(0);

    private AnalyzeResult<T> currentResult;

    private Solver currentSolver;

    public IntegerProperty stateProperty() {
        return state;
    }

    public HMCLSolverPane(Iterator<AnalyzeResult<T>> results) {
        this.results = results;

        VBox.setVgrow(solverContainer, Priority.ALWAYS);
        solverContainer.setPadding(new Insets(0, 0, 0, 8));
        if (!results.hasNext()) {
            solverContainer.getChildren().setAll(new Label(i18n("message.error")));
            state.set(1);
        } else {
            controller.transferTo(null);
        }

        VBox container = new VBox(8);

        HBox titleBar = new HBox(8);
        titleBar.getStyleClass().addAll("jfx-tool-bar-second", "depth-1", "padding-8");
        titleBar.setAlignment(Pos.BASELINE_LEFT);
        titleBar.getChildren().setAll(info, next);

        container.getChildren().setAll(titleBar, solverContainer);

        StackPane.setAlignment(container, Pos.BOTTOM_LEFT);
        getChildren().setAll(container);
    }

    private void update() {
        if (controller.state == null) {
            solverContainer.setAlignment(Pos.CENTER);
            JFXProgressBar progressBar = new JFXProgressBar();
            progressBar.setProgress(1D);
            solverContainer.getChildren().setAll(progressBar, new Label(i18n("analyzer.solved")));

            next.setText(i18n("analyzer.launch_again"));
            next.setDisable(false);
            next.setOnAction(e -> state.set(2));
            return;
        }

        info.setText(i18n("analyzer.progress", i18n("analyzer.result." + currentResult.getResultID().name().toLowerCase(Locale.ROOT) + ".title")));

        switch (controller.state) {
            case AUTO: {
                solverContainer.setAlignment(Pos.CENTER);
                JFXProgressBar progressBar = new JFXProgressBar();
                Task<?> task = controller.task;
                if (task == null) {
                    throw new IllegalStateException("Illegal state AUTO.");
                }
                progressBar.progressProperty().bind(task.progressProperty());
                next.setDisable(true);
                task.whenComplete(Schedulers.javafx(), exception -> currentSolver.callbackSelection(controller, 0)).start();

                Label txt = new Label(i18n("analyzer.processing"));
                solverContainer.getChildren().setAll(progressBar, txt);
                break;
            }
            case MANUAL: {
                solverContainer.setAlignment(Pos.BASELINE_LEFT);
                solverContainer.getChildren().clear();
                next.setDisable(false);
                next.setOnAction(e -> currentSolver.callbackSelection(controller, 0));
                if (controller.description != null) {
                    solverContainer.getChildren().add(new Label(controller.description));
                }
                if (!controller.buttons.isEmpty()) {
                    HBox buttons = new HBox(8);
                    for (String btnText : controller.buttons) {
                        Button button = FXUtils.newBorderButton(btnText);
                        button.setOnAction(e -> currentSolver.callbackSelection(controller, buttons.getChildren().size() + 1));
                        buttons.getChildren().add(button);
                    }
                    solverContainer.getChildren().add(buttons);
                }
                if (solverContainer.getChildren().isEmpty()) {
                    throw new IllegalStateException("Illegal state MANUAL.");
                }
                if (controller.image != null) {
                    HBox pane = new HBox();

                    ImageView view = new ImageView(controller.image);
                    view.setPreserveRatio(true);
                    view.fitWidthProperty().bind(pane.widthProperty());
                    view.fitHeightProperty().bind(pane.heightProperty());
                    pane.getChildren().setAll(view);

                    pane.setAlignment(Pos.CENTER_LEFT);
                    pane.setMinWidth(0);
                    pane.prefWidthProperty().bind(solverContainer.widthProperty());
                    pane.maxWidthProperty().bind(solverContainer.widthProperty());
                    pane.setMinHeight(0);
                    pane.setPrefHeight(0);
                    VBox.setVgrow(pane, Priority.ALWAYS);

                    solverContainer.getChildren().add(pane);
                }
            }
        }
    }

    private enum State {
        AUTO, MANUAL
    }

    private final class HMCLSolverController implements SolverConfigurator {
        private State state = null;

        private String description;

        private Image image;

        private Task<?> task;

        private final List<String> buttons = new ArrayList<>();


        @Override
        public void setImage(Image image) {
            if (state != null && state != State.MANUAL) {
                throw new IllegalStateException("State " + state + " doesn't allowed setImage.");
            }
            state = State.MANUAL;
            this.image = image;
        }

        @Override
        public void setDescription(String description) {
            if (state != null && state != State.MANUAL) {
                throw new IllegalStateException("State " + state + " doesn't allowed setImage.");
            }
            state = State.MANUAL;
            this.description = description;
        }

        @Override
        public void setTask(Task<?> task) {
            if (state != null && state != State.AUTO) {
                throw new IllegalStateException("State " + state + " doesn't allowed setImage.");
            }
            state = State.AUTO;
            this.task = task;
        }

        @Override
        public int putButton(String text) {
            if (state != null && state != State.MANUAL) {
                throw new IllegalStateException("State " + state + " doesn't allowed setImage.");
            }
            state = State.MANUAL;
            this.buttons.add(text);
            return this.buttons.size();
        }

        @Override
        public void transferTo(Solver solver) {
            state = null;
            description = null;
            image = null;
            task = null;
            buttons.clear();

            if (solver != null) {
                (currentSolver = solver).configure(this);
            } else if (results.hasNext()) {
                (currentSolver = (currentResult = results.next()).getSolver()).configure(this);
            } else {
                HMCLSolverPane.this.state.set(1);
            }

            update();
        }
    }
}
