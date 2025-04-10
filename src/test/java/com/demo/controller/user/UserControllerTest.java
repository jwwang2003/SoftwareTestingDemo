package com.demo.controller.user;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.ui.Model;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testSignUp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    public void testLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testUser_info() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user_info"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_info"));
    }


    // Test cases for loginCheck.do
    @Test
    public void testUserLoginSuccess() throws Exception {
        User mockUser = new User(0,"user123","password123",0);
        mockUser.setIsadmin(0);
        when(userService.checkLogin("user123", "password123")).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/loginCheck.do")
                        .param("userID", "user123")
                        .param("password", "password123"))
                .andExpect(content().string("/index"))
                .andExpect(request().sessionAttribute("user", mockUser));
    }

    @Test
    public void testAdminLoginSuccess() throws Exception {
        User mockAdmin = new User();
        mockAdmin.setIsadmin(1);
        when(userService.checkLogin("admin123", "adminPass")).thenReturn(mockAdmin);

        mockMvc.perform(MockMvcRequestBuilders.post("/loginCheck.do")
                        .param("userID", "admin123")
                        .param("password", "adminPass"))
                .andExpect(content().string("/admin_index"))
                .andExpect(request().sessionAttribute("admin", mockAdmin));
    }

    @Test
    public void testLoginFailure() throws Exception {
        when(userService.checkLogin("invalid", "wrong")).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/loginCheck.do")
                        .param("userID", "invalid")
                        .param("password", "wrong"))
                .andExpect(content().string("false"));
    }

    // Test cases for register.do
    @Test
    public void testUserRegistrationSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register.do")
                        .param("userID", "newUser")
                        .param("userName", "New User")
                        .param("password", "newPass")
                        .param("email", "new@example.com")
                        .param("phone", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        verify(userService, times(1)).create(any(User.class));
    }

    // Test cases for updateUser.do
    @Test
    public void testUpdateUserWithPasswordAndPicture() throws Exception {
        User existingUser = new User();
        existingUser.setUserID("user123");
        when(userService.findByUserID("user123")).thenReturn(existingUser);

        MockMultipartFile file = new MockMultipartFile(
                "picture",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", existingUser);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/updateUser.do")
                        .file(file)
                        .param("userName", "Updated Name")
                        .param("userID", "user123")
                        .param("passwordNew", "newPassword")
                        .param("email", "updated@example.com")
                        .param("phone", "0987654321")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        verify(userService, times(1)).updateUser(existingUser);
    }

    // Test cases for checkPassword.do
    @Test
    public void testCheckPasswordCorrect() throws Exception {
        User mockUser = new User();
        mockUser.setPassword("correctPass");
        when(userService.findByUserID("user123")).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/checkPassword.do")
                        .param("userID", "user123")
                        .param("password", "correctPass"))
                .andExpect(content().string("true"));
    }

    @Test
    public void testCheckPasswordIncorrect() throws Exception {
        User mockUser = new User();
        mockUser.setPassword("correctPass");
        when(userService.findByUserID("user123")).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/checkPassword.do")
                        .param("userID", "user123")
                        .param("password", "wrongPass"))
                .andExpect(content().string("false"));
    }

    // Test cases for logout functionality
    @Test
    public void testUserLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new User());

        mockMvc.perform(MockMvcRequestBuilders.get("/logout.do")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andExpect(request().sessionAttributeDoesNotExist("user"));
    }

    @Test
    public void testAdminLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin", new User());

        mockMvc.perform(MockMvcRequestBuilders.get("/quit.do")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"))
                .andExpect(request().sessionAttributeDoesNotExist("admin"));
    }
}