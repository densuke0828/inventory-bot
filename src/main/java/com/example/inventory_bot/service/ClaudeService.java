package com.example.inventory_bot.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.example.inventory_bot.entity.Item;
import com.example.inventory_bot.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
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
    private AnthropicClient client;

    @PostConstruct
    public void init() {
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public String ask(String userMessage) {
        String inventoryContext = buildInventoryContext();
        Message response = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(512)
                        .system("""
                                あなたは在庫管理アシスタントです。
                                以下が現在の在庫状況です：
                                %s
                                
                                【重要なルール】
                                ・在庫の追加や削除、更新などの操作は絶対に行わないでください
                                ・在庫状況についての質問にのみ日本語で簡潔に答えてください
                                ・操作を求められた場合は「登録・削除・買った・使ったなどのコマンドを使ってください」と案内してください
                                """.formatted(inventoryContext))
                        .addUserMessage(userMessage)
                        .build()
        );
        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .findFirst()
                .orElse("返答を取得できませんでした");
    }

    /**
     * DBから全在庫を情報を取得し、String型に成形
     */
    private String buildInventoryContext() {
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(i -> i.getName() + " : " + i.getCurrentStock()
                        + (i.getUnit() != null ? i.getUnit() : "")
                        + "(最低在庫: " + i.getMinStock() + ")"
                        +(i.getLastPurchasedAt() != null ? i.getLastPurchasedAt().toLocalDate() : ""))
                .collect(Collectors.joining("\n"));
    }
}
