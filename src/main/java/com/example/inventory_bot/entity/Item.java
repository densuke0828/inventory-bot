package com.example.inventory_bot.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(name = "current_stock", nullable = false)
    private int currentStock = 0;

    @Column(name = "min_stock", nullable = false)
    private int minStock = 1;

    private String unit;

    @Column(name = "last_purchased_at")
    private LocalDateTime lastPurchasedAt;

    @Column(name = "line_user_id")
    private String lineUserId;
}
