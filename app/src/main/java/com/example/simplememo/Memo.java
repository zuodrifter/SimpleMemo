package com.example.simplememo;

import org.litepal.crud.DataSupport;

/*                  Memo类是一个Java Bean
所谓的对象关系映射模式,就是将Java Bean对象,直接映射到数据库中的表结构.
向数据库操作数据才是真正的意义所在,所以Book需要继承DataSupport才可以.*/
public class Memo extends DataSupport {
    private int id;
    private int num;
    private int tag;//标签
    private String textDate;
    private String textTime;
    private String alarm;
    private String mainText;

    //get
    public int getId() {
        return id;
    }
    public int getNum(){
        return num;
    }
    public int getTag(){
        return tag;
    }
    public String getTextDate(){
        return textDate;
    }
    public String getTextTime(){
        return textTime;
    }
    public String getAlarm(){
        return alarm;
    }
    public String getMainText(){
        return mainText;
    }

    //set
    public void setId(int id){
        this.id=id;
    }
    public void setNum(int num) {
        this.num=num;
    }
    public void setTag(int tag){
        this.tag=tag;
    }
    public void setTextDate(String textDate){
        this.textDate=textDate;
    }
    public void setTextTime(String textTime){
        this.textTime=textTime;
    }
    public void setAlarm(String alarm){
        this.alarm=alarm;
    }
    public void setMainText(String mainText){
        this.mainText=mainText;
    }
}
