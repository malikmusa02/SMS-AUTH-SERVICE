package com.java.sms.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ClassPeriod")
@Getter
@Setter
public class ClassPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // These are Django foreign key references (store IDs only)
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "year_level_id", nullable = false)
    private Long yearLevelId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "start_time_id", nullable = false)
    private Long startTimeId;

    @Column(name = "end_time_id", nullable = false)
    private Long endTimeId;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Override
    public String toString() {
        return this.name;
    }
}

