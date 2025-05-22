package com.virtual.space.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "space_elements")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpaceElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    @ManyToOne
    @JoinColumn(name = "element_id")
    private Element element;

    private Integer x;
    private Integer y;

    // constructors, getters, setters
}
