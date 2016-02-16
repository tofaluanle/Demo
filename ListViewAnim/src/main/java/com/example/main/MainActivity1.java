package com.example.main;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
/**
 * 2013年10月25日 17:07:20
 * @author 洋葱
 * */
public class MainActivity1 extends Activity {

	private ListView mListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        mListView = (ListView) this.findViewById(R.id.list);
        List<String> list = new ArrayList<String>();
        for(int i=0;i<12;i++){
        	list.add("洋葱"+i);
        }
        //实例化自定义内容适配类
        MyAdapter adapter = new MyAdapter(this,list);
        //为listView设置适配
        mListView.setAdapter(adapter);
    }


    
}
