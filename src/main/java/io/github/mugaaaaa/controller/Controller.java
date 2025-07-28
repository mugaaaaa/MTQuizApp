package io.github.mugaaaaa.controller;

import io.github.mugaaaaa.model.Question;
import io.github.mugaaaaa.service.QuestionService;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.net.URL;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Controller {

    // --- FXML 控件注入 ---
    @FXML private FlowPane questionsFlowPane;
    @FXML private Label stemLabel;
    @FXML private VBox optionsContainer;
    @FXML private Button lastQuestionButton;
    @FXML private Button nextQuestionButton;
    @FXML private Button answerButton;

    // --- 状态与服务 ---
    private final QuestionService questionService = new QuestionService();
    private List<Question> allQuestions;
    private Map<Integer, Button> questionButtonsMap = new HashMap<>();
    private ToggleGroup optionsGroup = new ToggleGroup();
    private int currentQuestionIndex = -1;

    @FXML
    public void initialize() {
        loadDataInBackground();
    }

    private void loadDataInBackground() {
        Task<List<Question>> loadTask = new Task<>() {
            @Override
            protected List<Question> call() { return questionService.loadAllQuestions(); }
        };
        loadTask.setOnSucceeded(event -> {
            allQuestions = loadTask.getValue();
            populateQuestionButtons();
            if (!allQuestions.isEmpty()) {
                displayQuestion(0);
            }
        });
        loadTask.setOnFailed(event -> loadTask.getException().printStackTrace());
        new Thread(loadTask).start();
    }

    // 1. 重构 populateQuestionButtons 来应用初始颜色
    private void populateQuestionButtons() {
        questionsFlowPane.getChildren().clear();
        questionButtonsMap.clear();
        for (int i = 0; i < allQuestions.size(); i++) {
            Question q = allQuestions.get(i);
            Button btn = new Button(String.valueOf(q.no()));
            btn.setPrefSize(40, 40);
            final int questionIndex = i;
            btn.setOnAction(e -> displayQuestion(questionIndex));

            updateButtonAppearance(btn, q.stat()); // 根据加载到的状态设置初始颜色

            questionsFlowPane.getChildren().add(btn);
            questionButtonsMap.put(q.no(), btn);
        }
    }

    private void displayQuestion(int index) {
        if (index < 0 || index >= allQuestions.size()) return;
        currentQuestionIndex = index;
        Question currentQuestion = allQuestions.get(index);

        stemLabel.setText(currentQuestion.stem());
        optionsContainer.getChildren().clear();
        optionsGroup.getToggles().clear();

        List<String> options = currentQuestion.getOptions();
        for (String optionText : options) {
            RadioButton rb = new RadioButton(optionText);
            rb.setToggleGroup(optionsGroup);
            VBox.setMargin(rb, new Insets(10));
            // 如果题目已经答过，则禁用所有选项
            if (currentQuestion.stat() != 0) {
                rb.setDisable(true);
                // 如果是正确答案，可以特殊高亮显示
                if (optionText.startsWith(currentQuestion.answer() + ".")) {
                    rb.setStyle("-fx-font-weight: bold;"); // 例如加粗
                }
            }
            optionsContainer.getChildren().add(rb);
        }

        lastQuestionButton.setDisable(index == 0);
        nextQuestionButton.setDisable(index == allQuestions.size() - 1);
        // 如果题目已经答过，禁用提交按钮
        answerButton.setDisable(currentQuestion.stat() != 0);
    }

    // 3. 重构 handleSubmitAnswer 来更新数据库和UI
    @FXML
    void handleSubmitAnswer(ActionEvent event) {
        RadioButton selectedRb = (RadioButton) optionsGroup.getSelectedToggle();
        if (selectedRb == null) {
            System.out.println("请先选择一个答案！");
            return;
        }

        String selectedAnswerText = selectedRb.getText();
        String userAnswer = selectedAnswerText.substring(0, 1);
        Question currentQuestion = allQuestions.get(currentQuestionIndex);

        int newStatus;
        if (userAnswer.equals(currentQuestion.answer())) {
            newStatus = 1; // 答对
        } else {
            newStatus = 2; // 答错
        }

        // 更新UI
        Button currentButton = questionButtonsMap.get(currentQuestion.no());
        updateButtonAppearance(currentButton, newStatus);

        // 更新内存中的数据状态
        allQuestions.set(currentQuestionIndex, currentQuestion.withStat(newStatus));

        // 禁用提交按钮和选项
        answerButton.setDisable(true);
        optionsContainer.getChildren().forEach(node -> node.setDisable(true));

        // 在后台线程中更新数据库
        final int questionNo = currentQuestion.no();
        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                questionService.updateQuestionStatus(questionNo, newStatus);
                return null;
            }
        };
        updateTask.setOnFailed(e -> updateTask.getException().printStackTrace());
        new Thread(updateTask).start();
    }

    // 辅助方法，根据状态更新按钮样式
    private void updateButtonAppearance(Button button, int status) {
        button.getStyleClass().removeAll("correct-button", "incorrect-button"); // 先移除旧样式
        switch (status) {
            case 1: // 答对
                button.setStyle("-fx-background-color: #90EE90;"); // 亮绿色
                // 或者使用CSS: button.getStyleClass().add("correct-button");
                break;
            case 2: // 答错
                button.setStyle("-fx-background-color: #F08080;"); // 亮珊瑚色
                // 或者使用CSS: button.getStyleClass().add("incorrect-button");
                break;
            case 0: // 未答
            default:
                button.setStyle(""); // 恢复默认样式
                break;
        }
    }

    @FXML
    void handleLastQuestion(ActionEvent event) {
        // 如果当前不是第一题，就显示前一题
        if (currentQuestionIndex > 0) {
            displayQuestion(currentQuestionIndex - 1);
        }
    }

    @FXML
    void handleNextQuestion(ActionEvent event) {
        // 如果当前不是最后一题，就显示后一题
        if (currentQuestionIndex < allQuestions.size() - 1) {
            displayQuestion(currentQuestionIndex + 1);
        }
    }

    @FXML
    void showDataAnalysis(ActionEvent event) {
        if (allQuestions == null || allQuestions.isEmpty()) {
            System.out.println("没有题目数据");
            return;
        }

        int correctCount = 0;
        int incorrectCount = 0;
        int unansweredCount = 0;

        for (Question q: allQuestions) {
            switch (q.stat()) {
                case 1:
                    correctCount++;
                    break;
                case 2:
                    incorrectCount++;
                    break;
                default:
                    unansweredCount++;
                    break;
            }
        }

        int totalAnswered = correctCount + incorrectCount;
        double accuracy = (totalAnswered == 0) ? 0.0 : ((double) correctCount / totalAnswered);

        //弹出新窗口, 获取当前窗口为父窗口.
        try {
            URL fxmlUrl = getClass().getResource("/io/github/mugaaaaa/dataAnalysis.fxml");
            if (fxmlUrl == null) {
                System.err.println("找不到FXML文件");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            DataAnalysisController dataAnalysisController = loader.getController();

            Stage dataAnalysisStage = new Stage();
            dataAnalysisStage.setTitle("答题统计");

            dataAnalysisStage.initModality(Modality.WINDOW_MODAL);

            Window ownerWindow = stemLabel.getScene().getWindow();
            dataAnalysisStage.initOwner(ownerWindow);

            // 将加载的FXML内容放到新窗口
            dataAnalysisStage.setScene(new Scene(root));

            // 传入之前计算的数据
            dataAnalysisController.displayData(correctCount, incorrectCount, unansweredCount, accuracy);

            // 子窗口的渲染进程持续
            dataAnalysisStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("子窗口不能打开");
        }
    }

    /**
     * 菜单->清空记录
     * @param event
     */
    @FXML
    void executeClearRecords(ActionEvent event) {
        // 创建一个后台任务来执行数据库操作，避免UI卡顿
        Task<Void> resetTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 在后台执行重置操作
                questionService.resetAllQuestionStats();
                return null;
            }
        };

        // 后台任务完成后, 这个处理器会在UI线程上被调用
        resetTask.setOnSucceeded(e -> {
            for(int i = 0; i < allQuestions.size(); i++) {
                Question orinalQuestion = allQuestions.get(i);
                if (orinalQuestion.stat() != 0) {
                    allQuestions.set(i, orinalQuestion.withStat(0));
                }
            }

            questionButtonsMap.forEach((questionNo, button) -> {
                updateButtonAppearance(button, 0);
            });

            if (currentQuestionIndex != -1) {
                displayQuestion(currentQuestionIndex);
            }

            //showAlert("操作成功", "已清空答题记录");
        });

        resetTask.setOnFailed(e -> {
            resetTask.getException().printStackTrace();
            //showAlert("操作失败", "清空答题记录失败");
        });

        new Thread(resetTask).start();
    }
}