package com.demo.service.impl;

import com.demo.entity.News;
import com.demo.dao.NewsDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUnit 5 用法
public class NewsServiceImplTest {

    @Mock
    private NewsDao newsDao;

    @InjectMocks
    private NewsServiceImpl newsService;

    private News news;

    @BeforeEach
    public void setUp() {
        news = new News();
        news.setNewsID(1);
        news.setTitle("Test News");
        news.setContent("This is a test news content.");
    }

    @Test
    public void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")));
        Page<News> page = new PageImpl<>(Collections.singletonList(news), pageable, 1);
        when(newsDao.findAll(pageable)).thenReturn(page);

        Page<News> result = newsService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test News", result.getContent().get(0).getTitle());
    }

    @Test
    public void testFindAll_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(newsDao.findAll(pageable)).thenReturn(emptyPage);

        Page<News> result = newsService.findAll(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }



    @Test
    public void testFindById() {
        when(newsDao.getOne(1)).thenReturn(news);

        News result = newsService.findById(1);

        assertNotNull(result);
        assertEquals(news.getTitle(), result.getTitle());
    }

    @Test
    public void testFindById_NewsNotFound() {
        when(newsDao.findById(99)).thenReturn(Optional.empty());

        News result = newsService.findById(99);

        assertNull(result);
    }

    @Test
    public void testFindById_InvalidId_Zero() {
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.findById(0);
        });
    }



    @Test
    public void testCreate() {
        when(newsDao.save(news)).thenReturn(news);

        int newsID = newsService.create(news);

        assertEquals(1, newsID);
        verify(newsDao, times(1)).save(news);
    }

    @Test
    public void testCreate_WithNullNews() {
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.create(null);
        });
    }

    @Test
    public void testCreate_WithEmptyTitle() {
        News invalidNews = new News();
        invalidNews.setContent("Some content");

        assertThrows(IllegalArgumentException.class, () -> {
            newsService.create(invalidNews);
        });
    }




    @Test
    public void testDelById() {
        doNothing().when(newsDao).deleteById(1);

        newsService.delById(1);

        verify(newsDao, times(1)).deleteById(1);
    }

    @Test
    public void testDelById_NewsNotFound() {
        doThrow(new EntityNotFoundException()).when(newsDao).deleteById(99);

        assertThrows(EntityNotFoundException.class, () -> {
            newsService.delById(99);
        });
    }

    @Test
    public void testDelById_InvalidId_Zero() {
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.delById(0);
        });
    }





    @Test
    public void testUpdate() {
        when(newsDao.save(news)).thenReturn(news);

        newsService.update(news);

        verify(newsDao, times(1)).save(news);
    }

    @Test
    public void testUpdate_WithEmptyTitle() {
        News invalidNews = new News();
        invalidNews.setNewsID(1);
        invalidNews.setContent("Some content");

        assertThrows(IllegalArgumentException.class, () -> {
            newsService.update(invalidNews);
        });
    }

    @Test
    public void testUpdate_NewsNotFound() {
        when(newsDao.save(any(News.class))).thenThrow(new EntityNotFoundException());

        assertThrows(EntityNotFoundException.class, () -> {
            newsService.update(news);
        });
    }

    @Test
    public void testUpdate_WithDatabaseError() {
        when(newsDao.save(any(News.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            newsService.update(news);
        });
    }

}
