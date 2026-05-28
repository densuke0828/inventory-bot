package com.example.inventory_bot.controller;

import com.example.inventory_bot.service.InventoryService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import lombok.RequiredArgsConstructor;

import java.util.List;

@LineMessageHandler
@RequiredArgsConstructor
public class LineWebhookController {
    private final MessagingApiClient messagingApiClient;
    private final InventoryService inventoryService;

    @EventMapping
    public void handleTextMessage(MessageEvent event) {
        if (event.message() instanceof TextMessageContent textMessage) {
            String reply = inventoryService.processMessage(textMessage.text());
            messagingApiClient.replyMessage(new ReplyMessageRequest(
                    event.replyToken(),
                    List.of(new TextMessage(reply)),
                    false
            ));
        }
    }

    @EventMapping
    public void handleDefaultEvent(Event event) {

    }
}
