package com.example.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.cloud.stream.messaging.Processor;
import javax.persistence.*;

@Entity
@Table(name="Order_table")
public class Order {
    @Id @GeneratedValue
    Long id;
    Long productId;
    String productName;
    int qty;

    @PostPersist
    public void onPlaced(){
        OrderPlaced orderplaced = new OrderPlaced();
        orderplaced.setOrderId(this.getId());
        orderplaced.setProductId(this.getProductId());
        orderplaced.setProductName(this.getProductName());
        orderplaced.setOrderQty(this.getQty());

        // 해당 클래스를 json 으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;

        try {
            json = objectMapper.writeValueAsString(orderplaced);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }
        //System.out.println(json);

        // 메세지 큐에 publish
        Processor processor = OrderApplication.applicationContext.getBean(Processor.class);
        MessageChannel outputChannel = processor.output();

        outputChannel.send(MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
