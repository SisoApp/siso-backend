package com.siso.call.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "Calls")
@Getter
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
