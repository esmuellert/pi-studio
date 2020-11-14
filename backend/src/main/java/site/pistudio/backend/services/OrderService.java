package site.pistudio.backend.services;

import org.springframework.stereotype.Service;
import site.pistudio.backend.dao.OrderRepository;
import site.pistudio.backend.entities.Order;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

@Service
public class OrderService {
    final
    OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order order) {
        long orderNumber = generateValidOrderNumber();

        return orderRepository.save(order);
    }

    private long generateValidOrderNumber() {
        int bound = 10000;
        int failure = 0;
        long id;
        do {
            if (failure > 3) {
                bound *= 10;
                failure = 0;
            }
            id = generateOrderNumber(bound);
            if (orderRepository.findByOrderNumber(id) != null) {
                failure++;
            } else {
                failure = 0;
            }
        } while (failure > 0);
        return id;
    }

    private long generateOrderNumber(int bound) {
        LocalDateTime now = LocalDateTime.now();
        String year = String.format("%02d", now.getYear() % 2000);
        String day = String.format("%03d", now.getDayOfYear());
        String hour = String.format("%02d", now.getHour());
        String time = year + day + hour;
        int suffix = new Random(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).nextInt(bound);
        return Long.parseLong(time + String.format("%0" + (Integer.toString(bound).length() - 1) + "d", suffix));
    }

}
