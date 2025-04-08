package com.demo.venue;

import com.demo.entity.Venue;
import com.demo.dao.VenueDao;
import com.demo.service.impl.VenueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class VenueServiceTest {

    @Mock
    private VenueDao venueDao;
    @InjectMocks
    private VenueServiceImpl venueService;
    private Venue testVenue;

    private void assertVenueEqual(Venue expected, Venue actual) {
        assertAll("Venue",
                () -> assertEquals(expected.getVenueID(), actual.getVenueID()),
                () -> assertEquals(expected.getVenueName(), actual.getVenueName()),
                () -> assertEquals(expected.getDescription(), actual.getDescription()),
                () -> assertEquals(expected.getPrice(), actual.getPrice()),
                () -> assertEquals(expected.getPicture(), actual.getPicture()),
                () -> assertEquals(expected.getAddress(), actual.getAddress()),
                () -> assertEquals(expected.getOpen_time(), actual.getOpen_time()),
                () -> assertEquals(expected.getClose_time(), actual.getClose_time()));
    }

    @BeforeEach
    void setUp() {
        int venueID = 1;
        String venue_name = "venue";
        String description = "this is description";
        int price = 200;
        String picture = "";
        String address = "address";
        String open_time = "09:00";
        String close_time = "20:00";
        testVenue = new Venue(venueID, venue_name, description, price, picture, address, open_time, close_time);
    }

    @Test
    void testFindByVenueID() {
        when(venueDao.getOne(1)).thenReturn(testVenue);
        Venue res = venueService.findByVenueID(1);
        assertNotNull(res);
        assertVenueEqual(testVenue, res);
        verify(venueDao).getOne(1);
    }

    @Test
    void testFindByVenueName() {
        when(venueDao.findByVenueName(testVenue.getVenueName())).thenReturn(testVenue);
        Venue res = venueService.findByVenueName("venue");
        assertVenueEqual(testVenue, res);
        verify(venueDao).findByVenueName(testVenue.getVenueName());
    }

    @Test
    void testFindByInvalidVenueID() {
        when(venueDao.getOne(999)).thenThrow(new EntityNotFoundException("Venue not found"));
        assertThrows(EntityNotFoundException.class, () -> venueService.findByVenueID(999));
    }

    @Test
    void testFindByNonExistingVenueName() {
        when(venueDao.findByVenueName("non_existing")).thenReturn(null);
        Venue res = venueService.findByVenueName("non_existing");
        assertNull(res);
        verify(venueDao).findByVenueName("non_existing");
    }

    @Test
    void testCreate() {
        when(venueDao.save(testVenue)).thenReturn(testVenue);
        assertEquals(1, venueService.create(testVenue));
        verify(venueDao).save(any());
    }

    @Test
    void testUpdate() {
        when(venueDao.save(any())).thenReturn(testVenue);
        venueService.update(testVenue);
        verify(venueDao).save(any());
    }

    @Test
    void testDelById() {
        venueService.delById(1);
        verify(venueDao).deleteById(1);
        venueService.delById(2);
        verify(venueDao).deleteById(2);
        verify(venueDao, times(2)).deleteById(anyInt());
    }

    @Test
    void testCountVenueName() {
        String venueName = "venue";
        when(venueDao.countByVenueName(venueName)).thenReturn(1).thenReturn(2);
        assertEquals(1, venueService.countVenueName(venueName));
        assertEquals(2, venueService.countVenueName(venueName));
        verify(venueDao, times(2)).countByVenueName(venueName);
    }

    @Test
    void testFindAllReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Venue> page = new PageImpl<>(Collections.singletonList(testVenue), pageable, 1);
        when(venueDao.findAll(pageable)).thenReturn(page);
        Page<Venue> res = venueService.findAll(pageable);
        assertEquals(1, res.getTotalElements());
        assertEquals(1, res.getContent().size());
        verify(venueDao).findAll(pageable);

        when(venueDao.findAll(pageable)).thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));
        Page<Venue> emptyRes = venueService.findAll(pageable);
        assertEquals(0, emptyRes.getTotalElements());
        assertEquals(0, emptyRes.getContent().size());
        verify(venueDao, times(2)).findAll(pageable);
    }

    @Test
    void testFindAllReturnList() {
        when(venueDao.findAll()).thenReturn(Collections.singletonList(testVenue));
        List<Venue> res = venueService.findAll();
        assertEquals(1, res.size());
        verify(venueDao).findAll();
    }
}