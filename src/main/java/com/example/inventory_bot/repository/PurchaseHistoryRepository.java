package com.example.inventory_bot.repository;

import com.example.inventory_bot.entity.Item;
import com.example.inventory_bot.entity.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
}
