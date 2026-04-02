package com.HotelBook.HotelBooking.room;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public class RoomResponseDTO {

    private UUID id;
    private UUID hotelId;
    private String name;
    private RoomType type;
    private BedType bedType;
    private String description;
    private Integer maxAdults;
    private Integer maxChildren;
    private Integer quantity;
    private BigDecimal sizeSqm;
    private Integer floor;
    private RoomView view;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private List<PhotoDTO> photos;
    private List<AmenityDTO> amenities;
    private List<AccessibilityDTO> accessibilities;



    public static class PhotoDTO {
        private UUID id;
        private String url;
        private Integer displayOrder;
        private String caption;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
    }

    public static class AmenityDTO {
        private UUID id;
        private String name;
        private String category;
        private String icon;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    public static class AccessibilityDTO {
        private UUID id;
        private String feature;
        private Boolean isAvailable;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getFeature() { return feature; }
        public void setFeature(String feature) { this.feature = feature; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }



    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getHotelId() { return hotelId; }
    public void setHotelId(UUID hotelId) { this.hotelId = hotelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public BedType getBedType() { return bedType; }
    public void setBedType(BedType bedType) { this.bedType = bedType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxAdults() { return maxAdults; }
    public void setMaxAdults(Integer maxAdults) { this.maxAdults = maxAdults; }

    public Integer getMaxChildren() { return maxChildren; }
    public void setMaxChildren(Integer maxChildren) { this.maxChildren = maxChildren; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSizeSqm() { return sizeSqm; }
    public void setSizeSqm(BigDecimal sizeSqm) { this.sizeSqm = sizeSqm; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public RoomView getView() { return view; }
    public void setView(RoomView view) { this.view = view; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PhotoDTO> getPhotos() { return photos; }
    public void setPhotos(List<PhotoDTO> photos) { this.photos = photos; }

    public List<AmenityDTO> getAmenities() { return amenities; }
    public void setAmenities(List<AmenityDTO> amenities) { this.amenities = amenities; }

    public List<AccessibilityDTO> getAccessibilities() { return accessibilities; }
    public void setAccessibilities(List<AccessibilityDTO> accessibilities) { this.accessibilities = accessibilities; }
}