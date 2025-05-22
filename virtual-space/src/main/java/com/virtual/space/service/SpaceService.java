package com.virtual.space.service;

import com.virtual.space.dto.*;
import com.virtual.space.entity.Space;
import com.virtual.space.entity.SpaceElement;
import com.virtual.space.entity.User;
import com.virtual.space.entity.UserType;
import com.virtual.space.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UserService userService;

    public Space createSpace(CreateSpaceRequest request, String username) {
        User user = userService.findByUsername(username);

        Space space = new Space();
        space.setName(request.getName());
        space.setDimensions(request.getDimensions());
        space.setMapId(request.getMapId());
        space.setCreator(user);

        return spaceRepository.save(space);
    }

    public void deleteSpace(Long spaceId, String username) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found"));

        User user = userService.findByUsername(username);

        if (!space.getCreator().getId().equals(user.getId()) &&
                user.getType() != UserType.ADMIN) {
            throw new RuntimeException("Unauthorized to delete this space");
        }

        spaceRepository.delete(space);
    }

    public List<SpaceResponse> getAllSpaces() {
        return spaceRepository.findAll().stream()
                .map(this::convertToSpaceResponse)
                .collect(Collectors.toList());
    }

    public SpaceDetailResponse getSpaceById(Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found"));

        return convertToSpaceDetailResponse(space);
    }

    private SpaceResponse convertToSpaceResponse(Space space) {
        SpaceResponse response = new SpaceResponse();
        response.setId(space.getId());
        response.setName(space.getName());
        response.setDimensions(space.getDimensions());
        response.setThumbnail(space.getThumbnail());
        return response;
    }

    private SpaceDetailResponse convertToSpaceDetailResponse(Space space) {
        SpaceDetailResponse response = new SpaceDetailResponse();
        response.setDimensions(space.getDimensions());

        List<SpaceElementResponse> elementResponses = space.getElements().stream()
                .map(this::convertToSpaceElementResponse)
                .collect(Collectors.toList());

        response.setElements(elementResponses);
        return response;
    }

    private SpaceElementResponse convertToSpaceElementResponse(SpaceElement spaceElement) {
        SpaceElementResponse response = new SpaceElementResponse();
        response.setId(spaceElement.getId());
        response.setX(spaceElement.getX());
        response.setY(spaceElement.getY());

        // Convert Element to ElementResponse
        ElementResponse elementResponse = new ElementResponse();
        elementResponse.setId(spaceElement.getElement().getElementId());
        elementResponse.setImageUrl(spaceElement.getElement().getImageUrl());
        elementResponse.setIsStatic(spaceElement.getElement().getIsStatic());
        elementResponse.setHeight(spaceElement.getElement().getHeight());
        elementResponse.setWidth(spaceElement.getElement().getWidth());

        response.setElement(elementResponse);
        return response;
    }
}
