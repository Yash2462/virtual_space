package com.virtual.space.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "spaces")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String dimensions;
    private String mapId;
    private String thumbnail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
    private List<SpaceElement> elements = new ArrayList<>();

    // constructors, getters, setters
}
