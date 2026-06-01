package com.example.inventory_bot.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.example.inventory_bot.entity.Item;
import com.example.inventory_bot.repository.ItemRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaudeService {
    @Value("${anthropic.api-key}")
    private String apiKey;
    private final ItemRepository itemRepository;

    public String ask(String userMessage) {
        String inventoryContext = buildInventoryContext();
        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
        Message response = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(512)
                        .system("あなたは在庫管理アシスタントです。\n" +
                                "以下が現在の在庫状況です：\n" + inventoryContext +
                                "\nユーザーの質問に対して日本語で簡潔に答えてください。")
                        .addUserMessage(userMessage)
                        .build()
        );
        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .findFirst()
                .orElse("返答を取得できませんでした");
    }

    private String buildInventoryContext() {
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(i -> i.getName() + " : " + i.getCurrentStock()
                        + (i.getUnit() != null ? i.getUnit() : "")
                        + "(最低在庫: " + i.getMinStock() + ")"
                        +(i.getLastPurchasedAt() != null ? i.getLastPurchasedAt() : ""))
                .collect(Collectors.joining("\n"));
    }
}
