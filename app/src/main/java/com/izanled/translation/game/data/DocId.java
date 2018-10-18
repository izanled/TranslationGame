package com.izanled.translation.game.data;

import android.support.annotation.Nullable;

public class DocId {
    public String _docId;

    @SuppressWarnings("unchecked")
    public <T extends DocId> T withId(@Nullable final String id){
        this._docId = id;
        return (T) this;
    }

    public String get_docId() {
        return _docId;
    }

    public void set_docId(String _docId) {
        this._docId = _docId;
    }
}
