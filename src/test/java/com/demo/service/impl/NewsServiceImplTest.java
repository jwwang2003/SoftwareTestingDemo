package com.demo.service.impl;

import java.util.Collections;

import com.demo.entity.News;
import com.demo.dao.NewsDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import static org.mockito.Mockito.*;
import static org.junit.Assert.*;



@RunWith(MockitoJUnitRunner.class)  // 让 Mockito 在测试类中生效
public class NewsServiceImplTest {

    @Mock
    private NewsDao newsDao;

    @InjectMocks
    private NewsServiceImpl newsService;

    private News news;

    @Before
    public void setUp() {
        news = new News();
        news.setNewsID(1);
        news.setTitle("Test News");
        news.setContent("This is a test news content.");
    }

    @Test
    public void testFindAll() {
        // 模拟分页查询的行为
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")));
        Page<News> page = new PageImpl<>(Collections.singletonList(news), pageable, 1);
        when(newsDao.findAll(pageable)).thenReturn(page);

        Page<News> result = newsService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test News", result.getContent().get(0).getTitle());
    }

    @Test
    public void testFindById() {
        when(newsDao.getOne(1)).thenReturn(news);

        News result = newsService.findById(1);

        assertNotNull(result);
        assertEquals(news.getTitle(), result.getTitle());
    }

    @Test
    public void testCreate() {
        when(newsDao.save(news)).thenReturn(news);

        int newsID = newsService.create(news);

        assertEquals(1, newsID);
        verify(newsDao, times(1)).save(news);
    }

    @Test
    public void testDelById() {
        doNothing().when(newsDao).deleteById(1);

        newsService.delById(1);

        verify(newsDao, times(1)).deleteById(1);
    }

    @Test
    public void testUpdate() {
        when(newsDao.save(news)).thenReturn(news);

        newsService.update(news);

        verify(newsDao, times(1)).save(news);
    }
}