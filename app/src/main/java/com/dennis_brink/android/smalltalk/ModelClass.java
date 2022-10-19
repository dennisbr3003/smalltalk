package com.dennis_brink.android.smalltalk;

public class ModelClass {

    String message;
    String from;

    public ModelClass() {
    }

    public ModelClass(String message, String sender) {
        this.message = message;
        this.from = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "ModelClass{" +
                "message='" + message + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}
