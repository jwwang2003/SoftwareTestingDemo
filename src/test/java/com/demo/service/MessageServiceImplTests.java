package com.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.MetaAnnotationUtils;
import org.springframework.data.domain.Pageable;

import com.demo.dao.MessageDao;
import com.demo.entity.Message;
import com.demo.service.impl.MessageServiceImpl;

public class MessageServiceImplTests {

    @InjectMocks
    private MessageServiceImpl messageService;

    @Mock
    private MessageDao messageDao;

    // testing Message
    Message defaultMessage;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        // Initialize the mocks
        MockitoAnnotations.openMocks(this);

        defaultMessage = new Message();
        defaultMessage.setContent("Testing");
        defaultMessage.setMessageID(100);
        defaultMessage.setState(1);
        defaultMessage.setTime(LocalDateTime.now());
        defaultMessage.setUserID("test");

        pageable = PageRequest.of(0, 10);
    }

    // findById
    @Test
    void testFindById_Exist() { // normal case

        when(messageDao.getOne(defaultMessage.getMessageID())).thenReturn(defaultMessage);
        Message test = messageService.findById(defaultMessage.getMessageID());
        assertEquals("Testing", test.getContent());
        assertEquals("test", test.getUserID());

    }

    @Test
    public void testFindById_NonExist() { // return null for not found
        int nonExistingId = 9999;
        when(messageDao.getOne(nonExistingId)).thenReturn(null);
        Message test = messageService.findById(nonExistingId);
        assertNull(test);
    }

    @Test
    public void testFindById_Invalid() { // throw exception for invalid
        int invalidId = -1;
        when(messageDao.getOne(invalidId)).thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class, () -> {
            messageService.findById(invalidId);
        });
    }

    @Test
    public void testFindByUser_WithMessages() {
        String userID = "user1";
        Pageable pageable = PageRequest.of(0, 5);

        // 模拟返回一个包含 defaultMessage 的分页结果
        List<Message> messages = new ArrayList<Message>();
        messages.add(defaultMessage);
        Page<Message> page = new PageImpl<>(messages, pageable, messages.size());
        when(messageDao.findAllByUserID(userID, pageable)).thenReturn(page);

        Page<Message> resultPage = messageService.findByUser(userID, pageable);
        assertNotNull(resultPage, "返回的分页对象不应为null");
        assertFalse(resultPage.getContent().isEmpty(), "返回的消息列表不应为空");
        assertEquals(1, resultPage.getTotalElements(), "消息总数应为1");
        assertEquals(defaultMessage.getMessageID(), resultPage.getContent().get(0).getMessageID(), "消息ID应匹配");
    }

    @Test
    public void testFindByUser_NoMessages() { // return empty page, if user exist but no message
        String userID = "user2";
        Pageable pageable = PageRequest.of(0, 5);

        // 模拟返回一个空的分页结果
        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(messageDao.findAllByUserID(userID, pageable)).thenReturn(emptyPage);

        Page<Message> resultPage = messageService.findByUser(userID, pageable);
        assertNotNull(resultPage, "返回的分页对象不应为null");
        assertTrue(resultPage.getContent().isEmpty(), "消息列表应为空");
        assertEquals(0, resultPage.getTotalElements(), "消息总数应为0");
    }

    @Test
    public void testFindByUser_invalidUser() { // return null, if the user is invalid or not exist
        String userID = "invalidUser";
        Pageable pageable = PageRequest.of(0, 5);

        // 模拟返回一个空的分页结果
        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(messageDao.findAllByUserID(userID, pageable)).thenReturn(null);

        Page<Message> resultPage = messageService.findByUser(userID, pageable);
        assertNull(resultPage);

    }

    @Test
    public void testCreate_ValidMessage() {
        // 模拟 DAO 保存并返回一个具有 messageID 的 Message 对象
        when(messageDao.save(defaultMessage)).thenReturn(defaultMessage);

        int savedId = messageService.create(defaultMessage);
        assertEquals(defaultMessage.getMessageID(), savedId, "保存后返回的 messageID 应该和期望一致");
    }

    @Test
    public void testCreate_NullMessage() {
        // 如果业务期望 null 输入抛出异常，则验证异常行为
        assertThrows(NullPointerException.class, () -> {
            messageService.create(null);
        });
    }

    @Test
    public void testDelById_ValidId() {
        int validId = 1;
        messageService.delById(validId);
        // 验证 messageDao.deleteById 被调用了一次
        verify(messageDao, times(1)).deleteById(validId);
    }

    @Test
    public void testDelById_InvalidId() {
        int invalidId = 9999;
        // 模拟当调用 deleteById(invalidId) 时 DAO 抛出异常
        doThrow(new EmptyResultDataAccessException(1)).when(messageDao).deleteById(invalidId);

        // 验证抛出预期的异常
        assertThrows(EmptyResultDataAccessException.class, () -> {
            messageService.delById(invalidId);
        });
    }

    @Test
    public void testUpdate_ValidMessage() {
        // Since update returns void, we just call the method and then verify the
        // interaction.
        messageService.update(defaultMessage);
        // Check that the DAO's save method is invoked exactly once with defaultMessage.
        verify(messageDao, times(1)).save(defaultMessage);
    }

    @Test
    public void testUpdate_NullMessage() {

        when(messageDao.save(null)).thenReturn(null);

        // Assert that no exception is thrown.
        assertDoesNotThrow(() -> {
            messageService.update(null);
        });
    }

    @Test
    public void testConfirmMessage_MessageExists() {
        when(messageDao.findByMessageID(1)).thenReturn(defaultMessage);

        messageService.confirmMessage(1);

        verify(messageDao, times(1)).updateState(2, defaultMessage.getMessageID());

    }

    @Test
    public void testConfirmMessage_MessageNotFound() {
        when(messageDao.findByMessageID(1)).thenReturn(null);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            messageService.confirmMessage(1);
        });

        assertEquals("留言不存在", thrown.getMessage());
        verify(messageDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    public void testRejectMessage_MessageExists() {
        when(messageDao.findByMessageID(defaultMessage.getMessageID())).thenReturn(defaultMessage);

        messageService.rejectMessage(defaultMessage.getMessageID());

        verify(messageDao, times(1)).updateState(3, defaultMessage.getMessageID());
    }

    @Test
    public void testRejectMessage_MessageNotFound() {
        when(messageDao.findByMessageID(1)).thenReturn(null);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            messageService.rejectMessage(1);
        });

        assertEquals("留言不存在", thrown.getMessage());
        verify(messageDao, never()).updateState(anyInt(), anyInt());
    }

    // state 1: wait, 2: confirm , 3: reject

    @Test
    public void testFindWaitState() {
        Message message1 = new Message();
        message1.setMessageID(2);
        message1.setState(1);

        Message message2 = new Message();
        message2.setMessageID(3);
        message2.setState(1);

        when(messageDao.findAllByState(1, pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(message1, message2), pageable, 2));

        Page<Message> result = messageService.findWaitState(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getState());
    }

    @Test
    public void testFindPassState() {
        Message message1 = new Message();
        message1.setMessageID(4);
        message1.setState(2);
        // Set other properties as needed

        Message message2 = new Message();
        message2.setMessageID(5);
        message2.setState(2);
        // Set other properties as needed

        when(messageDao.findAllByState(2, pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(message1, message2), pageable, 2));

        Page<Message> result = messageService.findPassState(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().get(0).getState());
    }
}
