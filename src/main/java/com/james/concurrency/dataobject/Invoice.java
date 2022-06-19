package com.james.concurrency.dataobject;

/**
 * 发票
 */
public class Invoice {

    private Long id;

    private String productBuyerId;

    /**
     * 发票接收人
     */
    private String recipient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductBuyerId() {
        return productBuyerId;
    }

    public void setProductBuyerId(String productBuyerId) {
        this.productBuyerId = productBuyerId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
