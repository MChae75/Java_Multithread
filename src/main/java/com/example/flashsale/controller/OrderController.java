package com.example.flashsale.controller;

import com.example.flashsale.domain.OrderRequest;
import com.example.flashsale.entity.OrderEntity;
import com.example.flashsale.service.FlashSaleServiceDb;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.flashsale.domain.ProductResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class OrderController {

    private final FlashSaleServiceDb flashSaleService;

    public OrderController(FlashSaleServiceDb flashSaleService) {
        this.flashSaleService = flashSaleService;
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody OrderRequest request) {
        Map<String, Object> response = flashSaleService.placeOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Integer>> inventory() {
        return ResponseEntity.ok(flashSaleService.getInventorySnapshot());
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getProducts() {
        return ResponseEntity.ok(flashSaleService.getAllProducts());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderEntity>> getAllOrders() {
        return ResponseEntity.ok(flashSaleService.getAllOrders());
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        flashSaleService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }
}
