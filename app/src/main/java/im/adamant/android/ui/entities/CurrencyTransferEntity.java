package im.adamant.android.ui.entities;

import java.math.BigDecimal;

public class CurrencyTransferEntity {
    public enum Direction {
        SEND,
        RECEIVE
    }

    private String address;
    private BigDecimal amount;
    private int precision;
    private Direction direction;
    private String currencyAbbreviation;
    private String contactName;
    private long unixTransferDate;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getCurrencyAbbreviation() {
        return currencyAbbreviation;
    }

    public void setCurrencyAbbreviation(String currencyAbbreviation) {
        this.currencyAbbreviation = currencyAbbreviation;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public long getUnixTransferDate() {
        return unixTransferDate;
    }

    public void setUnixTransferDate(long unixTransferDate) {
        this.unixTransferDate = unixTransferDate;
    }
}
