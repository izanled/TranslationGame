package com.izanled.translation.game.view.base;

public interface BaseView<T extends BasePresenter> {
    void setPresenter(T presenter);
}
