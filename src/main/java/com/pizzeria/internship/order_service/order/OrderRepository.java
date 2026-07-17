package com.pizzeria.internship.order_service.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByPhoneNumberAndStatusIn(String phoneNumber, List<Status> statuses);
}
