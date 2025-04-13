package com.demo.controller.admin;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import com.demo.entity.Message;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminMessageController.class)
public class AdminMessageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageVoService messageVoService;

    @DisplayName("1.")
    @Test
    void testMessageManage_EmptyMessage() throws Exception {
        Page<Message> page = new PageImpl<>(Collections.emptyList());
        when(messageService.findWaitState(any())).thenReturn(page);

        mockMvc.perform(get("/message_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/message_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @DisplayName("2.")
    @Test
    void testMessageManage_WithMessages() throws Exception {
        // 创建两个示例 Message 对象（可设置必要属性）
        Message msg1 = new Message();
        Message msg2 = new Message();
        List<Message> messageList = Arrays.asList(msg1, msg2);
        // 构造一个非空分页对象，假设总记录数为20，分页大小为10，则 totalPages=2
        Page<Message> page = new PageImpl<>(messageList, PageRequest.of(0, 10), 20);

        when(messageService.findWaitState(any())).thenReturn(page);

        mockMvc.perform(get("/message_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/message_manage"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", page.getTotalPages()));
    }

    @DisplayName("3.")
    @Test
    void testMessageManage_Exception() throws Exception {
        when(messageService.findWaitState(any())).thenThrow(new RuntimeException("DB failed"));

        mockMvc.perform(get("/message_manage")).andExpect(status().is5xxServerError());

        // currently if messageService.findWaitState throw out exception, there is no
        // error handle
    }

    @DisplayName("4.")
    @Test
    void testMessageManage_Null() throws Exception {
        when(messageService.findWaitState(any())).thenReturn(null);

        mockMvc.perform(get("/message_manage")).andExpect(status().is5xxServerError());
        // also no handle if null is return from messageService
    }

    @DisplayName("5.") // 正常获取第一页 MessageVo 列表
    @Test
    void testMessageList_Normal() throws Exception {
        Message msg = new Message(); // 可配置属性
        List<Message> messageList = Arrays.asList(msg);
        List<MessageVo> voList = Arrays.asList(new MessageVo());

        when(messageService.findWaitState(any())).thenReturn(new PageImpl<>(messageList));
        when(messageVoService.returnVo(messageList)).thenReturn(voList);

        mockMvc.perform(get("/messageList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @DisplayName("6.") // page=0 边界值分析
    @Test
    void testMessageList_PageZero() throws Exception {
        when(messageService.findWaitState(any())).thenReturn(new PageImpl<>(Arrays.asList()));
        when(messageVoService.returnVo(any())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/messageList.do").param("page", "0"))
                .andExpect(status().isOk());
    }

    @DisplayName("7.") // 返回空列表情况
    @Test
    void testMessageList_EmptyList() throws Exception {
        when(messageService.findWaitState(any())).thenReturn(new PageImpl<>(Arrays.asList()));
        when(messageVoService.returnVo(any())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/messageList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @DisplayName("8.") // 异常情况：messageService 抛出异常
    @Test
    void testMessageList_ServiceException() throws Exception {
        when(messageService.findWaitState(any())).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/messageList.do").param("page", "1"))
                .andExpect(status().is5xxServerError());
    }

    @DisplayName("9.") // 正常通过 message
    @Test
    void testPassMessage_Success() throws Exception {
        mockMvc.perform(post("/passMessage.do").param("messageID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).confirmMessage(1);
    }

    @DisplayName("10.") // 异常情况
    @Test
    void testPassMessage_Exception() throws Exception {
        doThrow(new RuntimeException("fail")).when(messageService).confirmMessage(anyInt());

        mockMvc.perform(post("/passMessage.do").param("messageID", "2"))
                .andExpect(status().is5xxServerError());
    }

    @DisplayName("11.") // 拒绝 message 正常情况
    @Test
    void testRejectMessage_Success() throws Exception {
        mockMvc.perform(post("/rejectMessage.do").param("messageID", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).rejectMessage(3);
    }

    @DisplayName("12.") // 异常情况
    @Test
    void testRejectMessage_Exception() throws Exception {
        doThrow(new RuntimeException("fail")).when(messageService).rejectMessage(anyInt());

        mockMvc.perform(post("/rejectMessage.do").param("messageID", "2"))
                .andExpect(status().is5xxServerError());
    }

    @DisplayName("13.") // 删除 message 正常情况
    @Test
    void testDelMessage_Success() throws Exception {
        mockMvc.perform(get("/delMessage.do").param("messageID", "4"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).delById(4);
    }

    @DisplayName("14.") // 删除 message 异常处理
    @Test
    void testDelMessage_Exception() throws Exception {
        doThrow(new RuntimeException("fail")).when(messageService).delById(anyInt());

        mockMvc.perform(get("/delMessage.do").param("messageID", "4"))
                .andExpect(status().is5xxServerError());
    }

}
