package model;

import java.sql.Timestamp;

public class Question {

    private int questionId;
    private String questionText;
    private int questionTypeId;
    private int courseId;
    private Timestamp dateCreated;
    private byte isDeleted;

    public Question() {
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getQuestionTypeId() {
        return questionTypeId;
    }

    public void setQuestionTypeId(int questionTypeId) {
        this.questionTypeId = questionTypeId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public byte getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(byte isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "Question{" + "questionId=" + questionId + ", questionText=" + questionText + ", questionTypeId=" + questionTypeId + ", courseId=" + courseId + ", dateCreated=" + dateCreated + ", isDeleted=" + isDeleted + '}';
    }

}
