package com.bazz_techtronics.codefind.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nshidbaby on 11/7/2016.
 */

public class Response implements Serializable {
    private String a_id;
    private String c_id;
    private String q_id;

    private String question;
    private String question_title;
    private String comment;
    private String answer;
    private String link;
    private String score;
    private Integer answer_cnt;
    private Integer comment_cnt;
    private List<String> tags;
    private List<Tuple<String, String>> answers;
    private List<Tuple<String, String>> comments;

    public Response() {
        // empty constructor
    }

    /*** Get query id values and arrays ***/
    public void setQuestionId(String q_id) { this.q_id = q_id; }

    public String getQuestionId() {
        return q_id;
    }

    public void setAnswerId(String a_id) {
        this.a_id = a_id;
    }

    public String getAnswerId() {
        return a_id;
    }

    public void setCommentId(String c_id) {
        this.c_id = c_id;
    }

    public String getCommentId() {
        return c_id;
    }

    public List<String> getAnswerIds() {
        if (answers != null) {
            List<String> answerIds = new ArrayList<>();
            for (Tuple answer : answers) {
                answerIds.add(answer.getKey().toString());
            }
            return answerIds;
        }
        return null;
    }

    public List<String> getCommentIds() {
        if (comments != null) {
            List<String> commentIds = new ArrayList<>();
            for (Tuple comment : comments) {
                commentIds.add(comment.getKey().toString());
            }
            return commentIds;
        }
        return null;
    }

    /*** Get query values ***/
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionTitle() {
        return question_title;
    }

    public void setQuestionTitle(String question_title) {
        this.question_title = question_title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public void setAnswerCount(Integer answer_cnt) {
        this.answer_cnt = answer_cnt;
    }

    public Integer getAnswerCount() {
        return answer_cnt;
    }

    public void setCommentCount(Integer comment_cnt) { this.comment_cnt = comment_cnt; }

    public Integer getCommentCount() {
        return comment_cnt;
    }

    /*** Get query arrays  ***/
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setAnswers(List<Tuple<String, String>> answers) {
        this.answers = answers;
    }

    public List<String> getAnswers() {
        if (answers != null) {
            List<String> answerVals = new ArrayList<>();
            for (Tuple answer : answers) {
                answerVals.add(answer.getObj().toString());
            }
            return answerVals;
        }
        return null;
    }

    public void setComments(List<Tuple<String, String>> comments) { this.comments = comments; }

    public List<String> getComments() {
        if (comments != null) {
            List<String> commentVals = new ArrayList<>();
            for (Tuple comment : comments) {
                commentVals.add(comment.getObj().toString());
            }
            return commentVals;
        }
        return null;
    }
}