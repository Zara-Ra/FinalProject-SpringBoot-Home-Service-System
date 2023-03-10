package ir.maktab.finalproject.controller;

import ir.maktab.finalproject.data.dto.ExpertOfferDto;
import ir.maktab.finalproject.data.entity.ExpertOffer;
import ir.maktab.finalproject.data.mapper.OfferMapper;
import ir.maktab.finalproject.service.impl.ExpertOfferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@Slf4j
@RequestMapping("/offer")
@Validated
public class ExpertOfferController {
    private final ExpertOfferService expertOfferService;

    public ExpertOfferController(ExpertOfferService expertOfferService) {
        this.expertOfferService = expertOfferService;
    }

    @PostMapping("/submit-offer")
    @PreAuthorize("hasRole('EXPERT')")
    public String submitOffer(@Valid @RequestBody ExpertOfferDto expertOfferDto, Principal principal) {
        log.info("*** Submit Offer: {} ***", expertOfferDto);
        expertOfferDto.setExpertEmail(principal.getName());
        ExpertOffer expertOffer = expertOfferService.submitOffer(expertOfferDto.getOrderId()
                , OfferMapper.INSTANCE.convertOffer(expertOfferDto));
        log.info("*** Offer Submitted: {} ***", expertOffer);
        return "Offer Has Been Submitted";
    }

    @GetMapping("/chose-offer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String choseOffer(@RequestParam @Min(1) Integer orderId, @RequestParam @Min(1) Integer offerId) {
        log.info("*** Chose Offer {} For Order {} ***", offerId, orderId);
        expertOfferService.choseOffer(orderId, offerId);
        log.info("*** Offer {} Chosen For Order {} ***", offerId, orderId);
        return "Offer Has Been Chosen";
    }

    @GetMapping("/expert-arrived")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String expertArrived(@RequestParam @Min(1) Integer orderId, @RequestParam @Min(1) Integer offerId) {
        log.info("*** Set Expert Status ARRIVED, Order {}, Offer {} ***", orderId, offerId);
        expertOfferService.expertArrived(orderId, offerId);
        log.info("*** Expert ARRIVED, Order {}, Offer {} ***", orderId, offerId);
        return "Expert Arrived";
    }

    @GetMapping("/expert-done")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String expertDone(@RequestParam @Min(1) Integer orderId, @RequestParam @Min(1) Integer offerId) {
        log.info("*** Set Expert Done, Order {}, Offer {} ***", orderId, offerId);
        expertOfferService.expertDone(orderId, offerId);
        log.info("*** Expert Done, Order {}, Offer {} ***", orderId, offerId);
        return "Expert Done";
    }

}
