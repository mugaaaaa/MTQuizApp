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
import javafx.stage.Window;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 主页面(main.fxml)控制器:
 * <p>
 * 方法包括:
 * <ul>
 * <li>initialize: 加载页面时自动调用. 调用loadDataInBackground方法.</li>
 * <li>loadDataInBackground: 后台调用loadAllQuestions方法.
 * loadAllQuestions方法查询数据库并将题目都转化为Question示例, 存入allQuestions列表.</li>
 * <li>populateQuestionButtons: 删除原来题目, 填充更新过后的新题目.</li>
 * <li>displayQuestion: 改变UI, 显示对应题号的题目.</li>
 * <li>handleSubmitAnswer: 提交答案后更新数据库, 调用updateButtonAppearance
 * 根据是否答对改变题目列表里面对应的按钮颜色, 在题干后追加正确答案.</li>
 * <li>updateButtonAppearance: 根据是否答对改变题目列表里面对应的按钮颜色.</li>
 * <li>handleLastQuestion: 如果当前不是第一题，就显示前一题</li>
 * <li>handleNextQuestion: 如果当前不是最后一题，就显示后一题</li>
 * <li>executeClearRecords: 重置答题记录, 题目列表里面的按钮颜色还原.
 * 后台执行数据库操作之后改变UI</li>
 * <li>showHelp: 展示"帮助"窗口</li>
 * <li>showAbout: 展示"关于"窗口</li>
 * <li>getFormattedCorrectAnswers: 得到问题答案的格式化字符串</li>
 * </ul>
 * </p>
 */
public class Controller {

    @FXML private FlowPane questionsFlowPane;
    @FXML private Label stemLabel;
    @FXML private VBox optionsContainer;
    @FXML private Button lastQuestionButton;
    @FXML private Button nextQuestionButton;
    @FXML private Button answerButton;

    private final QuestionService questionService = new QuestionService();
    private List<Question> allQuestions;
    private Map<Integer, Button> questionButtonsMap = new HashMap<>();  // 存储题号和按钮实例之间的映射关系
    private int currentQuestionIndex = -1;

    @FXML
    public void initialize() {
        loadDataInBackground();
    }

    /**
     * 在后台执行sql查询语句, 并将每个记录转化为Question实例, 装入allQuestions列表.
     */
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

    /**
     * 删除原先questionsFlowPane的题目, 并填充新题目.
     * <p>
     * FXML文件里面有个示例性质的Button示例, 初始化时要删除.
     * 调用executeClearRecords方法删除答题记录时也会调用本方法, 也要删除原先的Button实例.
     */
    private void populateQuestionButtons() {
        // 去掉之前questionsFlowPane里的Button实例和编号-Button实例映射关系.
        questionsFlowPane.getChildren().clear();
        questionButtonsMap.clear();

        for (int i = 0; i < allQuestions.size(); i++) {
            Question q = allQuestions.get(i);
            Button btn = new Button(String.valueOf(q.no()));

            btn.setPrefSize(40, 40);
            final int questionIndex = i;
            btn.setOnAction(e -> displayQuestion(questionIndex));
            updateButtonAppearance(btn, q.stat());  // 根据加载到的状态设置初始颜色

            questionsFlowPane.getChildren().add(btn);
            questionButtonsMap.put(q.no(), btn);
        }
    }

    /**
     * 改变UI, 显示对应题号的题目
     * @param index
     */
    private void displayQuestion(int index) {
        if (index < 0 || index >= allQuestions.size()) return;
        currentQuestionIndex = index;
        Question currentQuestion = allQuestions.get(index);

        // 更新题干区域
        String stemText = currentQuestion.stem();
        if (currentQuestion.stat() != 0) {
            stemText += "\n\n正确答案: " + getFormattedCorrectAnswers(currentQuestion);
        }
        stemLabel.setText(stemText);

        // 更新选项区域
        optionsContainer.getChildren().clear();
        List<String> options = currentQuestion.getOptions();
        for (String optionText : options) {
            CheckBox cb = new CheckBox(optionText);
            VBox.setMargin(cb, new Insets(10));

            if (currentQuestion.stat() != 0) {
                cb.setDisable(true);
                String optionChar = optionText.substring(0, 1);
                // 高亮正确答案
                if (currentQuestion.answer().contains(optionChar)) {
                    cb.setStyle("-fx-font-weight: bold;");
                }
            }
            optionsContainer.getChildren().add(cb);
        }

        // 更新底部按钮状态
        lastQuestionButton.setDisable(index == 0);
        nextQuestionButton.setDisable(index == allQuestions.size() - 1);
        answerButton.setDisable(currentQuestion.stat() != 0);
    }

