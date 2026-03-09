package com.HotelBook.HotelBooking.booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


public class BookingResponseDTO {

    private UUID id;
    private UUID customerId;
    private UUID hotelId;
    private UUID roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer adults;
    private Integer children;
    private Integer roomCount;
    private long numberOfNights;
    private BigDecimal pricePerNight;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String specialRequests;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

   private PaymentSummary payment;
    private CancellationTierSummary cancellationPolicy;


    public static class PaymentSummary {
        private UUID paymentId;
        private String status;        // PAID, FAILED, REFUNDED, PENDING_REFUND
        private BigDecimal amount;
        private BigDecimal refundAmount;
        private String paymentMethod;
        private LocalDateTime paidAt;

        public UUID getPaymentId() { return paymentId; }
        public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public LocalDateTime getPaidAt() { return paidAt; }
        public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    }


    public static class CancellationTierSummary {
        private UUID policyId;
        private String tierName;           // "Free Cancellation"
        private Integer deadlineHours;     // 168 (7 days)
        private Integer refundPercentage;  // 100
        private BigDecimal priceMultiplier; // 1.25

        public UUID getPolicyId() { return policyId; }
        public void setPolicyId(UUID policyId) { this.policyId = policyId; }
        public String getTierName() { return tierName; }
        public void setTierName(String tierName) { this.tierName = tierName; }
        public Integer getDeadlineHours() { return deadlineHours; }
        public void setDeadlineHours(Integer deadlineHours) { this.deadlineHours = deadlineHours; }
        public Integer getRefundPercentage() { return refundPercentage; }
        public void setRefundPercentage(Integer refundPercentage) { this.refundPercentage = refundPercentage; }
        public BigDecimal getPriceMultiplier() { return priceMultiplier; }
        public void setPriceMultiplier(BigDecimal priceMultiplier) { this.priceMultiplier = priceMultiplier; }
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getHotelId() { return hotelId; }
    public void setHotelId(UUID hotelId) { this.hotelId = hotelId; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getAdults() { return adults; }
    public void setAdults(Integer adults) { this.adults = adults; }

    public Integer getChildren() { return children; }
    public void setChildren(Integer children) { this.children = children; }

    public Integer getRoomCount() { return roomCount; }
    public void setRoomCount(Integer roomCount) { this.roomCount = roomCount; }

    public long getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(long numberOfNights) { this.numberOfNights = numberOfNights; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public PaymentSummary getPayment() { return payment; }
    public void setPayment(PaymentSummary payment) { this.payment = payment; }

    public CancellationTierSummary getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(CancellationTierSummary cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }
}
