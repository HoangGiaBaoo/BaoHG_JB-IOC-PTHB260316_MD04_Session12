package org.example.md04_ss12.order;

public enum OrderSource {
    /** Bai 2: dat hang thuong, xu ly bat dong bo qua Kafka. */
    ASYNC,
    /** Bai 3: mua co khoa phan tan, tru kho truc tiep tren DB. */
    LOCKED,
    /** Bai 6: Flash Sale. */
    FLASH_SALE
}