    /**
     * 提交答案后, 更改数据库, 更新对应记录的stat.
     * 提交答案后会立即在题干里面追加正确答案, 根据是否答对更改questionsFlowPane对应按钮的颜色, 并禁用选项按钮.
     * @param event
     */
    @FXML
    void handleSubmitAnswer(ActionEvent event) {
        StringBuilder userAnswerBuilder = new StringBuilder();
        for (Node node : optionsContainer.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected()) {
                    // 提取选项字母(例如从 "A. xxx" 中提取 "A")
                    userAnswerBuilder.append(cb.getText().substring(0, 1));
                }
            }
        }
        String userAnswer = userAnswerBuilder.toString();

        if (userAnswer.isEmpty()) {
            System.out.println("请先选择一个答案！");
            return;
        }

        Question currentQuestion = allQuestions.get(currentQuestionIndex);

        // 比较答案(忽略顺序)
        // 将两个答案字符串排序后再比较, 可以忽略原始顺序(例如"AC"和"CA"会被认为是相同的)
        char[] userAnswerChars = userAnswer.toCharArray();
        char[] correctAnswerChars = currentQuestion.answer().toCharArray();
        java.util.Arrays.sort(userAnswerChars);
        java.util.Arrays.sort(correctAnswerChars);

        int newStatus;
        if (new String(userAnswerChars).equals(new String(correctAnswerChars))) {
            newStatus = 1; // 答对
        } else {
            newStatus = 2; // 答错
        }

        // 更新UI
        Button currentButton = questionButtonsMap.get(currentQuestion.no());
        updateButtonAppearance(currentButton, newStatus);

        // 立即显示正确答案
        stemLabel.setText(currentQuestion.stem() + "\n\n正确答案: " + getFormattedCorrectAnswers(currentQuestion));

        // 更新内存和禁用控件
        allQuestions.set(currentQuestionIndex, currentQuestion.withStat(newStatus));
        answerButton.setDisable(true);
        optionsContainer.getChildren().forEach(node -> node.setDisable(true));

        // 后台更新数据库
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


    /**
     * 根据状态更新按钮样式
     * @param button
     * @param status
     */
    private void updateButtonAppearance(Button button, int status) {
        button.getStyleClass().removeAll("correct-button", "incorrect-button"); // 先移除旧样式
        switch (status) {
            case 1: // 答对
                button.setStyle("-fx-background-color: #90EE90;"); // 亮绿色
                break;
            case 2: // 答错
                button.setStyle("-fx-background-color: #F08080;"); // 亮珊瑚色
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

    /**
     * 计算答对数, 答错数, 未答数, 正确率等统计数据.
     * 弹出窗口显示上面统计的数据.
     * @param event
     */
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

        // 弹出新窗口, 获取当前窗口为父窗口.
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

            dataAnalysisStage.setScene(new Scene(root));

            dataAnalysisController.displayData(correctCount, incorrectCount, unansweredCount, accuracy);

            dataAnalysisStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("子窗口不能打开");
        }
    }

    /**
     * 清空答题记录, 重置UI界面, 将数据库里面所有记录的stat置0.
     * 创建后台进程执行数据库操作以方式UI卡顿.
     * 在`其他功能->清空记录`里面调用.
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
        });

        resetTask.setOnFailed(e -> {
            resetTask.getException().printStackTrace();
        });

        new Thread(resetTask).start();
    }

    /**
     * 展示"帮助"窗口
     * @param event
     */
    @FXML
    void showHelp(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setHeight(400);
        alert.setTitle("帮助");
        alert.setHeaderText("软件使用说明");
        alert.setContentText("1.在左侧题目列表里点击对应编号的题目可以跳转至该题. \n\n" +
                "2.右下角的\"上一题\"和\"下一题\"按钮可以跳转到相邻的题目. \n\n" +
                "3.右侧中间栏选择选项后, 点击\"提交答案\"就可以看到作答情况. 若答对则左侧题目列表对应编号的按钮会变绿, 答错则会变红. \n\n" +
                "4.菜单栏中点击\"其他功能\"会显示\"清空记录\"和\"数据统计\"按钮. \"清空记录\"可以重置答题记录; \"数据统计\"可以查看答对题数, 答错题数, 未答题数和正确率等数据. \n\n" +
                "5.多选题均在题干后面标注了\"（多选题）\", 如果没有标\"（多选题）\"的都是单选题");
        alert.setResizable(true);
        alert.showAndWait();
    }

    /**
     * 展示"关于"窗口
     * @param event
     */
    @FXML
    void showAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setHeight(350);
        alert.setTitle("关于");
        alert.setHeaderText("关于本软件");
        alert.setContentText("作者: 某不知名的学长 & Mugaaaaa\n" +
                "\n" +
                "本软件是对一个古老的中山大学军理考试刷题软件的重置版. 原作者已不可考, 据说是信科院(今计院)的学长.\n\n" +
                "原软件只流传有一个exe文件, 在命令行运行, 但是其采用GB2313编码, 与现在设备默认的UTF-8不符, " +
                "可能导致乱码. 所幸软件的题库仍有文本文件留存, 本人编写解析脚本将题目数据存入SQLite数据库后, " +
                "用Java重新编写了一个UI界面, 希望能帮到学弟学妹们.");

        alert.setResizable(true);
        alert.showAndWait();
    }

    /**
     * 根据一个问题对象, 获取其所有正确答案的格式化字符串.
     * @param question 题目对象
     * @return 例如 "A. AnchorPane, C. TilePane"
     */
    private String getFormattedCorrectAnswers(Question question) {
        String correctKeys = question.answer(); // 例如 "AC"
        List<String> correctOptions = new ArrayList<>();

        for (String optionText : question.getOptions()) {
            String optionKey = optionText.substring(0, 1);
            if (correctKeys.contains(optionKey)) {
                correctOptions.add(optionText);
            }
        }
        // 使用逗号和空格将所有正确选项连接成一个字符串
        return String.join(", ", correctOptions);
    }
}