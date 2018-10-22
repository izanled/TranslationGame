package com.izanled.translation.game.view.base;

public abstract class AbstractPresenter<V extends BaseView> implements BasePresenter {
    private V view;

    public AbstractPresenter(V view) {
        this.view = view;

        view.setPresenter(this);
    }

    public V getView() {
        return view;
    }
}
