package io.github.mugaaaaa.service;

import io.github.mugaaaaa.model.Question;
import io.github.mugaaaaa.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 主要处理数据库和后端的交互.
 * <p>
 * 方法:
 * <ul>
 * <li>loadAllQuestions: 查询并返回题目列表allQuestions.</li>
 * <li>updateQuestionStatus: 更新对应编号题目在数据库里的信息.</li>
 * <li>resetAllQuestionStats: 重置答题状况, 把数据库里所有记录的stat都置0(未答状态)</li>
 * </ul>
 */
public class QuestionService {

    /**
     * 执行sql查询语句并查询到的题目记录转化为Question实例, 返回包含所有问题的列表allQuestions.
     */
    public List<Question> loadAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT no, stem, answ, op_A, op_B, op_C, op_D, op_E, stat FROM questions ORDER BY no";

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("no"),
                        rs.getString("stem"),
                        rs.getString("answ"),
                        rs.getString("op_A"),
                        rs.getString("op_B"),
                        rs.getString("op_C"),
                        rs.getString("op_D"),
                        rs.getString("op_E"),
                        rs.getInt("stat")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * 更新数据库对应记录的stat
     * @param questionNo 问题号
     * @param status 新状态
     */
    public void updateQuestionStatus(int questionNo, int status) {
        String sql = "UPDATE questions SET stat = ? WHERE no = ?";

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status);
            pstmt.setInt(2, questionNo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置答题状态, 把data.db里面的stat都重置为0
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