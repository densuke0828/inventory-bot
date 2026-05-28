package com.example.inventory_bot.repository;

import com.example.inventory_bot.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i FROM Item i WHERE i.currentStock < i.minStock")
    List<Item> findInsufficientItems();

    Optional<Item> findByName(String itemName);
}
