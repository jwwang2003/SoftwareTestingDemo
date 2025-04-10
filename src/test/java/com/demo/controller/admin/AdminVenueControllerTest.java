package com.demo.controller.admin;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminVenueController.class)

class AdminVenueControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @MockBean
        private VenueService venueService;

        private static final String VENUE_NAME = "venue";
        private static final String ADDRESS = "address";
        private static final String DESCRIPTION = "this is description";
        private static final String PRICE = "100";
        private static final String OPEN_TIME = "09:00";
        private static final String CLOSE_TIME = "20:00";

        private Venue createTestVenue() {
                return new Venue(1, "venue", "this is description", 100, "", "address", "08:00", "20:00");
        }

        @Test
        public void testRetVenueManage() throws Exception {
                List<Venue> venues = List.of(createTestVenue());
                Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());
                when(venueService.findAll(pageable)).thenReturn(new PageImpl<>(venues, pageable, 1));
                mockMvc.perform(get("/venue_manage"))
                                .andExpect(status().isOk())
                                .andExpect(model().attributeExists("total"));
                verify(venueService).findAll(pageable);
        }

        @Test
        public void testRetVenueEdit() throws Exception {
                Venue venue = createTestVenue();
                when(venueService.findByVenueID(1)).thenReturn(venue);
                mockMvc.perform(get("/venue_edit").param("venueID", "1"))
                                .andExpect(status().isOk())
                                .andExpect(model().attributeExists("venue"));
                verify(venueService).findByVenueID(1);
        }

        @Test
        public void testRetVenueAdd() throws Exception {
                mockMvc.perform(get("/venue_add"))
                                .andExpect(status().isOk());
        }

        @Test
        public void testRetVenueList() throws Exception {
                Venue venue = createTestVenue();
                List<Venue> venues = new ArrayList<>();
                venues.add(venue);
                Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());
                when(venueService.findAll(pageable)).thenReturn(new PageImpl<>(venues, pageable, 1));
                mockMvc.perform(get("/venueList.do").param("page", "1"))
                                .andExpect(status().isOk());
                verify(venueService).findAll(pageable);
        }

        @Test
        void testSuccessAddVenueWithNullPicture() throws Exception {
                MockMultipartFile file = new MockMultipartFile("picture", "", "image/jpeg", new byte[0]);
                when(venueService.create(any())).thenReturn(1);
                mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                                .file(file)
                                .param("venueName", VENUE_NAME)
                                .param("address", ADDRESS)
                                .param("description", DESCRIPTION)
                                .param("price", PRICE)
                                .param("open_time", OPEN_TIME)
                                .param("close_time", CLOSE_TIME))
                                .andExpect(redirectedUrl("venue_manage"));
                verify(venueService).create(any());
        }

        @Test
        public void testSuccessAddVenueWithNotNullPicture() throws Exception {
                MockMultipartFile file = new MockMultipartFile("picture", "1.bmp",
                                "picture", "1.bmp".getBytes());
                when(venueService.create(any())).thenReturn(1);
                mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                                .file(file)
                                .param("venueName", VENUE_NAME)
                                .param("address", ADDRESS)
                                .param("description", DESCRIPTION)
                                .param("price", PRICE)
                                .param("open_time", OPEN_TIME)
                                .param("close_time", CLOSE_TIME))
                                .andExpect(redirectedUrl("venue_manage"));
                verify(venueService).create(any());
        }

        @Test
        public void testFailAddVenueWithNullPicture() throws Exception {
                MockMultipartFile file = new MockMultipartFile("picture", "",
                                "picture", "".getBytes());
                when(venueService.create(any())).thenReturn(0);
                mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                                .file(file)
                                .param("venueName", VENUE_NAME)
                                .param("address", ADDRESS)
                                .param("description", DESCRIPTION)
                                .param("price", PRICE)
                                .param("open_time", OPEN_TIME)
                                .param("close_time", CLOSE_TIME))
                                .andExpect(redirectedUrl("venue_add"));
                verify(venueService).create(any());
        }

        @Test
        public void testFailAddVenueWithNotNullPicture() throws Exception {
                MockMultipartFile file = new MockMultipartFile("picture", "1.bmp",
                                "picture", "1.bmp".getBytes());
                when(venueService.create(any())).thenReturn(0);
                mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                                .file(file)
                                .param("venueName", VENUE_NAME)
                                .param("address", ADDRESS)
                                .param("description", DESCRIPTION)
                                .param("price", PRICE)
                                .param("open_time", OPEN_TIME)
                                .param("close_time", CLOSE_TIME))
                                .andExpect(redirectedUrl("venue_add"));
                verify(venueService).create(any());
        }

        @Test
        public void testModifyNullPicture() throws Exception {
                when(venueService.findByVenueID(anyInt())).thenReturn(new Venue());
                MockMultipartFile file = new MockMultipartFile("picture", "",
                                "picture", "".getBytes());
                mockMvc.perform(MockMvcRequestBuilders.multipart("/modifyVenue.do")
                                .file(file)
                                .param("venueID", "1")
                                .param("venueName", VENUE_NAME)
                                .param("address", ADDRESS)
                                .param("description", DESCRIPTION)
                                .param("price", PRICE)
                                .param("open_time", OPEN_TIME)
                                .param("close_time", CLOSE_TIME))
                                .andExpect(redirectedUrl("venue_manage"));
                verify(venueService).findByVenueID(anyInt());
                verify(venueService).update(any());
        }

        @Test
        public void testModifyNotNullPicture() throws Exception {
                MockMultipartFile file = new MockMultipartFile("picture", "1.bmp",
                                "picture", "1.bmp".getBytes());
                when(venueService.findByVenueID(anyInt())).thenReturn(new Venue());
                mockMvc.perform(MockMvcRequestBuilders.multipart("/modifyVenue.do")
                                .file(file)
                                .param("venueID", "1")
                                .param("venueName", VENUE_NAME)
                                .param("address", ADDRESS)
                                .param("description", DESCRIPTION)
                                .param("price", PRICE)
                                .param("open_time", OPEN_TIME)
                                .param("close_time", CLOSE_TIME))
                                .andExpect(redirectedUrl("venue_manage"));
                verify(venueService).findByVenueID(anyInt());
                verify(venueService).update(any());
        }

        @Test
        public void testDelVenue() throws Exception {
                mockMvc.perform(post("/delVenue.do").param("venueID", "1"))
                                .andExpect(status().isOk());
                verify(venueService).delById(anyInt());
        }

        @Test
        public void testCheckVenueNameHasSame() throws Exception {
                when(venueService.countVenueName(VENUE_NAME)).thenReturn(1);
                mockMvc.perform(post("/checkVenueName.do").param("venueName", VENUE_NAME))
                                .andExpect(status().isOk())
                                .andExpect(content().string("false"));
                verify(venueService).countVenueName(VENUE_NAME);
        }

        @Test
        public void testCheckVenueNameNoSame() throws Exception {
                when(venueService.countVenueName(VENUE_NAME)).thenReturn(0);
                mockMvc.perform(post("/checkVenueName.do").param("venueName", VENUE_NAME))
                                .andExpect(status().isOk())
                                .andExpect(content().string("true"));
                verify(venueService).countVenueName(VENUE_NAME);
        }
}