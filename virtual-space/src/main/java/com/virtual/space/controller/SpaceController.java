package com.virtual.space.controller;

import com.virtual.space.dto.CreateSpaceRequest;
import com.virtual.space.dto.SpaceDetailResponse;
import com.virtual.space.dto.SpaceResponse;
import com.virtual.space.entity.Space;
import com.virtual.space.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/space")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @PostMapping
    public ResponseEntity<Space> createSpace(@Valid @RequestBody CreateSpaceRequest request,
                                             Authentication authentication) {
        Space space = spaceService.createSpace(request, authentication.getName());
        return ResponseEntity.ok(space);
    }

    @DeleteMapping("/{spaceId}")
    public ResponseEntity<?> deleteSpace(@PathVariable Long spaceId, Authentication authentication) {
        try {
            spaceService.deleteSpace(spaceId, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, List<SpaceResponse>>> getAllSpaces() {
        List<SpaceResponse> spaces = spaceService.getAllSpaces();
        Map<String, List<SpaceResponse>> response = new HashMap<>();
        response.put("spaces", spaces);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{spaceId}")
    public ResponseEntity<SpaceDetailResponse> getSpace(@PathVariable Long spaceId) {
        SpaceDetailResponse space = spaceService.getSpaceById(spaceId);
        return ResponseEntity.ok(space);
    }
}
