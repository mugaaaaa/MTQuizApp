package io.github.mugaaaaa.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 题目类, 包含编号, 题干, 正确答案, 五个选项(可能为空), 答题状态码stat.
 * sqlite数据库读出的数据将被转为Question实例以便在Java后端中传输.
 */
public record Question(
        int no,          // 题号
        String stem,     // 题干
        String answer,   // 正确答案, 可能为多选
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String optionE,
        int stat         // 答题状态 (0=未答, 1=答对, 2=答错)
) {
    /**
     * 一个辅助方法, 用于获取所有非空的选项列表.
     * 由于题目的选项数在3-5个不等, 故这里返回列表, 在表示阶段自动推断选项数量并显示.
     * @return 返回一个列表, 包含所有非空的选项.
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

    /**
     * 创建一个具有新答题状态的题目副本.
     * <p>
     * 由于Question是一个不可变记录, 只能返回一个包含所有旧数据和一个新状态值的新实例.
     *
     * @param newStat 新的答题状态.
     * @return 一个更新了答题状态的新的Question实例.
     */
    public Question withStat(int newStat) {
        return new Question(
                this.no, this.stem, this.answer, this.optionA, this.optionB,
                this.optionC, this.optionD, this.optionE,
                newStat
        );
    }
}