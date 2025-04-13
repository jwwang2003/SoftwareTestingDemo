package com.demo.controller.admin;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.junit.jupiter.api.Assertions.*;

import com.demo.controller.user.UserController;
import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import java.util.Collections;

@WebMvcTest(AdminUserController.class)
public class AdminUserControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUserID("testUser");
        testUser.setUserName("Test User");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setPhone("1234567890");
    }

    // 测试user_manage页面加载
    @Test
    void userManage_ShouldReturnViewWithPaginationData() throws Exception {
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(testUser),
                PageRequest.of(0, 10), 1);
        when(userService.findByUserID(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_manage"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", 1));
    }

    // 测试userList.do分页查询
    @Test
    void userList_WithValidPage_ShouldReturnUserList() throws Exception {
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userService.findByUserID(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/userList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userID").value("testUser"));
    }

    @Test
    void userList_WithPageZero_ShouldHandleException() throws Exception {
        when(userService.findByUserID(any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("Page index must not be less than zero"));

        mockMvc.perform(get("/userList.do").param("page", "0"))
                .andExpect(status().isBadRequest());
    }

    // 测试用户ID检查
    @Test
    void checkUserID_WhenNotExists_ShouldReturnTrue() throws Exception {
        when(userService.countUserID("newID")).thenReturn(0);

        mockMvc.perform(post("/checkUserID.do").param("userID", "newID"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkUserID_WhenExists_ShouldReturnFalse() throws Exception {
        when(userService.countUserID("existingID")).thenReturn(1);

        mockMvc.perform(post("/checkUserID.do").param("userID", "existingID"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // 测试添加用户
    @Test
    void addUser_WithValidData_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/addUser.do")
                        .param("userID", "newUser")
                        .param("userName", "New User")
                        .param("password", "pwd")
                        .param("email", "new@test.com")
                        .param("phone", "0987654321"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        verify(userService).create(argThat(user ->
                user.getUserID().equals("newUser") &&
                        user.getEmail().equals("new@test.com")
        ));
    }

    // 测试修改用户
    @Test
    void modifyUser_WithValidData_ShouldUpdate() throws Exception {
        when(userService.findByUserID("oldID")).thenReturn(testUser);

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", "updatedID")
                        .param("oldUserID", "oldID")
                        .param("userName", "Updated Name")
                        .param("password", "newPass")
                        .param("email", "updated@test.com")
                        .param("phone", "9876543210"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        assertEquals("updatedID", testUser.getUserID());
        assertEquals("Updated Name", testUser.getUserName());
        verify(userService).updateUser(testUser);
    }

    @Test
    void modifyUser_WhenUserNotFound_ShouldThrowException() throws Exception {
        when(userService.findByUserID("invalidID")).thenReturn(null);

        mockMvc.perform(post("/modifyUser.do")
                        .param("oldUserID", "invalidID"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void userAdd_ShouldReturnAddView() throws Exception {
        // 测试/user_add直接返回视图
        mockMvc.perform(get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_add"));
    }

    @Test
    void userEdit_WithValidId_ShouldReturnEditViewWithUser() throws Exception {
        // 准备测试数据
        User testUser = new User();
        testUser.setId(1);
        testUser.setUserID("testUser");
        when(userService.findById(1)).thenReturn(testUser);

        // 执行测试并验证
        mockMvc.perform(get("/user_edit").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", testUser));
    }

    @Test
    void userEdit_WithInvalidId_ShouldHandleError() throws Exception {
        // 模拟找不到用户的情况
        when(userService.findById(999)).thenReturn(null);

        // 执行测试并验证错误处理
        mockMvc.perform(get("/user_edit").param("id", "999"))
                .andExpect(status().isBadRequest()) // 注意：实际应根据项目规范返回404或其他状态码
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attributeDoesNotExist("user"));

    }

    // 测试删除用户
    @Test
    void delUser_WithExistingId_ShouldInvokeService() throws Exception {
        mockMvc.perform(post("/delUser.do").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).delByID(1);
    }

    @Test
    void delUser_WithInvalidId() throws Exception {
        mockMvc.perform(post("/delUser.do").param("id", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("false"));

        verify(userService).delByID(999);
    }
}