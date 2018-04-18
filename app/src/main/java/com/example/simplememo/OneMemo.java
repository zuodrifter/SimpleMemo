package com.example.simplememo;

//新建实体类OneMemo，作为ListView适配器的适配类型
public class OneMemo {
    private int tag;
    private String textDate;
    private String textTime;
    private boolean alarm;
    private String mainText;

    public OneMemo(int tag, String textDate, String textTime,boolean alarm, String mainText) {
        this.tag=tag;
        this.textDate=textDate;
        this.textTime=textTime;
        this.alarm=alarm;
        this.mainText=mainText;
    }

    //get
    public int getTag(){
        return tag;
    }
    public String getTextDate(){
        return textDate;
    }
    public String getTextTime(){
        return textTime;
    }
    public boolean getAlarm(){ return alarm; }
    public String getMainText(){
        return mainText;
    }

}
