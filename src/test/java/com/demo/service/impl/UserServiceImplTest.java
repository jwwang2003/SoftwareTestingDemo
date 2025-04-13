package com.demo.service.impl;

import com.demo.dao.UserDao;
import com.demo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUserID("testUser");
        testUser.setPassword("password123");
        testUser.setIsadmin(0);

        adminUser = new User();
        adminUser.setId(2);
        adminUser.setUserID("admin");
        adminUser.setPassword("admin123");
        adminUser.setIsadmin(1);
    }

    @Test
    void findByUserID_ShouldReturnUser_WhenUserExists() {
        // 等价类划分：存在的用户ID
        when(userDao.findByUserID("testUser")).thenReturn(testUser);

        User result = userService.findByUserID("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUserID());
        verify(userDao, times(1)).findByUserID("testUser");
    }

    @Test
    void findByUserID_ShouldReturnNull_WhenUserNotExists() {
        // 等价类划分：不存在的用户ID
        when(userDao.findByUserID(anyString())).thenReturn(null);

        User result = userService.findByUserID("nonExistingUser");

        assertNull(result);
        verify(userDao, times(1)).findByUserID("nonExistingUser");
    }

    @Test
    void findByUserID_ShouldHandleEmptyUserID() {
        // 边界值分析：空用户ID
        when(userDao.findByUserID("")).thenReturn(null);

        User result = userService.findByUserID("");

        assertNull(result);
        verify(userDao, times(1)).findByUserID("");
    }

    @Test
    void findById_ShouldReturnUser_WhenIdExists() {
        // 等价类划分：存在的ID
        when(userDao.findById(1)).thenReturn(testUser);

        User result = userService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(userDao, times(1)).findById(1);
    }

    @Test
    void findById_ShouldReturnNull_WhenIdNotExists() {
        // 等价类划分：不存在的ID
        when(userDao.findById(anyInt())).thenReturn(null);

        User result = userService.findById(999);

        assertNull(result);
        verify(userDao, times(1)).findById(999);
    }

    @Test
    void findById_ShouldHandleZeroId() {
        // 边界值分析：ID为0
        when(userDao.findById(0)).thenReturn(null);

        User result = userService.findById(0);

        assertNull(result);
        verify(userDao, times(1)).findById(0);
    }

    @Test
    void testFindByUserID_ShouldReturnPageOfNonAdminUsers() {
        // 语句覆盖和判定覆盖
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(userPage);
        Page<User> result = userService.findByUserID(pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("testUser", result.getContent().get(0).getUserID());
        verify(userDao, times(1)).findAllByIsadmin(0, pageable);
    }

    @Test
    void testFindByUserID_ShouldReturnEmptyPage_WhenNoNonAdminUsers() {
        // 等价类划分：没有非管理员用户
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(emptyPage);
        Page<User> result = userService.findByUserID(pageable);
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(userDao, times(1)).findAllByIsadmin(0, pageable);
    }

    @Test
    void checkLogin_ShouldReturnUser_WhenCredentialsCorrect() {
        // 等价类划分：正确的凭据
        when(userDao.findByUserIDAndPassword("testUser", "password123")).thenReturn(testUser);

        User result = userService.checkLogin("testUser", "password123");

        assertNotNull(result);
        assertEquals("testUser", result.getUserID());
        verify(userDao, times(1)).findByUserIDAndPassword("testUser", "password123");
    }

    @Test
    void checkLogin_ShouldReturnNull_WhenUserIDIncorrect() {
        // 等价类划分：错误的用户ID
        when(userDao.findByUserIDAndPassword(anyString(), anyString())).thenReturn(null);

        User result = userService.checkLogin("wrongUser", "password123");

        assertNull(result);
        verify(userDao, times(1)).findByUserIDAndPassword("wrongUser", "password123");
    }

    @Test
    void checkLogin_ShouldReturnNull_WhenPasswordIncorrect() {
        // 等价类划分：错误的密码
        when(userDao.findByUserIDAndPassword("testUser", "wrongPassword")).thenReturn(null);

        User result = userService.checkLogin("testUser", "wrongPassword");

        assertNull(result);
        verify(userDao, times(1)).findByUserIDAndPassword("testUser", "wrongPassword");
    }

    @Test
    void checkLogin_ShouldReturnNull_WhenBothIncorrect() {
        // 等价类划分：错误的用户ID和密码
        when(userDao.findByUserIDAndPassword("wrongUser", "wrongPassword")).thenReturn(null);

        User result = userService.checkLogin("wrongUser", "wrongPassword");

        assertNull(result);
        verify(userDao, times(1)).findByUserIDAndPassword("wrongUser", "wrongPassword");
    }

    @Test
    void checkLogin_ShouldHandleEmptyCredentials() {
        // 边界值分析：空凭据
        when(userDao.findByUserIDAndPassword("", "")).thenReturn(null);

        User result = userService.checkLogin("", "");

        assertNull(result);
        verify(userDao, times(1)).findByUserIDAndPassword("", "");
    }

    @Test
    void create_ShouldSaveUserAndReturnTotalCount() {
        // 语句覆盖
        List<User> existingUsers = Arrays.asList(testUser, adminUser);
        when(userDao.findAll()).thenReturn(existingUsers);
        when(userDao.save(any(User.class))).thenReturn(testUser);

        int result = userService.create(new User());

        assertEquals(2, result);
        verify(userDao, times(1)).save(any(User.class));
        verify(userDao, times(1)).findAll();
    }

    @Test
    void create_ShouldHandleEmptyUser() {
        // 边界值分析：空用户对象
        when(userDao.save(any(User.class))).thenReturn(new User());
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        int result = userService.create(new User());

        assertEquals(0, result);
        verify(userDao, times(1)).save(any(User.class));
        verify(userDao, times(1)).findAll();
    }

    @Test
    void delByID_ShouldDeleteUser_WhenIdExists() {
        // 语句覆盖
        doNothing().when(userDao).deleteById(anyInt());
        userService.delByID(1);
        verify(userDao, times(1)).deleteById(1);
    }

    @Test
    void delByID_ShouldHandleNonExistingId() {
        // 等价类划分：不存在的ID
        doNothing().when(userDao).deleteById(anyInt());
        userService.delByID(999);
        verify(userDao, times(1)).deleteById(999);
    }

    @Test
    void updateUser_ShouldSaveUser() {
        // 语句覆盖
        //doNothing().when(userDao).save(any(User.class));
        userService.updateUser(testUser);
        verify(userDao, times(1)).save(testUser);
    }

    @Test
    void updateUser_ShouldHandleNullUser() {
        // 边界值分析：空用户
        doThrow(new IllegalArgumentException()).when(userDao).save(isNull());
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null));
        verify(userDao, times(1)).save(null);
    }

    @Test
    void countUserID_ShouldReturnCount_WhenUserIDExists() {
        // 等价类划分：存在的用户ID
        when(userDao.countByUserID("testUser")).thenReturn(1);
        int result = userService.countUserID("testUser");
        assertEquals(1, result);
        verify(userDao, times(1)).countByUserID("testUser");
    }

    @Test
    void countUserID_ShouldReturnZero_WhenUserIDNotExists() {
        // 等价类划分：不存在的用户ID
        when(userDao.countByUserID("nonExistingUser")).thenReturn(0);
        int result = userService.countUserID("nonExistingUser");
        assertEquals(0, result);
        verify(userDao, times(1)).countByUserID("nonExistingUser");
    }

    @Test
    void countUserID_ShouldHandleEmptyUserID() {
        // 边界值分析：空用户ID
        when(userDao.countByUserID("")).thenReturn(0);

        int result = userService.countUserID("");

        assertEquals(0, result);
        verify(userDao, times(1)).countByUserID("");
    }
}