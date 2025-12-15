package com.java.sms.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ClassRoom")
@Getter
@Setter
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to ClassRoomType entity (local relation)
    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private ClassRoomType roomType;

    @Column(name = "room_name", nullable = false, length = 200)
    private String roomName;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Override
    public String toString() {
        return roomType + " - " + roomName;
    }
}

