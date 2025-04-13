package com.demo.controller.admin;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(AdminNewsController.class)
public class AdminNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    private News testNews;
    private News testNews2;

    @BeforeEach
    public void setup() {
        testNews = new News();
        testNews.setNewsID(1);
        testNews.setTitle("Test News 1");
        testNews.setContent("Test Content 1");
        testNews.setTime(LocalDateTime.now());

        testNews2 = new News();
        testNews2.setNewsID(2);
        testNews2.setTitle("Test News 2");
        testNews2.setContent("Test Content 2");
        testNews2.setTime(LocalDateTime.now().minusDays(1));
    }

    // ========== 测试 /news_manage 端点 ==========

    @DisplayName("1.1 测试新闻管理页面 - 有新闻数据")
    @Test
    public void testNewsManage_WithNews() throws Exception {
        Page<News> page = new PageImpl<>(Arrays.asList(testNews, testNews2), PageRequest.of(0, 10), 2);
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news_manage"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", 1))
                .andExpect(view().name("admin/news_manage"));
    }

    @DisplayName("1.2 测试新闻管理页面 - 无新闻数据")
    @Test
    public void testNewsManage_NoNews() throws Exception {
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/news_manage"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", 0))
                .andExpect(view().name("admin/news_manage"));
    }

    @DisplayName("1.3 测试新闻管理页面 - 服务层异常")
    @Test
    public void testNewsManage_ServiceException() throws Exception {
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/news_manage"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError());
    }

    // ========== 测试 /news_add 端点 ==========

    @DisplayName("2.1 测试新闻添加页面")
    @Test
    public void testNewsAddPage() throws Exception {
        mockMvc.perform(get("/news_add"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_add"));
    }

    // ========== 测试 /news_edit 端点 ==========

    @DisplayName("3.1 测试新闻编辑页面 - 正常情况")
    @Test
    public void testNewsEditPage_Success() throws Exception {
        Mockito.when(newsService.findById(1)).thenReturn(testNews);

        mockMvc.perform(get("/news_edit").param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(model().attribute("news", testNews))
                .andExpect(view().name("/admin/news_edit"));
    }

    @DisplayName("3.2 测试新闻编辑页面 - 新闻不存在")
    @Test
    public void testNewsEditPage_NewsNotFound() throws Exception {
        Mockito.when(newsService.findById(1)).thenReturn(null);

        mockMvc.perform(get("/news_edit").param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(model().attribute("news", nullValue()))
                .andExpect(view().name("/admin/news_edit"));
    }

    @DisplayName("3.3 测试新闻编辑页面 - 无效ID(负数)")
    @Test
    public void testNewsEditPage_InvalidNegativeId() throws Exception {
        mockMvc.perform(get("/news_edit").param("newsID", "-1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("3.4 测试新闻编辑页面 - 无效ID(字符串)")
    @Test
    public void testNewsEditPage_InvalidStringId() throws Exception {
        mockMvc.perform(get("/news_edit").param("newsID", "abc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("3.5 测试新闻编辑页面 - 服务层异常")
    @Test
    public void testNewsEditPage_ServiceException() throws Exception {
        Mockito.when(newsService.findById(anyInt())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/news_edit").param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError());
    }

    // ========== 测试 /newsList.do 端点 ==========

    @DisplayName("4.1 测试获取新闻列表API - 默认分页")
    @Test
    public void testGetNewsList_DefaultPage() throws Exception {
        Page<News> page = new PageImpl<>(Arrays.asList(testNews), PageRequest.of(0, 10), 1);
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/newsList.do"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @DisplayName("4.2 测试获取新闻列表API - 指定分页(page=2)")
    @Test
    public void testGetNewsList_Page2() throws Exception {
        Page<News> page = new PageImpl<>(Arrays.asList(testNews), PageRequest.of(1, 10), 20);
        Mockito.when(newsService.findAll(argThat(pageable -> pageable.getPageNumber() == 1)))
                .thenReturn(page);

        mockMvc.perform(get("/newsList.do?page=2"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @DisplayName("4.3 测试获取新闻列表API - 空列表")
    @Test
    public void testGetNewsList_Empty() throws Exception {
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/newsList.do"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @DisplayName("4.4 测试获取新闻列表API - 无效分页(page=0)")
    @Test
    public void testGetNewsList_InvalidPageZero() throws Exception {
        mockMvc.perform(get("/newsList.do?page=0"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("4.5 测试获取新闻列表API - 无效分页(负值)")
    @Test
    public void testGetNewsList_InvalidNegativePage() throws Exception {
        mockMvc.perform(get("/newsList.do?page=-1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("4.6 测试获取新闻列表API - 无效分页(非数字)")
    @Test
    public void testGetNewsList_InvalidNonNumericPage() throws Exception {
        mockMvc.perform(get("/newsList.do?page=abc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("4.7 测试获取新闻列表API - 服务层异常")
    @Test
    public void testGetNewsList_ServiceException() throws Exception {
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/newsList.do"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError());
    }

    // ========== 测试 /delNews.do 端点 ==========

    @DisplayName("5.1 测试删除新闻 - 成功")
    @Test
    public void testDeleteNews_Success() throws Exception {
        Mockito.doNothing().when(newsService).delById(1);

        mockMvc.perform(post("/delNews.do")
                        .param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @DisplayName("5.2 测试删除新闻 - 新闻不存在")
    @Test
    public void testDeleteNews_NotFound() throws Exception {
        Mockito.doThrow(new RuntimeException("News not found")).when(newsService).delById(1);

        mockMvc.perform(post("/delNews.do")
                        .param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @DisplayName("5.3 测试删除新闻 - 无效ID(负数)")
    @Test
    public void testDeleteNews_InvalidNegativeId() throws Exception {
        mockMvc.perform(post("/delNews.do")
                        .param("newsID", "-1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("5.4 测试删除新闻 - 无效ID(字符串)")
    @Test
    public void testDeleteNews_InvalidStringId() throws Exception {
        mockMvc.perform(post("/delNews.do")
                        .param("newsID", "abc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("5.5 测试删除新闻 - 服务层异常")
    @Test
    public void testDeleteNews_ServiceException() throws Exception {
        Mockito.doThrow(new RuntimeException("Database error")).when(newsService).delById(1);

        mockMvc.perform(post("/delNews.do")
                        .param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    // ========== 测试 /modifyNews.do 端点 ==========

    @DisplayName("6.1 测试修改新闻 - 成功")
    @Test
    public void testModifyNews_Success() throws Exception {
        Mockito.when(newsService.findById(1)).thenReturn(testNews);
        Mockito.doNothing().when(newsService).update(ArgumentMatchers.any(News.class));

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "1")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
    }

    @DisplayName("6.2 测试修改新闻 - 新闻不存在")
    @Test
    public void testModifyNews_NotFound() throws Exception {
        Mockito.when(newsService.findById(1)).thenReturn(null);

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "1")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
    }

    @DisplayName("6.3 测试修改新闻 - 无效ID(负数)")
    @Test
    public void testModifyNews_InvalidNegativeId() throws Exception {
        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "-1")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("6.4 测试修改新闻 - 无效ID(字符串)")
    @Test
    public void testModifyNews_InvalidStringId() throws Exception {
        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "abc")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("6.5 测试修改新闻 - 服务层异常")
    @Test
    public void testModifyNews_ServiceException() throws Exception {
        Mockito.when(newsService.findById(anyInt())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "1")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is5xxServerError());
    }

    // ========== 测试 /addNews.do 端点 ==========

    @DisplayName("7.1 测试添加新闻 - 成功")
    @Test
    public void testAddNews_Success() throws Exception {
        Mockito.when(newsService.create(ArgumentMatchers.any(News.class))).thenReturn(1);

        mockMvc.perform(post("/addNews.do")
                        .param("title", "New Title")
                        .param("content", "New Content"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
    }

    @DisplayName("7.2 测试添加新闻 - 标题为空")
    @Test
    public void testAddNews_EmptyTitle() throws Exception {
        mockMvc.perform(post("/addNews.do")
                        .param("title", "")
                        .param("content", "New Content"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("7.3 测试添加新闻 - 内容为空")
    @Test
    public void testAddNews_EmptyContent() throws Exception {
        mockMvc.perform(post("/addNews.do")
                        .param("title", "New Title")
                        .param("content", ""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("7.4 测试添加新闻 - 服务层异常")
    @Test
    public void testAddNews_ServiceException() throws Exception {
        Mockito.when(newsService.create(ArgumentMatchers.any(News.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/addNews.do")
                        .param("title", "New Title")
                        .param("content", "New Content"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is5xxServerError());
    }
}