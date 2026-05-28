package com.example.inventory_bot.service;

import com.example.inventory_bot.entity.Item;
import com.example.inventory_bot.entity.PurchaseHistory;
import com.example.inventory_bot.repository.ItemRepository;
import com.example.inventory_bot.repository.PurchaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {
    private final ItemRepository itemRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    public String processMessage(String text) {
        if (text.contains("不足") || text.contains("足りない") || text.contains("買うもの")) {
            return getInsufficientItems();
        }
        if (text.contains("一覧") || text.contains("全部") || text.contains("リスト")) {
            return getAllItems();
        }
        if (text.contains("買った") || text.contains("補充")) {
            String itemName = extractItemName(text, "買った", "補充");
            int quantity = extractQuantity(text);
            return purchaseItem(itemName, quantity);
        }
        if (text.contains("使った") || text.contains("消費")) {
            String itemName = extractItemName(text, "使った", "消費");
            int quantity = extractQuantity(text);
            return consumeItem(itemName, quantity);
        }
        if (text.contains(("登録"))) {
            String itemName = extractItemName(text, "登録");
            int quantity = extractQuantity(text);
            return registerItem(itemName, quantity);
        }
        if (text.contains(("削除"))) {
            String itemName = extractItemName(text, "削除");
            return deleteItem(itemName);
        }
        if (text.contains("いくつ") || text.contains("在庫") || text.contains("残り")) {
            String itemName = extractItemName(text, "いくつ", "在庫", "残り");
            return searchByName(itemName);
        }
        return "わかりませんでした。「不足」「〇〇 買った」「登録」などで話しかけてください";
    }

    private String getInsufficientItems() {
        List<Item> items = itemRepository.findInsufficientItems();
        if (items.isEmpty()) {
            return "不足しているものはありません";
        }
        String list = items.stream()
                .map(i -> "・" + i.getName() + "(残り" + i.getCurrentStock() + (i.getUnit() != null ? i.getUnit() : "") + ") ")
                .collect(Collectors.joining("\n"));
        return "不足しているものリスト：\n" + list;
    }

    private String getAllItems() {
        List<Item> items = itemRepository.findAll();
        if (items.isEmpty()) {
            return "在庫登録がありません";
        }
        String list = items.stream()
                .map(i -> "・" + i.getName() + "(残り" + i.getCurrentStock() + (i.getUnit() != null ? i.getUnit() : "") + ") ")
                .collect(Collectors.joining("\n"));
        return "在庫リスト：\n" + list;
    }

    /**
     * 文字列からexcludeWordsとその他の文字列を排除してItemNameを抽出する
     */
    private String extractItemName(String text, String... excludeWords) {
        String result = text;
        for (String excludeWord : excludeWords) {
            result = result
                    .replaceAll(excludeWord, "")
                    .replaceAll("\\d+[^\\s]*", "");
        }
        return result.trim();
    }

    /**
     * 文字列から数字を抽出する
     */
    private int extractQuantity(String text) {
        Matcher result = Pattern.compile("(\\d+)").matcher(text);
        if (result.find()) {
            return Integer.parseInt(result.group());
        }
        return 1;
    }

    /**
     * 在庫に登録されている商品の個数を増やす+購入履歴更新
     */
    private String purchaseItem(String itemName, int quantity) {
        Optional<Item> item = itemRepository.findByName(itemName);
        if (item.isEmpty()) {
            return "在庫が見つかりませんでした";
        }
        Item resultItem = item.get();
        resultItem.setCurrentStock(resultItem.getCurrentStock() + quantity);
        resultItem.setLastPurchasedAt(LocalDateTime.now());
        itemRepository.save(resultItem);

        PurchaseHistory history = new PurchaseHistory();
        history.setItem(resultItem);
        history.setQuantity(quantity);
        history.setPurchasedAt(LocalDateTime.now());
        purchaseHistoryRepository.save(history);

        return "追加しました";
    }

    /**
     * 在庫に登録されている商品の個数を減らす
     */
    private String consumeItem(String itemName, int quantity) {
        Optional<Item> foundItem = itemRepository.findByName(itemName);
        if (foundItem.isEmpty()) {
            return "在庫が見つかりませんでした";
        }
        Item item = foundItem.get();
        if (item.getCurrentStock() < quantity) {
            return "在庫がマイナスになります (現在" + item.getCurrentStock() + "個)";
        }
        item.setCurrentStock(item.getCurrentStock() - quantity);
        itemRepository.save(item);
        return "消費しました";
    }

    /**
     * 在庫リストに新規登録
     */
    private String registerItem(String itemName, int quantity) {
        if (itemRepository.findByName(itemName).isPresent()) {
            return itemName + "はすでに登録されています";
        }
        Item item = new Item();
        item.setName(itemName);
        item.setCurrentStock(quantity);
        itemRepository.save(item);
        return "在庫リストに新規登録しました";
    }

    /**
     * 在庫リストからアイテムを削除
     */
    private String deleteItem(String itemName) {
        Optional<Item> foundItem = itemRepository.findByName(itemName);
        if (foundItem.isEmpty()) {
            return "登録されていないアイテムです。削除できません";
        }
        Item item = foundItem.get();
        itemRepository.delete(item);
        return "在庫リストから削除しました";
    }

    /**
     * 在庫数の確認
     */
    private String searchByName(String itemName) {
        Optional<Item> foundItem = itemRepository.findByName(itemName);
        if (foundItem.isEmpty()) {
            return "登録されていないアイテムです";
        }
        Item item = foundItem.get();
        return item.getName() + " 残り" + item.getCurrentStock() + (item.getUnit() != null ? item.getUnit() : " ");
    }
}
