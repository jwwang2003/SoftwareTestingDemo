package com.demo.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MessageVoServiceImplTests {

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private MessageVoServiceImpl messageVoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 4, 8, 10, 0);

    @Test
    public void testReturnMessageVoByMessageID_ValidMessageID() {
        // Arrange
        int messageID = 1;
        Message message = new Message();
        message.setMessageID(messageID);
        message.setUserID("user123");
        message.setContent("Sample content");
        message.setTime(FIXED_TIME);
        message.setState(1);

        User user = new User();
        user.setUserID("user123");
        user.setUserName("John Doe");
        user.setPicture("profile.jpg");

        when(messageDao.findByMessageID(messageID)).thenReturn(message);
        when(userDao.findByUserID("user123")).thenReturn(user);

        // Act
        MessageVo result = messageVoService.returnMessageVoByMessageID(messageID);

        // Assert
        assertNotNull(result);
        assertEquals(messageID, result.getMessageID());
        assertEquals("user123", result.getUserID());
        assertEquals("Sample content", result.getContent());
        assertEquals(FIXED_TIME, result.getTime());
        assertEquals("John Doe", result.getUserName());
        assertEquals("profile.jpg", result.getPicture());
        assertEquals(1, result.getState());
    }

    @Test
    public void testReturnMessageVoByMessageID_NonExistentMessageID() {
        // Arrange
        int messageID = 999;
        when(messageDao.findByMessageID(messageID)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageVoService.returnMessageVoByMessageID(messageID);
        });
    }

    @Test
    public void testReturnMessageVoByMessageID_MessageWithNonExistentUser() {
        // Arrange
        int messageID = 2;
        Message message = new Message();
        message.setMessageID(messageID);
        message.setUserID("nonExistentUser");

        when(messageDao.findByMessageID(messageID)).thenReturn(message);
        when(userDao.findByUserID("nonExistentUser")).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageVoService.returnMessageVoByMessageID(messageID);
        });
    }

    @Test
    public void testReturnVo_EmptyMessageList() {
        // Arrange
        List<Message> messages = new ArrayList<>();

        // Act
        List<MessageVo> result = messageVoService.returnVo(messages);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testReturnVo_NonEmptyMessageList() {
        // Arrange
        List<Message> messages = new ArrayList<>();
        Message message1 = new Message();
        message1.setMessageID(1);
        Message message2 = new Message();
        message2.setMessageID(2);
        messages.add(message1);
        messages.add(message2);

        MessageVoServiceImpl spyService = spy(messageVoService);
        doReturn(new MessageVo()).when(spyService).returnMessageVoByMessageID(anyInt());

        // Act
        List<MessageVo> result = spyService.returnVo(messages);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(spyService, times(2)).returnMessageVoByMessageID(anyInt());
    }

    @Test
    public void testReturnVo_ValidMessages() {
        // Arrange: Create a list of messages
        List<Message> messages = new ArrayList<>();
        Message message1 = new Message();
        message1.setMessageID(1);
        message1.setUserID("user123");
        message1.setContent("Hello, world!");
        message1.setTime(FIXED_TIME);
        message1.setState(1);

        Message message2 = new Message();
        message2.setMessageID(2);
        message2.setUserID("user456");
        message2.setContent("Goodbye, world!");
        message2.setTime(FIXED_TIME.plusHours(1));
        message2.setState(0);

        messages.add(message1);
        messages.add(message2);

        // Mock the return values from DAOs
        User user1 = new User();
        user1.setUserID("user123");
        user1.setUserName("John Doe");
        user1.setPicture("profile1.jpg");

        User user2 = new User();
        user2.setUserID("user456");
        user2.setUserName("Jane Doe");
        user2.setPicture("profile2.jpg");

        when(messageDao.findByMessageID(1)).thenReturn(message1);
        when(messageDao.findByMessageID(2)).thenReturn(message2);
        when(userDao.findByUserID("user123")).thenReturn(user1);
        when(userDao.findByUserID("user456")).thenReturn(user2);

        // Act: Call the method under test
        List<MessageVo> result = messageVoService.returnVo(messages);

        // Assert: Verify the size and correctness of the returned MessageVo objects
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check first MessageVo
        MessageVo messageVo1 = result.get(0);
        assertEquals(1, messageVo1.getMessageID());
        assertEquals("user123", messageVo1.getUserID());

        // Check second MessageVo
        MessageVo messageVo2 = result.get(1);
        assertEquals(2, messageVo2.getMessageID());
        assertEquals("user456", messageVo2.getUserID());

    }

    @Test
    public void testReturnVo_NullList() {
        assertThrows(NullPointerException.class, () -> {
            messageVoService.returnVo(null);
        });
    }

    @Test
    public void testReturnVo_MessageWithNonExistentUser() {
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        message.setMessageID(1);
        message.setUserID("nonExistentUser");
        messages.add(message);

        when(messageDao.findByMessageID(1)).thenReturn(message);
        when(userDao.findByUserID("nonExistentUser")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            messageVoService.returnVo(messages);
        });
    }

}
