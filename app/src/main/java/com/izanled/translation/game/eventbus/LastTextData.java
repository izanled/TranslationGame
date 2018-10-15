package com.izanled.translation.game.eventbus;

public class LastTextData {
    private String lastText;

    public LastTextData(String lastText) {
        this.lastText = lastText;
    }

    public String getLastText() {
        return lastText;
    }

    public void setLastText(String lastText) {
        this.lastText = lastText;
    }
}
