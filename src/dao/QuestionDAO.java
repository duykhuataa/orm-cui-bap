package dao;

import model.Question;

public class QuestionDAO extends _BaseDAO<Question> {

    public QuestionDAO() {
        super(Question.class, "Question");
    }
}
