package com.izanled.translation.game.data;

public class TranslationData {
    private int _id;
    private String original;
    private String translation;
    private int count;
    private String target;
    private String source;

    public TranslationData() {
    }

    public TranslationData(int _id, String original, String translation, int count, String source,  String target) {
        this._id = _id;
        this.original = original;
        this.translation = translation;
        this.count = count;
        this.source = source;
        this.target = target;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
