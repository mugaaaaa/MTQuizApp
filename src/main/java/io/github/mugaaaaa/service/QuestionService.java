package io.github.mugaaaaa.service;

// 修正一：添加了缺失的 import 语句
import io.github.mugaaaaa.model.Question;
import io.github.mugaaaaa.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    public List<Question> loadAllQuestions() {
        List<Question> questions = new ArrayList<>();
        // 确保 SELECT 语句包含了 stat 字段
        String sql = "SELECT no, stem, answ, op_A, op_B, op_C, op_D, op_E, stat FROM questions ORDER BY no";

        try (Connection conn = JdbcUtil.getConnection(); // 现在 JdbcUtil 能被找到了
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // 修正二：在构造函数中添加了第9个参数 stat
                questions.add(new Question(
                        rs.getInt("no"),
                        rs.getString("stem"),
                        rs.getString("answ"),
                        rs.getString("op_A"),
                        rs.getString("op_B"),
                        rs.getString("op_C"),
                        rs.getString("op_D"),
                        rs.getString("op_E"),
                        rs.getInt("stat") // 读取并传入 stat 字段的值
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public void updateQuestionStatus(int questionNo, int status) {
        String sql = "UPDATE questions SET stat = ? WHERE no = ?";

        try (Connection conn = JdbcUtil.getConnection(); // 现在 JdbcUtil 能被找到了
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, questionNo);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置答题状态, 把data.db里面的stat都重置为0(未答状态)
     */
    public void resetAllQuestionStats() {
        String sql = "UPDATE questions SET stat = 0";

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println("已重置");
        } catch (SQLException e) {
            System.out.println("重置失败");
        }
    }
}