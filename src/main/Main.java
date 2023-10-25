package main;

import model.*;
import dao.*;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<Question> questionList = new QuestionDAO()
                .select()
                .where("questionText", "LIKE", "%22%")
                .exec();
        
        questionList.forEach(q -> {
            System.out.println(q.getQuestionText());
        });
    }
}
