package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Question;
import com.aptech.projectmgmt.repository.QuestionRepository;

import java.util.List;

public class QuestionService {

    private final QuestionRepository questionRepository = new QuestionRepository();

    public void createQuestion(int studentId, int teacherId, Integer taskId, String questionContent) {
        if (questionContent == null || questionContent.trim().isEmpty()) {
            throw new RuntimeException("Noi dung cau hoi khong duoc de trong");
        }
        questionRepository.createQuestion(studentId, teacherId, taskId, questionContent.trim());
    }

    public void answerQuestion(int questionId, String answerContent) {
        if (answerContent == null || answerContent.trim().isEmpty()) {
            throw new RuntimeException("Noi dung tra loi khong duoc de trong");
        }
        questionRepository.answerQuestion(questionId, answerContent.trim());
    }

    public List<Question> getQuestionsByTeacher(int teacherId) {
        return questionRepository.findByTeacherId(teacherId);
    }

    public List<Question> getQuestionsByStudent(int studentId) {
        return questionRepository.findByStudentId(studentId);
    }

    public Question getQuestionById(int questionId) {
        return questionRepository.findById(questionId);
    }
}