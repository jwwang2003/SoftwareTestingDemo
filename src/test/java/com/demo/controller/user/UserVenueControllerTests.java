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
import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        @Test
        public void testToGymPageWhenValidVenueID() throws Exception {
                Venue venue = createVenue(1);
                when(venueService.findByVenueID(1)).thenReturn(venue);
                mockMvc.perform(get("/venue?venueID=1"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("venue"))
                                .andExpect(model().attribute("venue", venue));
        }

        @Test
        void testToGymPageWhenVenueIDNotFound() throws Exception {
                when(venueService.findByVenueID(404))
                                .thenThrow(EntityNotFoundException.class);
                mockMvc.perform(get("/venue?venueID=404"))
                                .andExpect(status().isNotFound());
        }

        @Test
        public void testToGymPageWhenNoParam() throws Exception {
                mockMvc.perform(get("/venue"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testToGymPageWhenVenueIDIsNeg() throws Exception {
                mockMvc.perform(get("/venue?venueID=-1"))
                                .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = { "1.5", "abc" })
        public void testToGymPageWhenVenueIDIsInvalid(String param) throws Exception {
                mockMvc.perform(get("/venue?venueID=" + param))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testToGymPageWhenDatabaseError() throws Exception {
                when(venueService.findByVenueID(2)).thenThrow(new RuntimeException("Database error"));
                mockMvc.perform(get("/venue?venueID=2"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testVenueListWhenPageIsValid() throws Exception {
                Pageable venue_pageable = PageRequest.of(0, 5, Sort.by("venueID").ascending());
                Page<Venue> page = new PageImpl<>(Collections.singletonList(
                                new Venue(1, "venue_name", "description", 1, "picture", "address", "09:00", "20:00")),
                                venue_pageable,
                                1);
                when(venueService.findAll(venue_pageable))
                                .thenReturn(page);
                mockMvc.perform(get("/venuelist/getVenueList?page=1"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$.content[0].venueID").value(1))
                                .andExpect(jsonPath("$.content[0].venueName").value("venue_name"))
                                .andExpect(jsonPath("$.content[0].description").value("description"))
                                .andExpect(jsonPath("$.content[0].price").value(1))
                                .andExpect(jsonPath("$.content[0].picture").value("picture"))
                                .andExpect(jsonPath("$.content[0].address").value("address"))
                                .andExpect(jsonPath("$.content[0].open_time").value("09:00"))
                                .andExpect(jsonPath("$.content[0].close_time").value("20:00"));
        }

        @Test
        public void testVenueListWhenNoPageParam() throws Exception {
                Pageable venue_pageable = PageRequest.of(0, 5, Sort.by("venueID").ascending());
                Page<Venue> page = new PageImpl<>(Collections.singletonList(
                                new Venue(1, "venue_name", "description", 1, "picture", "address", "09:00", "20:00")),
                                venue_pageable,
                                1);
                when(venueService.findAll(venue_pageable))
                                .thenReturn(page);
                mockMvc.perform(get("/venuelist/getVenueList"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$.content[0].venueID").value(1))
                                .andExpect(jsonPath("$.content[0].venueName").value("venue_name"))
                                .andExpect(jsonPath("$.content[0].description").value("description"))
                                .andExpect(jsonPath("$.content[0].price").value(1))
                                .andExpect(jsonPath("$.content[0].picture").value("picture"))
                                .andExpect(jsonPath("$.content[0].address").value("address"))
                                .andExpect(jsonPath("$.content[0].open_time").value("09:00"))
                                .andExpect(jsonPath("$.content[0].close_time").value("20:00"));
        }

        @Test
        public void testVenueListWhenPageIsNeg() throws Exception {
                mockMvc.perform(get("/venuelist/getVenueList?page=-1"))
                                .andExpect(status().isBadRequest());
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

        @ParameterizedTest
        @ValueSource(strings = { "1.5", "abc" })
        public void testVenueListWhenParamIsFloat(String param) throws Exception {
                mockMvc.perform(get("/venuelist/getVenueList?page=" + param))
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
