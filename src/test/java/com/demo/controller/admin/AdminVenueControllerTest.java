package com.demo.controller.admin;

import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.service.VenueService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;

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
    private MockHttpServletRequest request;
    private User user = new User(1, "userID", "userName", "userPassword", "user@example.com", "1234567", 0, "userPic");
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10, Sort.by("venueID").ascending());

    @BeforeEach
    public void setUp() {
        User admin = new User(1, "admin", "adminName", "adminPassword", "admin@example.com", "12345678", 1,
                "adminPic");
        request = new MockHttpServletRequest();
        Objects.requireNonNull(request.getSession()).setAttribute("admin", admin);
    }

    private Venue createTestVenue() {
        return new Venue(1, "venue", "this is description", 100, "", "address", "08:00", CLOSE_TIME);
    }

    @Test
    public void testVenueManagePageWhenVenueListNotEmpty() throws Exception {
        List<Venue> venues = List.of(createTestVenue());
        when(venueService.findAll(DEFAULT_PAGEABLE)).thenReturn(new PageImpl<>(venues, DEFAULT_PAGEABLE, 1));
        mockMvc.perform(get("admin/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"));
        verify(venueService).findAll(DEFAULT_PAGEABLE);
    }

    @Test
    public void testVenueManagePageWhenUserIsNotAdmin() throws Exception {
        List<Venue> venues = List.of(createTestVenue());
        when(venueService.findAll(DEFAULT_PAGEABLE)).thenReturn(new PageImpl<>(venues, DEFAULT_PAGEABLE, 1));
        mockMvc.perform(get("/venue_manage").session((MockHttpSession) request.getSession()))
                .andExpect(status().isUnauthorized());
        verify(venueService).findAll(DEFAULT_PAGEABLE);
    }

    @Test
    public void testVenueManagePageWhenVenueListEmpty() throws Exception {
        when(venueService.findAll(DEFAULT_PAGEABLE))
                .thenReturn(new PageImpl<>(Collections.emptyList(), DEFAULT_PAGEABLE, 0));
        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue_manage"))
                .andExpect(model().attribute("total", 0));
        verify(venueService).findAll(DEFAULT_PAGEABLE);
    }

    @Test
    public void testVenueEditPageWhenVenueExists() throws Exception {
        Venue venue = createTestVenue();
        when(venueService.findByVenueID(1)).thenReturn(venue);
        mockMvc.perform(get("/venue_edit").param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"));
        verify(venueService).findByVenueID(1);
    }

    @Test
    public void testVenueEditPageWhenUserIsNotAdmin() throws Exception {
        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        Venue venue = createTestVenue();
        when(venueService.findByVenueID(1)).thenReturn(venue);
        mockMvc.perform(get("/venue_edit?venueID=1").session((MockHttpSession) request.getSession()))
                .andExpect(status().isUnauthorized());
        verify(venueService).findByVenueID(1);
    }

    @Test
    public void testVenueEditPageWhenVenueNotFound() throws Exception {
        when(venueService.findByVenueID(404)).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(get("/venue_edit?venueID=404"))
                .andExpect(status().isNotFound());
        verify(venueService).findByVenueID(404);
    }

    @Test
    public void testVenueEditPageWhenNoVenueIDParam() throws Exception {
        mockMvc.perform(get("/venue_edit"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testVenueEditPageWhenVenueIDIsNeg() throws Exception {
        mockMvc.perform(get("/venue_edit?venueID=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testVenueEditPageWhenVenueIDIsFloat() throws Exception {
        mockMvc.perform(get("/venue_edit?venueID=1.5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testVenueAddPageAsAdmin() throws Exception {
        mockMvc.perform(get("/venue_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_add"));
    }

    @Test
    public void testVenueAddPageWhenUserIsNotAdmin() throws Exception {
        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        mockMvc.perform(get("/venue_add").session((MockHttpSession) request.getSession()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testVenueListPageWhenParamProvided() throws Exception {
        Venue venue = createTestVenue();
        List<Venue> venues = new ArrayList<>();
        venues.add(venue);
        when(venueService.findAll(DEFAULT_PAGEABLE)).thenReturn(new PageImpl<>(venues, DEFAULT_PAGEABLE, 1));
        mockMvc.perform(get("/venueList.do").param("page", "1"))
                .andExpect(status().isOk());
        verify(venueService).findAll(DEFAULT_PAGEABLE);
    }

    @Test
    public void testVenueListPageWhenUserIsNotAdmin() throws Exception {

        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        Page<Venue> page = new PageImpl<>(Collections.singletonList(
                new Venue(1, "venue_name", "description", 1, "picture", "address", OPEN_TIME,
                        CLOSE_TIME)),
                DEFAULT_PAGEABLE, 1);
        when(venueService.findAll(DEFAULT_PAGEABLE))
                .thenReturn(page);
        mockMvc.perform(get("/venueList.do?page=1").session((MockHttpSession) request.getSession()))
                .andExpect(status().isUnauthorized());
        verify(venueService).findAll(DEFAULT_PAGEABLE);
    }

    @Test
    public void testVenueListPageWhenNoParam() throws Exception {
        Page<Venue> page = new PageImpl<>(Collections.singletonList(
                new Venue(1, "venue_name", "description", 1, "picture", "address", OPEN_TIME,
                        CLOSE_TIME)),
                DEFAULT_PAGEABLE, 1);
        when(venueService.findAll(DEFAULT_PAGEABLE))
                .thenReturn(page);
        mockMvc.perform(get("/venueList.do"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"venueID\":1,\"venueName\":\"venue_name\",\"description\":\"description\",\"price\":1,\"picture\":\"picture\",\"address\":\"address\",\"open_time\":\""
                                + OPEN_TIME + "\",\"close_time\":\"" + CLOSE_TIME + "\"}]"));
        verify(venueService).findAll(DEFAULT_PAGEABLE);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.5", "abc", "-1" })
    public void testVenueListPageWhenPageParamIsInvalid(String param) throws Exception {
        mockMvc.perform(get("/venueList.do?page=" + param))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddVenueSuccessWithEmptyPicture() throws Exception {
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
    public void testAddVenueSuccessWithValidPicture() throws Exception {
        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        Path imagePath = Paths.get("./src/main/resources/static/venue.jpg");
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MockMultipartFile file = new MockMultipartFile("picture", "test.jpg", "image/jpg", imageBytes);
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
    public void testAddVenueFailureWithEmptyPicture() throws Exception {
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
    public void testAddVenueFailureWithValidPicture() throws Exception {
        Path imagePath = Paths.get("./src/main/resources/static/venue.jpg");
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MockMultipartFile file = new MockMultipartFile("picture", "test.jpg", "image/jpg", imageBytes);
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
    public void testAddVenueWhenUserIsNotAdmin() throws Exception {
        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        Path imagePath = Paths.get("./src/main/resources/static/venue.jpg");
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MockMultipartFile imageFile = new MockMultipartFile("picture", "test.jpg", "image/jpg", imageBytes);
        when(venueService.create(any(Venue.class)))
                .thenReturn(1);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                .file(imageFile)
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME)
                .session((MockHttpSession) request.getSession()))
                .andExpect(status().isUnauthorized());
        verify(venueService).create(any(Venue.class));
    }

    @Test
    public void testAddVenueWhenPriceOverflow() throws Exception {
        Venue venue = new Venue(0, "venue_name", "description", 1, "", "address", OPEN_TIME, CLOSE_TIME);
        when(venueService.create(venue))
                .thenReturn(1);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                .file(new MockMultipartFile("picture", "", "image/jpg", new byte[0]))
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price",
                        "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME))
                .andExpect(status().isBadRequest());
        verify(venueService).create(venue);
    }

    @Test
    public void testAddVenueWhenNoParams() throws Exception {
        mockMvc.perform(post("/addVenue.do"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddVenueWhenPriceIsFloat() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                .file(new MockMultipartFile("picture", "", "image/jpg", new byte[0]))
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1.5")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddVenueWhenPriceIsNeg() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                .file(new MockMultipartFile("picture", "", "image/jpg", new byte[0]))
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "-1")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddVenueWhenTimeInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                .file(new MockMultipartFile("picture", "", "image/jpg", new byte[0]))
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1")
                .param("open_time", "open_time")
                .param("close_time", "close_time"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddVenueFailureWithDuplicateName() throws Exception {
        when(venueService.countVenueName("conflict"))
                .thenReturn(1);
        when(venueService.findByVenueName("conflict"))
                .thenReturn(new Venue(1, "conflict", "descriptions", 1,
                        "", "addresses", OPEN_TIME, CLOSE_TIME));
        when(venueService.create(any(Venue.class)))
                .thenReturn(1);
        Path imagePath = Paths.get("./src/main/resources/static/venue.jpg");
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MockMultipartFile imageFile = new MockMultipartFile("picture", "test.jpg", "image/jpg", imageBytes);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/addVenue.do")
                .file(imageFile)
                .param("venueName", "conflict")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME);
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_add"))
                .andExpect(model().attribute("message", "添加失败！"));
    }

    @Test
    public void testModifyVenueSuccessWithEmptyPicture() throws Exception {
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
    public void testModifyVenueSuccessWithValidPicture() throws Exception {
        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        Path imagePath = Paths.get("./src/main/resources/static/venue.jpg");
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MockMultipartFile file = new MockMultipartFile("picture", "test.jpg", "image/jpg", imageBytes);
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
    public void testModifyVenueWhenUserIsNotAdmin() throws Exception {
        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        Path imagePath = Paths.get("./src/main/resources/static/venue.jpg");
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MockMultipartFile imageFile = new MockMultipartFile("picture", "test.jpg", "image/jpg", imageBytes);
        when(venueService.findByVenueID(1))
                .thenReturn(createTestVenue());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/modifyVenue.do")
                .file(imageFile)
                .param("venueID", "1")
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME)
                .session((MockHttpSession) request.getSession()))
                .andExpect(status().isUnauthorized());
        verify(venueService).findByVenueID(1);
        verify(venueService).update(any(Venue.class));
    }

    @Test
    public void testModifyVenueWhenVenueNotFound() throws Exception {
        Path imagePath = Paths.get("./src/main/resources/static/venue.jpg");
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MockMultipartFile imageFile = new MockMultipartFile("picture", "test.jpg", "image/jpg", imageBytes);
        when(venueService.findByVenueID(1))
                .thenThrow(EntityNotFoundException.class);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/modifyVenue.do")
                .file(imageFile)
                .param("venueID", "1")
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME))
                .andExpect(status().isNotFound());
        verify(venueService).findByVenueID(1);
    }

    @Test
    public void testModifyVenueWhenPriceIsFloat() throws Exception {
        mockMvc.perform(post("/modifyVenue.do")
                .param("venueID", "1.5")
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1.5")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testModifyVenueWhenPriceIsNeg() throws Exception {
        mockMvc.perform(post("/modifyVenue.do")
                .param("venueID", "-1")
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "-1")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testModifyVenueWhenTimeInvalid() throws Exception {
        mockMvc.perform(post("/modifyVenue.do")
                .param("venueID", "1")
                .param("venueName", "venue_name")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1")
                .param("open_time", "open_time")
                .param("close_time", "close_time"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testModifyVenueWithDuplicateName() throws Exception {
        when(venueService.findByVenueName("conflict"))
                .thenThrow(NonUniqueResultException.class);
        when(venueService.countVenueName("conflict"))
                .thenReturn(1);
        when(venueService.findByVenueID(1))
                .thenReturn(new Venue(1, "venue_name", "description",
                        1, "pic", "address", OPEN_TIME, CLOSE_TIME));
        Path imagePath = Paths.get("./src/main/resources/static/venue.jpg");
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MockMultipartFile imageFile = new MockMultipartFile("picture", "test.jpg", "image/jpg", imageBytes);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/modifyVenue.do")
                .file(imageFile)
                .param("venueID", "1")
                .param("venueName", "conflict")
                .param("address", "address")
                .param("description", "description")
                .param("price", "1")
                .param("open_time", OPEN_TIME)
                .param("close_time", CLOSE_TIME);
        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testModifyVenueWithNoParam() throws Exception {
        mockMvc.perform(post("/modifyVenue.do"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDelVenue() throws Exception {
        mockMvc.perform(post("/delVenue.do").param("venueID", "1"))
                .andExpect(status().isOk());
        verify(venueService).delById(anyInt());
    }

    @Test
    public void testDelVenueWhenUserIsNotAdmin() throws Exception {
        User user = new User(1, "userID", "userName", "userPassword", "user@example.com", "14695846221", 0, "userPic");
        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        doNothing()
                .when(venueService).delById(1);
        mockMvc.perform(post("/delVenue.do")
                .param("venueID", "1").session((MockHttpSession) request.getSession()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDelVenueWhenParamIsFloat() throws Exception {
        mockMvc.perform(post("/delVenue.do")
                .param("venueID", "1.5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDelVenueWhenParamIsNeg() throws Exception {
        doThrow(new EmptyResultDataAccessException(-1))
                .when(venueService).delById(-1);
        mockMvc.perform(post("/delVenue.do")
                .param("venueID", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDelVenueWhenParamNotFound() throws Exception {
        doThrow(EmptyResultDataAccessException.class)
                .when(venueService).delById(1);
        mockMvc.perform(post("/delVenue.do")
                .param("venueID", "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("false"));
    }

    @Test
    public void testDelVenueWithNoParam() throws Exception {
        mockMvc.perform(post("/delVenue.do"))
                .andExpect(status().isBadRequest());
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

    @Test
    public void testCheckVenueNameWhenUserIdNotAdmin() throws Exception {
        User user = new User(1, "userID", "userName", "userPassword", "user@example.com", "14695846221", 0, "userPic");
        Objects.requireNonNull(request.getSession()).setAttribute("admin", user);
        when(venueService.countVenueName("venue_name"))
                .thenReturn(0);
        mockMvc.perform(post("/checkVenueName.do")
                .param("venueName", "venue_name").session((MockHttpSession) request.getSession()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCheckVenueNameWithNoParam() throws Exception {
        mockMvc.perform(post("/checkVenueName.do"))
                .andExpect(status().isBadRequest());
    }
}