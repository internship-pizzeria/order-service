package com.pizzeria.internship.order_service.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByLocationId(Long locationId);

    List<Order> findByLocationIdAndStatusIn(Long locationId, List<Status> statuses);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") Status status);

    List<Order> findByStatusAndCreatedAtBefore(Status status, Instant createdAt);
}
