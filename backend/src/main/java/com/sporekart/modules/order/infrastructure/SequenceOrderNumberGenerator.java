package com.sporekart.modules.order.infrastructure;

import com.sporekart.modules.order.domain.OrderNumberPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SequenceOrderNumberGenerator implements OrderNumberPort {

    private final JdbcTemplate jdbcTemplate;
    private final AtomicLong fallbackCounter = new AtomicLong(100001);

    public SequenceOrderNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seqValue;
        try {
            Long val = jdbcTemplate.queryForObject("SELECT nextval('order_number_seq')", Long.class);
            seqValue = val != null ? val : fallbackCounter.getAndIncrement();
        } catch (Exception e) {
            seqValue = fallbackCounter.getAndIncrement();
        }
        return String.format("SPK-%s-%06d", datePrefix, seqValue % 1000000);
    }
}
