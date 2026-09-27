package org.example.md04_ss12.common;

public class OutOfStockException extends RuntimeException {
    public static final String MESSAGE = "Sản phẩm đã hết hàng";

    public OutOfStockException() {
        super(MESSAGE);
    }
}
