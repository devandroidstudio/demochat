package com.example.chatapplication.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class NewsViewModel extends ViewModel {
    private final MutableLiveData<List<News>> listNews = new MutableLiveData<>();
    private final MutableLiveData<List<String>> listUsersId = new MutableLiveData<>();
    public LiveData<List<News>> getListNews() {
        return listNews;
    }
    public void setListNews(List<News> list){
        this.listNews.setValue(list);
    }
    public MutableLiveData<List<String>> getListUsersId() {
        return listUsersId;
    }
    public void setListUsersId(List<String> list){
        listUsersId.setValue(list);
    }
}
