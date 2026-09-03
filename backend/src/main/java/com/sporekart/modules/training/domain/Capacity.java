package com.sporekart.modules.training.domain;

import com.sporekart.modules.training.domain.exception.CapacityExceededException;

import java.util.Objects;

public class Capacity {

    private final int totalCapacity;
    private final int occupiedSeats;

    public Capacity(int totalCapacity, int occupiedSeats) {
        if (totalCapacity <= 0) {
            throw new IllegalArgumentException("Total capacity must be greater than zero");
        }
        if (occupiedSeats < 0) {
            throw new IllegalArgumentException("Occupied seats cannot be negative");
        }
        if (occupiedSeats > totalCapacity) {
            throw new CapacityExceededException("Occupied seats (" + occupiedSeats + ") cannot exceed total capacity (" + totalCapacity + ")");
        }
        this.totalCapacity = totalCapacity;
        this.occupiedSeats = occupiedSeats;
    }

    public static Capacity of(int totalCapacity) {
        return new Capacity(totalCapacity, 0);
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public int getOccupiedSeats() {
        return occupiedSeats;
    }

    public int getAvailableSeats() {
        return totalCapacity - occupiedSeats;
    }

    public boolean isFull() {
        return occupiedSeats >= totalCapacity;
    }

    public Capacity allocateSeat() {
        if (isFull()) {
            throw new CapacityExceededException("Cannot allocate seat: Batch capacity is full (" + totalCapacity + "/" + totalCapacity + ")");
        }
        return new Capacity(totalCapacity, occupiedSeats + 1);
    }

    public Capacity releaseSeat() {
        if (occupiedSeats <= 0) {
            return this;
        }
        return new Capacity(totalCapacity, occupiedSeats - 1);
    }

    public Capacity withTotalCapacity(int newTotalCapacity) {
        if (newTotalCapacity < occupiedSeats) {
            throw new CapacityExceededException("New capacity (" + newTotalCapacity + ") cannot be less than currently occupied seats (" + occupiedSeats + ")");
        }
        return new Capacity(newTotalCapacity, occupiedSeats);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capacity capacity = (Capacity) o;
        return totalCapacity == capacity.totalCapacity && occupiedSeats == capacity.occupiedSeats;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCapacity, occupiedSeats);
    }
}
