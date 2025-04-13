package com.demo.controller.user;

import com.demo.entity.News;
//import com.demo.exception.NewsNotFoundException;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(NewsController.class)
public class NewsControllerTest {

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

    // ========== 测试 /news 端点 ==========

    @DisplayName("1.1 测试获取单个新闻详情 - 成功")
    @Test
    public void testGetNewsById_Success() throws Exception {
        Mockito.when(newsService.findById(1)).thenReturn(testNews);

        mockMvc.perform(get("/news")
                        .param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(model().attribute("news", testNews))
                .andExpect(view().name("news"));
    }

    @DisplayName("1.2 测试获取单个新闻详情 - 新闻不存在")
    @Test
    public void testGetNewsById_NotFound() throws Exception {
        Mockito.when(newsService.findById(1)).thenReturn(null);

        mockMvc.perform(get("/news")
                        .param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(model().attribute("news", nullValue()))
                .andExpect(view().name("news"));
    }

    @DisplayName("1.3 测试获取单个新闻详情 - 无效ID(负数)")
    @Test
    public void testGetNewsById_InvalidNegativeId() throws Exception {
        mockMvc.perform(get("/news")
                        .param("newsID", "-1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("1.4 测试获取单个新闻详情 - 无效ID(字符串)")
    @Test
    public void testGetNewsById_InvalidStringId() throws Exception {
        mockMvc.perform(get("/news")
                        .param("newsID", "abc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("1.5 测试获取单个新闻详情 - 服务层异常")
    @Test
    public void testGetNewsById_ServiceException() throws Exception {
        Mockito.when(newsService.findById(anyInt())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/news")
                        .param("newsID", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError());
    }

    // ========== 测试 /news_list 端点 ==========

    @DisplayName("2.1 测试获取新闻列表页面 - 有新闻")
    @Test
    public void testNewsListPage_WithNews() throws Exception {
        Page<News> page = new PageImpl<>(Arrays.asList(testNews, testNews2), PageRequest.of(0, 5), 2);
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news_list"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attribute("news_list", hasSize(2)))
                .andExpect(model().attribute("total", 1)) // 每页5条，2条数据共1页
                .andExpect(view().name("news_list"));
    }

    @DisplayName("2.2 测试获取新闻列表页面 - 无新闻")
    @Test
    public void testNewsListPage_NoNews() throws Exception {
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0);
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/news_list"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attribute("news_list", hasSize(0)))
                .andExpect(model().attribute("total", 0))
                .andExpect(view().name("news_list"));
    }

    @DisplayName("2.3 测试获取新闻列表页面 - 服务层异常")
    @Test
    public void testNewsListPage_ServiceException() throws Exception {
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/news_list"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError());
    }

    // ========== 测试 /news/getNewsList 端点 ==========

    @DisplayName("3.1 测试获取新闻列表API - 默认分页")
    @Test
    public void testGetNewsList_DefaultPage() throws Exception {
        Page<News> page = new PageImpl<>(Arrays.asList(testNews), PageRequest.of(0, 5), 1);
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news/getNewsList"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @DisplayName("3.2 测试获取新闻列表API - 指定分页(page=2)")
    @Test
    public void testGetNewsList_Page2() throws Exception {
        Page<News> page = new PageImpl<>(Arrays.asList(testNews), PageRequest.of(1, 5), 10);
        Mockito.when(newsService.findAll(argThat(pageable -> pageable.getPageNumber() == 1)))
                .thenReturn(page);

        mockMvc.perform(get("/news/getNewsList?page=2"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @DisplayName("3.3 测试获取新闻列表API - 空列表")
    @Test
    public void testGetNewsList_Empty() throws Exception {
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0);
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/news/getNewsList"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @DisplayName("3.4 测试获取新闻列表API - 无效分页(page=0)")
    @Test
    public void testGetNewsList_InvalidPageZero() throws Exception {
        mockMvc.perform(get("/news/getNewsList?page=0"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("3.5 测试获取新闻列表API - 无效分页(负值)")
    @Test
    public void testGetNewsList_InvalidNegativePage() throws Exception {
        mockMvc.perform(get("/news/getNewsList?page=-1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("3.6 测试获取新闻列表API - 无效分页(非数字)")
    @Test
    public void testGetNewsList_InvalidNonNumericPage() throws Exception {
        mockMvc.perform(get("/news/getNewsList?page=abc"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("3.7 测试获取新闻列表API - 服务层异常")
    @Test
    public void testGetNewsList_ServiceException() throws Exception {
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/news/getNewsList"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("3.8 测试获取新闻列表API - 多页数据")
    @Test
    public void testGetNewsList_MultiplePages() throws Exception {
        List<News> newsList = Arrays.asList(testNews, testNews2, new News(), new News(), new News(), new News());
        Page<News> page = new PageImpl<>(newsList.subList(0, 5), PageRequest.of(0, 5), newsList.size());
        Mockito.when(newsService.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news/getNewsList"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @DisplayName("3.9 测试获取新闻列表API - 排序验证")
    @Test
    public void testGetNewsList_SortOrder() throws Exception {
        List<News> newsList = Arrays.asList(testNews2, testNews); // testNews2时间更早
        Page<News> page = new PageImpl<>(newsList, PageRequest.of(0, 5, Sort.by("time").descending()), 2);
        Mockito.when(newsService.findAll(argThat(pageable ->
                pageable.getSort().equals(Sort.by("time").descending())
        ))).thenReturn(page);

        mockMvc.perform(get("/news/getNewsList"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].newsID").value(1)) // 应该testNews在前
                .andExpect(jsonPath("$.content[1].newsID").value(2));
    }
}