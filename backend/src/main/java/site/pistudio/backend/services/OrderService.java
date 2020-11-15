package site.pistudio.backend.services;

import org.springframework.stereotype.Service;
import site.pistudio.backend.dao.OrderRepository;
import site.pistudio.backend.dao.ScheduleRepository;
import site.pistudio.backend.dto.OrderClientBody;
import site.pistudio.backend.dto.OrderForm;
import site.pistudio.backend.entities.Order;
import site.pistudio.backend.entities.Schedule;
import site.pistudio.backend.exceptions.InvalidTokenException;
import site.pistudio.backend.utils.OrderStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

@Service
public class OrderService {
    final
    OrderRepository orderRepository;

    VerifyTokenService verifyTokenService;

    ScheduleRepository scheduleRepository;

    public OrderService(OrderRepository orderRepository,
                        VerifyTokenService verifyTokenService,
                        ScheduleRepository scheduleRepository) {
        this.orderRepository = orderRepository;
        this.verifyTokenService = verifyTokenService;
        this.scheduleRepository = scheduleRepository;
    }


    public OrderClientBody placeOrder(OrderForm orderForm) {
        Order order = new Order();
        String openId = verifyTokenService.verifyToken(orderForm.getToken());
        long orderNumber = generateValidOrderNumber();

        order.setOrderNumber(orderNumber);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setWechatId(orderForm.getWechatId());
        order.setOpenId(openId);
        order.setPhoneNumber(orderForm.getPhoneNumber());
        order.setType(orderForm.getType());
        order.setNotes(orderForm.getNotes());
        order.setOrderedTime(LocalDateTime.now());
        orderRepository.save(order);

        for (LocalDateTime time : orderForm.getSchedule()) {
            Schedule schedule = new Schedule();
            schedule.setTime(time);
            schedule.setOrder(order);
            scheduleRepository.save(schedule);
        }
        OrderClientBody orderClientBody = OrderClientBody.orderToClientBody(order);
        orderClientBody.setSchedule(orderForm.getSchedule());
        return orderClientBody;
    }

    public OrderClientBody getOrderByOrderNumber(long orderNumber, String token) {
        String openId = verifyTokenService.verifyToken(token);
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order == null) {
            throw new NoSuchElementException("Order: " + orderNumber + " not found!");
        }
        if (!openId.equals(order.getOpenId())) {
            throw new InvalidTokenException();
        }
        List<Schedule> schedules = scheduleRepository.findSchedulesByOrder_OrderNumberOrderByTime(order.getOrderNumber());
        return OrderClientBody.OrderToClientBody(order, schedules);
    }

    public List<OrderClientBody> getOrders(String token) {
        String openId = verifyTokenService.verifyToken(token);
        List<Order> orders = orderRepository.findOrdersByOpenIdOrderByOrderedTime(openId);
        List<OrderClientBody> orderClientBodies = new ArrayList<>();
        for (Order order : orders) {
            List<Schedule> schedules =
                    scheduleRepository.findSchedulesByOrder_OrderNumberOrderByTime(order.getOrderNumber());
            orderClientBodies.add(OrderClientBody.OrderToClientBody(order, schedules));
        }
        return orderClientBodies;
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
