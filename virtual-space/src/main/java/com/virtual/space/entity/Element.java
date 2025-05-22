package com.virtual.space.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "elements")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Element {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String elementId;
    private String imageUrl;
    private Integer width;
    private Integer height;
    private Boolean isStatic;
}
