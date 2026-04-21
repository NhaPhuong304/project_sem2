package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Message;
import com.aptech.projectmgmt.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;

public class MessageService {

	private final MessageRepository messageRepository = new MessageRepository();

	public void sendQuestionToTeacher(int studentId, int teacherId, Integer taskId, String questionContent) {
		messageRepository.createMessage(studentId, teacherId, taskId, "QUESTION|" + questionContent.trim());
	}

	public void replyToStudent(int teacherId, int studentId, Integer taskId, String answerContent) {
		messageRepository.createMessage(teacherId, studentId, taskId, "ANSWER|" + answerContent.trim());
	}

	public List<Message> getQuestionsByTeacher(int teacherId) {
		return messageRepository.findQuestionsByTeacher(teacherId);
	}

	public List<Message> getAnswersByStudent(int studentId) {
		return messageRepository.findAnswersByStudent(studentId);
	}

	public List<Message> getConversationByTask(int taskId) {
		return messageRepository.findConversationByTask(taskId);
	}

	public List<Message> getInboxByStudent(int studentId) {
		return messageRepository.findByReceiverId(studentId);
	}

	public int countUnread(int studentId) {
		return messageRepository.countUnread(studentId);
	}

	public void createAnswerMessageForStudent(int teacherId, int studentId, Integer taskId, String answerContent) {
		messageRepository.createMessage(teacherId, studentId, taskId, "ANSWER|" + answerContent.trim());
	}

	public void markAsRead(int messageId) {
		messageRepository.markAsRead(messageId);
	}

	public void sendMessage(int senderId, int receiverId, Integer taskId, String content) {
		if (content == null || content.trim().isEmpty()) {
			throw new RuntimeException("Noi dung tin nhan khong duoc de trong");
		}
		Message msg = new Message();
		msg.setSenderId(senderId);
		msg.setReceiverId(receiverId);
		msg.setTaskId(taskId);
		msg.setContent(content.trim());
		messageRepository.insert(msg, LocalDateTime.now());
	}
}
