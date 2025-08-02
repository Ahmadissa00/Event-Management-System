package com.example.orderservice.service;


import com.bookingservice.event.BookingEvent;
import com.example.orderservice.client.InventoryServiceClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public OrderService(OrderRepository orderRepository,
                        InventoryServiceClient inventoryServiceClient)  {
        this.inventoryServiceClient = inventoryServiceClient;
        this.orderRepository = orderRepository;
    }




    @KafkaListener(topics = "booking", groupId = "order")
    public void orderEvent(BookingEvent bookingEvent) {
        log.info("Received booking event: {}", bookingEvent);

        Order order = createOrder(bookingEvent);
        orderRepository.saveAndFlush(order);

        inventoryServiceClient.updateInventory(bookingEvent.getEventId(), bookingEvent.getTicketCount());
        log.info("Inventory updated for event ID: {}, Ticket Count: {}",
                bookingEvent.getEventId(), bookingEvent.getTicketCount());

    }

    private Order createOrder(BookingEvent bookingEvent) {
        return Order.builder()
                .customerId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }
}
