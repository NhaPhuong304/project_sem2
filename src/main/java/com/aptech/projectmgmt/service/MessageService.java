package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Message;
import com.aptech.projectmgmt.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;

public class MessageService {

    private final MessageRepository messageRepository = new MessageRepository();

    public List<Message> getInboxByStudent(int studentId) {
        return messageRepository.findByReceiverId(studentId);
    }

    public int countUnread(int studentId) {
        return messageRepository.countUnread(studentId);
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
