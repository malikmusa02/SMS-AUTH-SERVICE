package com.java.sms.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ClassRoomType")
@Getter
@Setter
public class ClassRoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Override
    public String toString() {
        return this.name;
    }
}

