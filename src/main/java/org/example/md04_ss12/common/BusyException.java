package org.example.md04_ss12.common;

/** Khong gianh duoc khoa trong thoi gian cho phep. */
public class BusyException extends RuntimeException {
    public BusyException(String message) {
        super(message);
    }
}
