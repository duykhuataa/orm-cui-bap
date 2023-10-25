package main;

import model.*;
import dao.*;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
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
//        q.setQuestionText("1 cộng 1 bằng?");
//        q.setCourseId(1);
//
//        qdao.create(q);

        Question q = qdao.getWhere("QuestionId", 232);
        System.out.println(q);
    }
}
