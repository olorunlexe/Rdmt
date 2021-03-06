package com.interswitch.redemptionapi.Service.Impl;

import com.interswitch.redemptionapi.Domain.GiftResult;
import com.interswitch.redemptionapi.Domain.Redemption;
import com.interswitch.redemptionapi.Service.IGiftRedemptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class GiftRedemptionService implements IGiftRedemptionService {
    private static final Logger log = LoggerFactory.getLogger(GiftRedemptionService.class);
    private RabbitTemplate rabbitTemplate;
    private RedemptionService redemptionService;

    public GiftRedemptionService(RabbitTemplate rabbitTemplate, RedemptionService redemptionService) {
        this.rabbitTemplate = rabbitTemplate;
        this.redemptionService = redemptionService;
    }

    @Override
    public void redeemGiftVoucher(GiftResult receiveGift, Redemption redemption) {
        // Use Amount and GiftBalance to make necessary deductions and
        // the Insert into Redemption table or Update the existing value if still valid

        redemption.setGiftBalanceBeforeRedemption(receiveGift.getGiftBalance());
        redemption.setVoucherType(receiveGift.getVoucherType());
        redemption.setMerchantId(receiveGift.getMerchantId());
        redemptionService.createRedemption(redemption);
        log.info("Creating redemption" + redemption.toString());

        long newBalanceForVoucherUpdate = receiveGift.getGiftBalance() - redemption.getGiftAmountRedeemed();
        receiveGift.setGiftBalance(newBalanceForVoucherUpdate);
        rabbitTemplate.convertAndSend("gift-exchange", "gift-two", receiveGift);
        log.info("Redeemed ValueObject published" + redemption.toString());
    }
}
