package net.manyahl.sampleecommerce.controller;

import net.manyahl.sampleecommerce.dto.OrderDTO;
import net.manyahl.sampleecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping("/place")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO){
        return orderService.placeOrder(orderDTO);
    }
}
