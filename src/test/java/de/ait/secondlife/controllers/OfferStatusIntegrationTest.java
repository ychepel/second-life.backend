package de.ait.secondlife.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.*;
import de.ait.secondlife.repositories.*;
import de.ait.secondlife.scheduler.AuctionFinisher;
import de.ait.secondlife.services.interfaces.StatusService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Offer lifecycle tests:")
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class OfferStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private OfferStatusHistoryRepository offerStatusHistoryRepository;

    @Autowired
    private StatusService statusService;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private RejectionReasonRepository rejectionReasonRepository;

    private Long user1Id;
    private Long user2Id;
    private Cookie user1Cookie;
    private Cookie user2Cookie;
    private Cookie adminCookie;
    private Long user1OfferId;
    private Long rejectionReasonId;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void createUsers() throws Exception {
        MvcResult registerResult1 = mockMvc.perform(post("/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "firstName": "TestUser1FirstName",
                                      "lastName": "TestUser1LastName",
                                      "email": "test.user1@test.com",
                                      "password": "qwerty!123"
                                    }"""))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResponse = registerResult1.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        this.user1Id = jsonNode.get("id").asLong();

        MvcResult loginResult1 = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "email": "test.user1@test.com",
                                      "password": "qwerty!123"
                                    }"""))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken1 = loginResult1.getResponse().getCookie("Access-Token").getValue();
        this.user1Cookie = new Cookie("Access-Token", accessToken1);

        MvcResult registerResult2 = mockMvc.perform(post("/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "firstName": "TestUser2FirstName",
                                      "lastName": "TestUser2LastName",
                                      "email": "test.user2@test.com",
                                      "password": "qwerty!123"
                                    }"""))
                .andExpect(status().isCreated())
                .andReturn();
        jsonResponse = registerResult2.getResponse().getContentAsString();
        jsonNode = objectMapper.readTree(jsonResponse);
        this.user2Id = jsonNode.get("id").asLong();

        MvcResult loginResult2 = mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "email": "test.user2@test.com",
                                      "password": "qwerty!123"
                                    }"""))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken2 = loginResult2.getResponse().getCookie("Access-Token").getValue();
        this.user2Cookie = new Cookie("Access-Token", accessToken2);

        MvcResult loginResult3 = mockMvc.perform(post("/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "email": "admin@email.com",
                                      "password": "Security!234"
                                    }"""))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken3 = loginResult3.getResponse().getCookie("Access-Token").getValue();
        this.adminCookie = new Cookie("Access-Token", accessToken3);

        MvcResult creatingResult = mockMvc.perform(post("/v1/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "title": "Test title",
                                      "description": "Test description",
                                      "auctionDurationDays": 3,
                                      "startPrice": 100,
                                      "winBid": 200,
                                      "isFree": false,
                                      "categoryId": 2,
                                      "locationId": 1,
                                      "sendToVerification": false
                                    }""").cookie(user1Cookie))
                .andExpect(status().isCreated())
                .andReturn();
        jsonResponse = creatingResult.getResponse().getContentAsString();
        jsonNode = objectMapper.readTree(jsonResponse);
        this.user1OfferId = jsonNode.get("id").asLong();

        RejectionReason rejectionReason = RejectionReason.builder()
                .name("test rejection reason")
                .build();
        rejectionReasonRepository.save(rejectionReason);
        this.rejectionReasonId = rejectionReason.getId();
    }

    @Nested
    @DisplayName("POST /v1/offers")
    @Transactional
    @Rollback
    public class CreateOffer {

        @Test
        public void create_offer_in_draft_status() throws Exception {
            MvcResult creatingResult = mockMvc.perform(post("/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Test title",
                                      "description": "Test description",
                                      "auctionDurationDays": 3,
                                      "startPrice": 100,
                                      "winBid": 200,
                                      "isFree": false,
                                      "categoryId": 2,
                                      "locationId": 1,
                                      "sendToVerification": false
                                    }""").cookie(user1Cookie))
                    .andExpect(status().isCreated())
                    .andReturn();

            String jsonResponse = creatingResult.getResponse().getContentAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            Long offerId = jsonNode.get("id").asLong();

            Offer offerFromDB = offerRepository.findById(offerId).get();
            assertEquals("Test title", offerFromDB.getTitle());
            assertEquals(OfferStatus.DRAFT, offerFromDB.getOfferStatus());

            Optional<OfferStatusHistory> firstHistory = offerStatusHistoryRepository.findByOfferId(offerId)
                    .stream()
                    .findFirst();
            assertEquals(OfferStatus.DRAFT, firstHistory.get().getOfferStatus());
        }

        @Test
        public void create_offer_in_verification_status() throws Exception {
            MvcResult creatingResult = mockMvc.perform(post("/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Test title",
                                      "description": "Test description",
                                      "auctionDurationDays": 3,
                                      "startPrice": 100,
                                      "winBid": 200,
                                      "isFree": false,
                                      "categoryId": 2,
                                      "locationId": 1,
                                      "sendToVerification": true
                                    }""").cookie(user1Cookie))
                    .andExpect(status().isCreated())
                    .andReturn();

            String jsonResponse = creatingResult.getResponse().getContentAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            Long offerId = jsonNode.get("id").asLong();

            Offer offerFromDB = offerRepository.findById(offerId).get();
            assertEquals("Test title", offerFromDB.getTitle());
            assertEquals(OfferStatus.VERIFICATION, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.VERIFICATION, getLastHistoryStatus(offerId));
        }
    }

    @Nested
    @DisplayName("PUT /v1/offers")
    @Transactional
    @Rollback
    public class UpdateOffer {

        @Test
        public void update_offer_from_draft_to_draft_by_owner() throws Exception {
            mockMvc.perform(put("/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"sendToVerification\": false\n" +
                                    "}").cookie(user1Cookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.DRAFT, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.DRAFT, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void return_403_on_updating_offer_from_draft_to_verification_by_not_owner() throws Exception {
            mockMvc.perform(put("/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"sendToVerification\": true\n" +
                                    "}").cookie(user2Cookie))
                    .andExpect(status().isForbidden());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.DRAFT, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_403_on_updating_offer_from_draft_to_verification_by_admin() throws Exception {
            mockMvc.perform(put("/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"sendToVerification\": true\n" +
                                    "}").cookie(adminCookie))
                    .andExpect(status().isUnauthorized());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.DRAFT, offerFromDB.getOfferStatus());
        }

        @Test
        public void update_offer_from_draft_to_verification_by_owner() throws Exception {
            mockMvc.perform(put("/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"sendToVerification\": true\n" +
                                    "}").cookie(user1Cookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.VERIFICATION, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.VERIFICATION, getLastHistoryStatus(user1OfferId));
        }
    }

    @Nested
    @DisplayName("PATCH /v1/offers/{id}/reject")
    @Transactional
    @Rollback
    public class DraftOffer {

        @Test
        public void reject_offer_from_verification_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"rejectionReasonId\": \"" + rejectionReasonId + "\"\n" +
                                    "}").cookie(adminCookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.REJECTED, offerFromDB.getOfferStatus());

            List<OfferStatusHistory> statusHistory = offerStatusHistoryRepository.findByOfferId(user1OfferId);
            Optional<OfferStatusHistory> lastHistory = statusHistory.stream()
                    .skip(statusHistory.size() - 1)
                    .findFirst();
            assertEquals(OfferStatus.REJECTED, lastHistory.get().getOfferStatus());
            assertEquals(rejectionReasonId, lastHistory.get().getRejection().getId());
        }

        @Test
        public void return_401_on_reject_offer_from_verification_status_by_owner() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"rejectionReasonId\": \"" + rejectionReasonId + "\"\n" +
                                    "}").cookie(user1Cookie))
                    .andExpect(status().isForbidden());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.VERIFICATION, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_reject_offer_from_auction_started_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.AUCTION_STARTED));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"rejectionReasonId\": \"" + rejectionReasonId + "\"\n" +
                                    "}").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.AUCTION_STARTED, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_reject_offer_from_auction_finished_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.AUCTION_FINISHED));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"rejectionReasonId\": \"" + rejectionReasonId + "\"\n" +
                                    "}").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.AUCTION_FINISHED, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_reject_offer_from_qualification_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.QUALIFICATION));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"rejectionReasonId\": \"" + rejectionReasonId + "\"\n" +
                                    "}").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.QUALIFICATION, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_reject_offer_from_completed_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.COMPLETED));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"rejectionReasonId\": \"" + rejectionReasonId + "\"\n" +
                                    "}").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.COMPLETED, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_reject_offer_from_blocked_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.BLOCKED_BY_ADMIN));
            offer.setIsActive(false);

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "\"id\": \"" + user1OfferId + "\",\n" +
                                    "\"rejectionReasonId\": \"" + rejectionReasonId + "\"\n" +
                                    "}").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.BLOCKED_BY_ADMIN, offerFromDB.getOfferStatus());
        }
    }

    @Nested
    @DisplayName("PATCH /v1/offers/{id}/start-auction")
    @Transactional
    @Rollback
    public class StartAuction {

        @Test
        public void start_auction_from_verification_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));
            assertNull(offer.getAuctionFinishedAt());

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.AUCTION_STARTED, offerFromDB.getOfferStatus());
            assertNotNull(offerFromDB.getAuctionFinishedAt());
            assertEquals(OfferStatus.AUCTION_STARTED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void return_500_on_start_auction_from_draft_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.DRAFT));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.DRAFT, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_start_auction_from_rejected_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.REJECTED));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.REJECTED, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_start_auction_from_qualification_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.QUALIFICATION));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.QUALIFICATION, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_start_auction_from_completed_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.COMPLETED));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.COMPLETED, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_start_auction_from_blocked_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.BLOCKED_BY_ADMIN));
            offer.setIsActive(false);

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.BLOCKED_BY_ADMIN, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_500_on_start_auction_from_canceled_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.CANCELED));
            offer.setIsActive(false);

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.CANCELED, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_403_on_start_auction_from_verification_status_by_owner() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(user1Cookie))
                    .andExpect(status().isForbidden());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.VERIFICATION, offerFromDB.getOfferStatus());
        }
    }

    @Nested
    @DisplayName("Scheduled task")
    @Transactional
    @Rollback
    public class AuctionFinish {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AuctionFinisher auctionFinisher;

        @PersistenceContext
        private EntityManager entityManager;

        @Test
        public void finish_auction_without_bids_by_scheduler() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));
            assertNull(offer.getAuctionFinishedAt());

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.AUCTION_STARTED, offerFromDB.getOfferStatus());
            offerFromDB.setAuctionFinishedAt(LocalDateTime.now().minusMinutes(5));

            auctionFinisher.finishAuction();
            offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.COMPLETED, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.COMPLETED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void not_finish_actual_auction_by_scheduler() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));
            assertNull(offer.getAuctionFinishedAt());

            mockMvc.perform(patch("/v1/offers/" + user1OfferId + "/start-auction").cookie(adminCookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.AUCTION_STARTED, offerFromDB.getOfferStatus());

            auctionFinisher.finishAuction();
            offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.AUCTION_STARTED, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.AUCTION_STARTED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void finish_auction_with_one_bid_by_scheduler() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.AUCTION_STARTED));
            offer.setAuctionFinishedAt(LocalDateTime.now().minusMinutes(5));
            offerRepository.saveAndFlush(offer);
            entityManager.refresh(offer);

            BigDecimal bidValue = BigDecimal.valueOf(110);
            User user2 = userRepository.findById(user2Id).get();
            Bid bid = Bid.builder()
                    .offer(offer)
                    .user(user2)
                    .bidValue(bidValue)
                    .createdAt(LocalDateTime.now())
                    .build();
            bidRepository.saveAndFlush(bid);

            auctionFinisher.finishAuction();

            assertEquals(OfferStatus.COMPLETED, offer.getOfferStatus());
            assertEquals(bidValue, offer.getWinnerBid().getBidValue());
            assertEquals(OfferStatus.COMPLETED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void finish_auction_with_many_bids_by_scheduler() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.AUCTION_STARTED));
            offer.setAuctionFinishedAt(LocalDateTime.now().minusMinutes(5));
            offerRepository.saveAndFlush(offer);
            entityManager.refresh(offer);

            User user2 = userRepository.findById(user2Id).get();
            Bid bid1 = Bid.builder()
                    .offer(offer)
                    .user(user2)
                    .bidValue(BigDecimal.valueOf(110))
                    .createdAt(LocalDateTime.now())
                    .build();
            bidRepository.saveAndFlush(bid1);

            Bid bid2 = Bid.builder()
                    .offer(offer)
                    .user(user2)
                    .bidValue(BigDecimal.valueOf(120))
                    .createdAt(LocalDateTime.now())
                    .build();
            bidRepository.saveAndFlush(bid2);

            auctionFinisher.finishAuction();

            assertEquals(OfferStatus.QUALIFICATION, offer.getOfferStatus());
            assertEquals(OfferStatus.QUALIFICATION, getLastHistoryStatus(user1OfferId));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/offers/{id}/block-by-admin")
    @Transactional
    @Rollback
    public class BlockOffer {

        @Test
        public void return_500_on_blocking_offer_from_draft_status_by_admin() throws Exception {
            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/block-by-admin").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.DRAFT, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.DRAFT, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void return_500_on_blocking_offer_from_verification_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/block-by-admin").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.VERIFICATION, offerFromDB.getOfferStatus());
        }

        @Test
        public void block_offer_from_auction_started_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.AUCTION_STARTED));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/block-by-admin").cookie(adminCookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.BLOCKED_BY_ADMIN, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.BLOCKED_BY_ADMIN, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void block_offer_from_qualification_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.QUALIFICATION));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/block-by-admin").cookie(adminCookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.BLOCKED_BY_ADMIN, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.BLOCKED_BY_ADMIN, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void return_500_on_blocking_offer_from_complete_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.COMPLETED));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/block-by-admin").cookie(adminCookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.COMPLETED, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_403_on_blocking_offer_from_verification_status_by_user() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/block-by-admin").cookie(user1Cookie))
                    .andExpect(status().isForbidden());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.VERIFICATION, offerFromDB.getOfferStatus());
        }
    }

    @Nested
    @DisplayName("DELETE /v1/offers/{id}/cancel")
    @Transactional
    @Rollback
    public class CancelOffer {

        @Test
        public void cancel_offer_from_draft_status_by_owner() throws Exception {
            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/cancel").cookie(user1Cookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.CANCELED, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.CANCELED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void cancel_offer_from_rejected_status_by_owner() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.REJECTED));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/cancel").cookie(user1Cookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.CANCELED, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.CANCELED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void cancel_offer_from_verification_status_by_owner() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/cancel").cookie(user1Cookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.CANCELED, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.CANCELED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void cancel_offer_from_auction_started_status_by_owner() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.AUCTION_STARTED));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/cancel").cookie(user1Cookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.CANCELED, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.CANCELED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void cancel_offer_from_qualification_status_by_owner() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.QUALIFICATION));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/cancel").cookie(user1Cookie))
                    .andExpect(status().isOk());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.CANCELED, offerFromDB.getOfferStatus());
            assertEquals(OfferStatus.CANCELED, getLastHistoryStatus(user1OfferId));
        }

        @Test
        public void return_500_on_canceling_offer_from_complete_status_by_owner() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.COMPLETED));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/cancel").cookie(user1Cookie))
                    .andExpect(status().isInternalServerError());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.COMPLETED, offerFromDB.getOfferStatus());
        }

        @Test
        public void return_403_on_canceling_offer_from_verification_status_by_admin() throws Exception {
            Offer offer = offerRepository.findById(user1OfferId).get();
            offer.setStatus(statusService.getByOfferStatus(OfferStatus.VERIFICATION));

            mockMvc.perform(delete("/v1/offers/" + user1OfferId + "/cancel").cookie(adminCookie))
                    .andExpect(status().isForbidden());

            Offer offerFromDB = offerRepository.findById(user1OfferId).get();
            assertEquals(OfferStatus.VERIFICATION, offerFromDB.getOfferStatus());
        }
    }

    private OfferStatus getLastHistoryStatus(Long id) {
        List<OfferStatusHistory> statusHistory = offerStatusHistoryRepository.findByOfferId(id);
        Optional<OfferStatusHistory> lastHistory = statusHistory.stream()
                .skip(statusHistory.size() - 1)
                .findFirst();
        return lastHistory.get().getOfferStatus();
    }

}