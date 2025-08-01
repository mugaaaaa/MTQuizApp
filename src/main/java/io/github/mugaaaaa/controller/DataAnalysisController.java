package io.github.mugaaaaa.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DataAnalysisController {

    @FXML private Label correctCountLabel;
    @FXML private Label incorrectCountLabel;
    @FXML private Label unansweredCountLabel;
    @FXML private Label totalCountLabel;
    @FXML private Label accuracyLabel;

    /**
     * 从主控制器接收统计数据并更新dataAnalysis.fxml子窗口的UI.
     * @param correct 答对数
     * @param incorrect 答错数
     * @param unanswered 未答数
     * @param accuracy 正确率
     */
    public void displayData(int correct, int incorrect, int unanswered, double accuracy) {
        correctCountLabel.setText(String.valueOf(correct));
        incorrectCountLabel.setText(String.valueOf(incorrect));
        unansweredCountLabel.setText(String.valueOf(unanswered));
        totalCountLabel.setText(String.valueOf(correct + incorrect + unanswered));
        accuracyLabel.setText(String.format("%.2f%%", accuracy));
    }
}