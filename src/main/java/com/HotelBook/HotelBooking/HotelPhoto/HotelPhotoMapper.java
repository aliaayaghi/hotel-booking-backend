package com.HotelBook.HotelBooking.HotelPhoto;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HotelPhotoMapper {

    public HotelPhotoResponse toResponse(HotelPhoto photo) {
        return HotelPhotoResponse.builder()
                .id(photo.getId())
                .hotelId(photo.getHotel().getId())   // ← fixed
                .url(photo.getUrl())
                .caption(photo.getCaption())
                .order(photo.getOrder())
                .isCover(photo.isCover())
                .uploadedAt(photo.getUploadedAt())
                .build();
    }

    public List<HotelPhotoResponse> toResponseList(List<HotelPhoto> photos) {
        return photos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
