package tourGuide.unit.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import tourGuide.config.InternalTestHelper;
import tourGuide.service.TourGuideService;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestTourGuideController {

    @Autowired
    private MockMvc mock;
    @Autowired
    TourGuideService tourGuideService;

    @Test
    public void shouldAccessHomePage() throws Exception {

        mock.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from TourGuide!")));
    }

    @Test
    public void shouldAccessUserLocation() throws Exception {

        InternalTestHelper.setInternalUserNumber(1);
        mock.perform(get("/getLocation").param("userName", "internalUser0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("longitude")))
                .andExpect(content().string(containsString("latitude")));
    }

    @Test
    public void shouldAccessNearbyAttractions() throws Exception {

        InternalTestHelper.setInternalUserNumber(1);
        mock.perform(get("/getNearbyAttractions").param("userName", "internalUser0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(5)));
    }

    @Test
    public void shouldAccessUserRewards() throws Exception {

        InternalTestHelper.setInternalUserNumber(1);
        mock.perform(get("/getRewards").param("userName", "internalUser0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    public void shouldAccessUserRecentLocations() throws Exception {

        InternalTestHelper.setInternalUserNumber(1);
        mock.perform(get("/getAllCurrentLocations").param("userName", "internalUser1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("longitude")))
                .andExpect(content().string(containsString("latitude")));
    }

    @Test
    public void shouldAccessUserTripDeals() throws Exception {

        InternalTestHelper.setInternalUserNumber(1);
        mock.perform(get("/getTripDeals").param("userName", "internalUser0").param("attractionUuid", String.valueOf(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(5)));
    }
}
