package com.demo.controller.user;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
public class UserVenueControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    private Pageable getPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by("venueID").ascending());
    }

    private Venue createVenue(int id) {
        return new Venue(id, "venue", "description", 1, "picture", "address", "09:00", "20:00");
    }

    private void assertVenueAttributes(Venue venue) throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList?page=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content[0].venueID").value(venue.getVenueID()))
                .andExpect(jsonPath("$.content[0].venueName").value(venue.getVenueName()))
                .andExpect(jsonPath("$.content[0].description").value(venue.getDescription()))
                .andExpect(jsonPath("$.content[0].price").value(venue.getPrice()))
                .andExpect(jsonPath("$.content[0].picture").value(venue.getPicture()))
                .andExpect(jsonPath("$.content[0].address").value(venue.getAddress()))
                .andExpect(jsonPath("$.content[0].open_time").value("09:00"))
                .andExpect(jsonPath("$.content[0].close_time").value("20:00"));
    }

    @Test
    public void testShowPageSuccess() throws Exception {
        Venue venue = createVenue(1);
        when(venueService.findByVenueID(1)).thenReturn(venue);
        mockMvc.perform(get("/venue?venueID=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue"))
                .andExpect(model().attribute("venue", venue));
    }

    @Test
    public void testShowPageNotFound() {
        org.mockito.Mockito.when(venueService.findByVenueID(404))
                .thenThrow(new EntityNotFoundException("Venue with ID 404 not found"));
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/venue?venueID=404"));
        });
    }

    @Test
    public void testShowPageWithNoParam() {
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/venue"));
        });

    }

    @Test
    public void testShowPageWithNegParam() throws Exception {
        mockMvc.perform(get("/venueList.do").param("page", "-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowPageWithDatabaseError() throws Exception {
        when(venueService.findByVenueID(2)).thenThrow(new RuntimeException("Database error"));
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/venue?venueID=2"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.5", "abc" })
    public void testShowPageWithInvalidParams(String param) throws Exception {
        mockMvc.perform(get("/venue?venueID=" + param))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSingleVenueList() throws Exception {
        Pageable pageable = getPageable(0, 5);
        Page<Venue> page = new PageImpl<>(
                Collections.singletonList(createVenue(1)),
                pageable, 1);
        when(venueService.findAll(pageable)).thenReturn(page);
        assertVenueAttributes(createVenue(1));
    }

    @Test
    public void testSingleVenueListWithNoParam() throws Exception {
        Pageable pageable = getPageable(0, 5);
        Page<Venue> page = new PageImpl<>(
                Collections.singletonList(createVenue(1)),
                pageable, 1);
        when(venueService.findAll(pageable)).thenReturn(page);
        assertVenueAttributes(createVenue(1));
    }

    @Test
    public void testVenueListWithNegParam() {
        assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/venuelist/getVenueList?page=-1"));
        });
    }

    @Test
    public void testVenueListWhenDataNotFound() throws Exception {
        Pageable pageable = getPageable(1, 5);
        when(venueService.findAll(pageable)).thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));
        mockMvc.perform(get("/venuelist/getVenueList?page=2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    public void testVenueListWithFloatParam() throws Exception {
        mockMvc.perform(get("/venuelist/getVenueList?page=1.5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testMultiVenueList() throws Exception {
        List<Venue> venues = IntStream.range(0, 15)
                .mapToObj(i -> createVenue(i + 1))
                .collect(Collectors.toList());
        Pageable pageable = getPageable(0, 5);
        Page<Venue> page = new PageImpl<>(venues.subList(0, 5), pageable, 15);
        when(venueService.findAll(pageable)).thenReturn(page);
        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue_list"))
                .andExpect(model().attribute("total", 3))
                .andExpect(model().attribute("venue_list", venues.subList(0, 5)));
    }

    @Test
    public void testVenueListWhenDataEmpty() throws Exception {
        Pageable pageable = getPageable(0, 5);
        when(venueService.findAll(pageable)).thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));
        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue_list"))
                .andExpect(model().attribute("venue_list", Collections.emptyList()))
                .andExpect(model().attribute("total", 0));
    }
}
