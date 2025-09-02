package com.siso.payment.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Payment payment;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "item", nullable = true)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = true)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = true)
    private PaymentMethod paymentMethod;

}
