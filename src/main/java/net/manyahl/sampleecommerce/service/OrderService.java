package net.manyahl.sampleecommerce.service;

import jakarta.annotation.PostConstruct;
import net.manyahl.sampleecommerce.dto.OrderDTO;
import net.manyahl.sampleecommerce.model.*;
import net.manyahl.sampleecommerce.repository.CustomerRepository;
import net.manyahl.sampleecommerce.repository.OrderRepository;
import net.manyahl.sampleecommerce.repository.ProductRepository;
import net.manyahl.sampleecommerce.storage.LocalStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;

    public ResponseEntity<?> placeOrder(OrderDTO orderDTO) {
        Object productInLocalStorage = LocalStorage.store.get(orderDTO.getProductCode());
        if (productInLocalStorage != null) {
            Object customerInLocalStorage = LocalStorage.store.get(orderDTO.getCustomerEmail());
            if (customerInLocalStorage != null) {
                return ResponseEntity.status(401).body("Already purchased");
            }
            int currentProductQuantity = Integer.parseInt(productInLocalStorage.toString());
            if (currentProductQuantity <= 0) {
                return ResponseEntity.status(404).body("Product out of stock");
            }
            if (orderDTO.getQuantity() > 1) {
                return ResponseEntity.badRequest().body("You can only buy 1");
            }
            return doPlaceOrder(orderDTO);
        }
        return doPlaceOrder(orderDTO);
    }

    private void saveOrder(OrderDTO orderDTO) {
        orderRepository.save(orderDTO.convertToModel());
    }

    private ResponseEntity<?> doPlaceOrder(OrderDTO orderDTO) {
        Product product = productRepository.findById(orderDTO.getProductCode()).orElse(null);
        if (product != null) {
            Customer customer = customerRepository.findById(orderDTO.getCustomerEmail()).orElse(null);
            if (customer != null) {
                saveOrder(orderDTO);
                storeCustomerInRedis(customer);
                updateProduct(product);
                return ResponseEntity.ok("Order created successfully");
            }
            return ResponseEntity.badRequest().body("Customer doesn't exist");
        }
        return ResponseEntity.badRequest().body("Product doesn't exist");
    }

    private void doPlaceOrder(OrderDTO orderDTO, Customer customer, Product product) {
        saveOrder(orderDTO);
        updateProduct(product);
        storeCustomerInRedis(customer);
    }

    private void updateProduct(Product product) {
        int productQuantity = product.getQuantityInStock();
        product.setQuantityInStock(productQuantity - 1);
        productRepository.save(product);
        LocalStorage.store.put(product.getCode(), productQuantity - 1);
    }

    private void storeCustomerInRedis(Customer customer) {
        LocalStorage.store.put(customer.getEmail(), true);
    }

    @PostConstruct
    public void populateRedis() {
        //Orders: filtering might be applied
        List<Orders> orders = orderRepository.findAll();
        for (Orders order : orders) {
            LocalStorage.store.put(order.getCode(), order.getQuantity());
            LocalStorage.store.put(order.getEmail(), true);
        }

        //Products: filtering might be applied
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            LocalStorage.store.put(product.getCode(), product.getQuantityInStock());
        }
    }

}
