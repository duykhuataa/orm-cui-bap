package main;

import dao.*;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {
//        UserDAO udao = new UserDAO();
//        
//        ArrayList<User> _userList = udao.select()
//                .where("UserId", "=", 138)
//                .exec();
//
//        User u = new User();
//        
//        u.setUsername("new created usnerma");
//        u.setPassword("new crat");
//        u.setEmail("new cr email");
//        
//        udao.create(u);

        QuestionDAO qdao = new QuestionDAO();

//        Question q = new Question();
//        q.setQuestionText("1h 0 9");
//        q.setCourseId(1);
//
//        qdao.create(q);
//        Question q = qdao.getWhere("QuestionText", "LIKE", "%12vr%");
//        System.out.println(q);
//        ResultSet rs = qdao.select()
//                .innerJoin("ExamQuestion")
//                .on("QuestionId")
//                .innerJoin("QuestionAnswer")
//                .on("QuestionId")
//                .where("QuestionId", "215")
//                .printCurrentStatement()
//                .execBigQuery();
//
//        while (rs.next()) {
//            System.out.println(rs.getInt(10));
//        }
        qdao.select()
                .innerJoin("ExamQuestion")
                    .on("QuestionId")
                .printCurrentStatement()
                .printResultSet();
    }
}
