package com.example.simplememo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{
    //长按事件和点击事件
    //list to store all the memo定义一个数组列表memolist里面存放OneMemo类型的数据.
    private List<OneMemo> memolist=new ArrayList<>();
    //adapter
    MemoAdapter adapter;
    //main ListView
    ListView lv;
    //alarm clock
    int BIG_NUM_FOR_ALARM=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Connector.getDatabase();
        loadHistoryData();

        //构建适配器对象
        adapter=new MemoAdapter(MainActivity.this, R.layout.memo_list, memolist);
        lv=(ListView) findViewById(R.id.list);
        lv.setAdapter(adapter);
        //注册点击事件
        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                onAdd();
                break;
            default:
        }
        return true;
    }



    private void loadHistoryData() {
        List<Memo> memoes= DataSupport.findAll(Memo.class);//查询Memo表，返回值是一个Memo类型的List集合：memos

        if(memoes.size()==0) {
            initializeLitePal();
            memoes = DataSupport.findAll(Memo.class);
        }

        for(Memo record:memoes) {
            Log.d("MainActivity", "current num: " + record.getNum());
            Log.d("MainActivity", "id: " + record.getId());
            Log.d("MainActivity", "getAlarm: " + record.getAlarm());
            int tag = record.getTag();
            String textDate = record.getTextDate();
            String textTime = record.getTextTime();
            boolean alarm = record.getAlarm().length() > 1 ? true : false;
            String mainText = record.getMainText();
            //将数据库中Memo表里的数据存放在temp里在提交到memolist
            OneMemo temp = new OneMemo(tag, textDate, textTime, alarm, mainText);
            memolist.add(temp);
        }
    }



    //点击事件
    //通过position参数判断用户点击的是哪一个子项
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent it=new Intent(this,Edit.class);
        Memo record=getMemoWithNum(position);
        //add information into intent
        transportInformationToEdit(it, record);
        startActivityForResult(it,position);
    }



    //长按事件
    //如果有闹钟先关闭再将子项从memolist里移除，最后进行litepal删除操作
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int n=memolist.size();
        if(memolist.get(position).getAlarm()) {
            cancelAlarm(position);
        }
        memolist.remove(position);
        adapter.notifyDataSetChanged();
        /* notifyDataSetChanged是Adater的一个方法，主要用来通知ListView，告诉它Adapter的数据发生了变化，
        需要更新ListView的显示，所以当Adapter的数据内容改变时会调用notifyDataSetChanged()方法
        简而言之就是动态刷新ListView数据*/
        String whereArgs = String.valueOf(position);
        DataSupport.deleteAll(Memo.class, "num = ?", whereArgs);

        for(int i=position+1; i<n; i++) {
            ContentValues temp = new ContentValues();
            temp.put("num", i-1);
            String where = String.valueOf(i);
            DataSupport.updateAll(Memo.class, temp, "num = ?", where);
        }
        /*当前项被删，position发生了变化，后面的子项的顺序num都需要减一位
        如：123456789 当num为5的移除后，后面子项顺序就得往前进一位*/
        return true;
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        if(resultCode==RESULT_OK) {
            updateLitePalAndList(requestCode, it);
        }
    }



    //根据Edit.class返回的“num”更新数据库和备忘录列表
    private void updateLitePalAndList(int requestCode, Intent it) {

        int num=requestCode;
        int tag=it.getIntExtra("tag",0);

        Calendar c=Calendar.getInstance();//创建实例 默认是当前时刻
        String current_date=getCurrentDate(c);
        String current_time=getCurrentTime(c);

        String alarm=it.getStringExtra("alarm");//获取Edit返回的闹钟信息.即设置的闹钟
        String mainText=it.getStringExtra("mainText");//获取Edit返回的文本信息.即编辑的备忘录

        boolean gotAlarm = alarm.length() > 1;//和第86行进行比较，用这种表达方式替代上面那种
        OneMemo new_memo = new OneMemo(tag, current_date, current_time, gotAlarm, mainText);

        if((requestCode+1)>memolist.size()) {
            // add a new memo record into database
            addRecordToLitePal(num, tag, current_date, current_time, alarm, mainText);

            // add a new OneMemo object into memolist and show
            memolist.add(new_memo);
        }
        else {
            /*若不是新增的，只是更改原有的备忘录信息：如果存有闹钟就先将其关闭*/
            if(memolist.get(num).getAlarm()) {
                cancelAlarm(num);
            }

            //update the previous "num" memo
            ContentValues temp = new ContentValues();
            temp.put("tag", tag);
            temp.put("textDate", current_date);
            temp.put("textTime", current_time);
            temp.put("alarm", alarm);
            temp.put("mainText", mainText);
            String where = String.valueOf(num);//点击哪个子项进行Edit,顺序num就对应的requestCode
            DataSupport.updateAll(Memo.class, temp, "num = ?", where);

            memolist.set(num, new_memo);//更新num值对应的备忘录信息到显示界面
        }
        //if user has set up an alarm
        if(gotAlarm) {
            loadAlarm(alarm, requestCode, 0);
        }

        adapter.notifyDataSetChanged();
    }



    //when there's no memo in the app，初始化LitePal and show
    private void initializeLitePal() {
        Calendar c=Calendar.getInstance();
        String textDate=getCurrentDate(c);
        String textTime=getCurrentTime(c);
        //insert two records into the database
        addRecordToLitePal(0,0,textDate,textTime,"","click to edit");
        addRecordToLitePal(1,1,textDate,textTime,"","long click to delete");
    }



    //以 XX/XX格式获取当前日期
    private String getCurrentDate(Calendar c){
        return c.get(Calendar.YEAR)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH);
    }
    //以 XX：XX格式获取当前时间
    private String getCurrentTime(Calendar c){
        String current_time="";
        if(c.get(Calendar.HOUR_OF_DAY)<10) current_time=current_time+"0"+c.get(Calendar.HOUR_OF_DAY);
        else current_time=current_time+c.get(Calendar.HOUR_OF_DAY);

        current_time=current_time+":";

        if(c.get(Calendar.MINUTE)<10) current_time=current_time+"0"+c.get(Calendar.MINUTE);
        else current_time=current_time+c.get(Calendar.MINUTE);

        return current_time;
    }



    //定义 使用Litepal添加数据的方法
    private void addRecordToLitePal(int num, int tag, String textDate, String textTime, String alarm, String mainText) {
        Memo record=new Memo();
        record.setNum(num);
        record.setTag(tag);
        record.setTextDate(textDate);
        record.setTextTime(textTime);
        record.setAlarm(alarm);
        record.setMainText(mainText);
        record.save();
    }

    private void transportInformationToEdit(Intent it, Memo record) {
        it.putExtra("num",record.getNum());
        it.putExtra("tag",record.getTag());
        it.putExtra("textDate",record.getTextDate());
        it.putExtra("textTime",record.getTextTime());
        it.putExtra("alarm",record.getAlarm());
        it.putExtra("mainText",record.getMainText());
    }

    //press the add button
    public void onAdd() {
        Intent it=new Intent(this,Edit.class);

        int position = memolist.size();

        Calendar c=Calendar.getInstance();
        String current_date=getCurrentDate(c);
        String current_time=getCurrentTime(c);

        it.putExtra("num",position);
        it.putExtra("tag",0);
        it.putExtra("textDate",current_date);
        it.putExtra("textTime",current_time);
        it.putExtra("alarm","");
        it.putExtra("mainText","");

        startActivityForResult(it,position);
    }

    //查询Memo表中所以的第一条数据num
    private Memo getMemoWithNum(int num) {
        String whereArgs = String.valueOf(num);
        Memo record= DataSupport.where("num = ?", whereArgs).findFirst(Memo.class);
        return record;
    }

    //***********************************load or cancel alarm************************************************************************************
    //*****************BUG  SOLVED*************************
    //still have a bug as I know:
    //after deleting a memo, the "num" changes, then the cancelAlarm may have some trouble (it do not cancel actually)
    //establishing a hash table may solve this problem
    //SOLVED through adding id
    //******************************************

    //根据"alarm"设置闹钟
    private void loadAlarm(String alarm, int num, int days) {
        int alarm_hour=0;
        int alarm_minute=0;
        int alarm_year=0;
        int alarm_month=0;
        int alarm_day=0;

        int i=0, k=0;
        while(i<alarm.length()&&alarm.charAt(i)!='/') i++;
        alarm_year=Integer.parseInt(alarm.substring(k,i));
        k=i+1;i++;
        while(i<alarm.length()&&alarm.charAt(i)!='/') i++;
        alarm_month=Integer.parseInt(alarm.substring(k,i));
        k=i+1;i++;
        while(i<alarm.length()&&alarm.charAt(i)!=' ') i++;
        alarm_day=Integer.parseInt(alarm.substring(k,i));
        k=i+1;i++;
        while(i<alarm.length()&&alarm.charAt(i)!=':') i++;
        alarm_hour=Integer.parseInt(alarm.substring(k,i));
        k=i+1;i++;
        alarm_minute=Integer.parseInt(alarm.substring(k));

        Memo record=getMemoWithNum(num);

        /*当闹铃响起时，我们想要向我们的BroadcastReceiver广播一个Intent。 在这里，我们用一个
        明确的类名制作一个Intent，让我们自己的接收器（已在AndroidManifest.xml中发布）实例化并调用，
        然后创建一个Intent发送者以将该意图作为广播执行。*/
        Intent intent = new Intent(MainActivity.this, OneShotAlarm.class);
        intent.putExtra("alarmId",record.getId()+BIG_NUM_FOR_ALARM);
        PendingIntent sender = PendingIntent.getBroadcast(
                MainActivity.this, record.getId()+BIG_NUM_FOR_ALARM, intent, 0);

        // 我们希望闹钟从现在起10秒后关闭。
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Calendar alarm_time = Calendar.getInstance();
        alarm_time.set(alarm_year,alarm_month-1,alarm_day,alarm_hour,alarm_minute);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, alarm_time.getTimeInMillis(), sender);
    }

    //关闭闹钟
    private void cancelAlarm(int num) {
        Memo record=getMemoWithNum(num);

        Intent intent = new Intent(MainActivity.this,
                OneShotAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(
                MainActivity.this, record.getId()+BIG_NUM_FOR_ALARM, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

}