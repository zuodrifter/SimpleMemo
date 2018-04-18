package com.example.simplememo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/*          ArrayAdapter通过泛型来指定要适配的数据类型
新建类MemoAdapter,这个适配器继承自ArrayAdapter，并将泛型指定为OneMemo类.*/
public class MemoAdapter extends ArrayAdapter<OneMemo>{

    private int resourceId;
    //定义备忘录颜色标签
    int[] color={Color.parseColor("#F5EFA0"),Color.parseColor("#8296D5"),
            Color.parseColor("#95C77E"),Color.parseColor("#F49393"),
            Color.parseColor("#FFFFFF")};

    // MemoAdapter重写了父类的一组构造函数，将上下文、ListView子项的布局的id和数据传递进来.
    public MemoAdapter(Context context, int resource, List<OneMemo> objects) {
        super(context, resource, objects);
        resourceId=resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OneMemo oneMemo=getItem(position);//获取当前项的OneMemo实例
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,
                false);//为子项加载传入的布局.注意false的含义

       /* 调用view的findViewById()方法分别获取ImageView和TextView的实例.*/
        ImageView tag=(ImageView)view.findViewById(R.id.tag);
        TextView textDate=(TextView)view.findViewById(R.id.textDate);
        TextView textTime=(TextView)view.findViewById(R.id.textTime);
        ImageView alarm=(ImageView) view.findViewById(R.id.alarm);
        TextView mainText=(TextView)view.findViewById(R.id.mainText);

        if(oneMemo.getTag()<color.length)
            tag.setBackgroundColor(color[oneMemo.getTag()]);
        //??????????????????
        textDate.setText(oneMemo.getTextDate());
        textTime.setText(oneMemo.getTextTime());
        mainText.setText(oneMemo.getMainText());
        //设置闹钟图标是否可见
        if(oneMemo.getAlarm()) {
            alarm.setVisibility(View.VISIBLE);
        }
        else {
            alarm.setVisibility(View.GONE);
        }

        return view;
    }
}
