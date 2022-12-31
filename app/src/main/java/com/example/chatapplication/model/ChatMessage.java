package com.example.chatapplication.model;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    public String senderId,receiverId,message,dateTime,type;
    public Date dateObject;
    public String conversionId, conversionName,conversionImage;
    public ChatMessage() {
    }


}
