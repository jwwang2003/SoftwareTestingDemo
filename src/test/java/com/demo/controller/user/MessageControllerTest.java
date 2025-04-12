package com.demo.controller.user;

import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.exception.LoginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(MessageController.class)
public class MessageControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private MessageService messageService;

        @MockBean
        private MessageVoService messageVoService;

        private MockHttpSession session;
        private User testUser;

        @BeforeEach
        public void setup() {
                testUser = new User();
                testUser.setUserID("user123");
                session = new MockHttpSession();
                session.setAttribute("user", testUser);
        }

        @DisplayName("1.") // 未登录
        @Test
        public void testMessageList_Unauthenticated_ShouldThrowLoginException() throws Exception {
                mockMvc.perform(get("/message_list"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(result -> assertTrue(
                                                result.getResolvedException() instanceof LoginException));
        }

        @DisplayName("2.") // 登录用户无留言
        @Test
        public void testMessageList_Authenticated_NoMessages() throws Exception {
                Page<Message> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0);
                Mockito.when(messageService.findPassState(any())).thenReturn(emptyPage);
                Mockito.when(messageService.findByUser(eq(testUser.getUserID()), any())).thenReturn(emptyPage);
                Mockito.when(messageVoService.returnVo(anyList())).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/message_list").session(session))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("total", 0))
                                .andExpect(model().attribute("user_total", 0))
                                .andExpect(view().name("message_list"));
        }

        @DisplayName("3.") // 登录用户有留言
        @Test
        public void testMessageList_Authenticated_WithMessages() throws Exception {
                Message msg = new Message();
                Page<Message> page = new PageImpl<>(Arrays.asList(msg), PageRequest.of(0, 5), 1);
                Mockito.when(messageService.findPassState(any())).thenReturn(page);
                Mockito.when(messageService.findByUser(eq(testUser.getUserID()), any())).thenReturn(page);
                Mockito.when(messageVoService.returnVo(anyList())).thenReturn(Arrays.asList(new MessageVo()));

                mockMvc.perform(get("/message_list").session(session))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("total", 1))
                                .andExpect(model().attribute("user_total", 1))
                                .andExpect(view().name("message_list"));
        }

        @DisplayName("4.") // 默认分页 /message/getMessageList
        @Test
        public void testGetMessageList_DefaultPage() throws Exception {
                Mockito.when(messageService.findPassState(any()))
                                .thenReturn(new PageImpl<>(Arrays.asList(new Message())));
                Mockito.when(messageVoService.returnVo(anyList())).thenReturn(Arrays.asList(new MessageVo()));

                mockMvc.perform(get("/message/getMessageList"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @DisplayName("5.") // 指定分页 page=2
        @Test
        public void testGetMessageList_Page2() throws Exception {
                Mockito.when(messageService.findPassState(any()))
                                .thenReturn(new PageImpl<>(Arrays.asList(new Message())));
                Mockito.when(messageVoService.returnVo(anyList())).thenReturn(Arrays.asList(new MessageVo()));

                mockMvc.perform(get("/message/getMessageList?page=2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @DisplayName("6.") // 未登录用户查留言
        @Test
        public void testFindUserMessages_Unauthenticated_ShouldThrow() throws Exception {
                mockMvc.perform(get("/message/findUserList"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(result -> assertTrue(
                                                result.getResolvedException() instanceof LoginException));
        }

        @DisplayName("7.") // 登录用户有留言
        @Test
        public void testFindUserMessages_WithMessages() throws Exception {
                Mockito.when(messageService.findByUser(eq(testUser.getUserID()), any()))
                                .thenReturn(new PageImpl<>(Arrays.asList(new Message())));
                Mockito.when(messageVoService.returnVo(anyList()))
                                .thenReturn(Arrays.asList(new MessageVo()));

                mockMvc.perform(get("/message/findUserList").session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @DisplayName("8.") // 登录用户无留言
        @Test
        public void testFindUserMessages_NoMessages() throws Exception {
                Mockito.when(messageService.findByUser(eq(testUser.getUserID()), any()))
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                Mockito.when(messageVoService.returnVo(anyList()))
                                .thenReturn(Collections.emptyList());

                mockMvc.perform(get("/message/findUserList").session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @DisplayName("9.") // 发送留言
        @Test
        public void testSendMessage_Success() throws Exception {
                Mockito.when(messageService.create(any(Message.class))).thenReturn(1);

                mockMvc.perform(post("/sendMessage")
                                .param("userID", "user123")
                                .param("content", "Test message"));

        }

        @DisplayName("10.") // 异常情况
        @Test
        public void testSendMessage_Exception() throws Exception {
                Mockito.when(messageService.create(any(Message.class)))
                                .thenThrow(new RuntimeException("Database error"));

                mockMvc.perform(post("/sendMessage.do")
                                .param("messageID", "1"))
                                .andExpect(status().isOk());

        }

        @DisplayName("11.") // 修改留言
        @Test
        public void testModifyMessage_Success() throws Exception {
                Message msg = new Message();
                Mockito.when(messageService.findById(1)).thenReturn(msg);
                Mockito.doNothing().when(messageService).update(any());

                mockMvc.perform(post("/modifyMessage.do")
                                .param("messageID", "1")
                                .param("content", "Updated content"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("true"));
        }

        @DisplayName("12.") // 异常
        @Test
        public void testModifyMessage_Exception() throws Exception {
                Message msg = new Message();
                Mockito.when(messageService.findById(1)).thenThrow(new RuntimeException("Database error"));
                Mockito.doNothing().when(messageService).update(any());

                mockMvc.perform(post("/modifyMessage.do")
                                .param("messageID", "1")
                                .param("content", "Updated content"))
                                .andExpect(status().isOk());

        }

        @DisplayName("13.") // 删除
        @Test
        public void testDelMessage() throws Exception {
                Mockito.doNothing().when(messageService).delById(1);

                mockMvc.perform(post("/delMessage.do")
                                .param("messageID", "1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("true"));
        }

        @DisplayName("14.") // 异常
        @Test
        public void testDelMessage_Exception() throws Exception {
                Mockito.doThrow(new RuntimeException()).when(messageService).delById(1);

                mockMvc.perform(post("/delMessage.do")
                                .param("messageID", "1"))
                                .andExpect(status().isOk());

        }
}
