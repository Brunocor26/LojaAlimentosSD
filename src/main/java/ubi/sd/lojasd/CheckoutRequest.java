package ubi.sd.lojasd;

import java.util.List;

public class CheckoutRequest {
    private List<CheckoutItem> itens;

    public CheckoutRequest() {}

    public CheckoutRequest(List<CheckoutItem> itens) {
        this.itens = itens;
    }

    public List<CheckoutItem> getItens() {
        return itens;
    }

    public void setItens(List<CheckoutItem> itens) {
        this.itens = itens;
    }
}
