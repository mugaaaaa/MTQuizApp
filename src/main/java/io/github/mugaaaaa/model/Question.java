package io.github.mugaaaaa.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 使用 record 定义一个不可变的数据类来表示一道题目
public record Question(
        int no,          // 题号
        String stem,     // 题干
        String answer,   // 正确答案, 可能为多选
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String optionE,
        int stat
) {
    /**
     * 一个辅助方法, 用于获取所有非空的选项列表
     * @return 返回一个包含所有有效选项文本的列表, 例如 ["A. ...", "B. ..."]
     */
    public List<String> getOptions() {
        return Stream.of(
                        formatOption("A", optionA),
                        formatOption("B", optionB),
                        formatOption("C", optionC),
                        formatOption("D", optionD),
                        formatOption("E", optionE)
                )
                .filter(option -> option != null) // 由于D, E选项并不存在, 要过滤掉空的选项
                .collect(Collectors.toList());
    }

    private String formatOption(String prefix, String content) {
        if (content != null && !content.isBlank()) {
            return prefix + ". " + content;
        }
        return null;
    }

    public Question withStat(int newStat) {
        return new Question(
                this.no, this.stem, this.answer, this.optionA, this.optionB,
                this.optionC, this.optionD, this.optionE,
                newStat
        );
    }
}