package com.example.sound_recorder;


import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.example.sound_recorder.MainActivity.getCtx;
import static com.example.sound_recorder.MainActivity.mySql;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSaved extends Fragment {
    private ListView listView;
    private View view;
    private TextView tv;
    private Map <String,String>map;
    public static List<Map<String,String>> list;
    private SQLiteDatabase db;
    public MyBaseAdapter myBaseAdapter;
    private Dialog mDialogManager;
    public static Handler Mhandler;
    private Intent intent;
    private myConn conn;
    public static MusicService.MyBinder binder;
//    private HomeAdapter homeAdapter;
    public FragmentSaved() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_fragment_saved, container, false);
        mDialogManager = new Dialog(getContext());
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        db = mySql.getWritableDatabase();
        Cursor cursor = db.query("RTable", null, null,
                null, null, null, null);
        list = new ArrayList<Map<String, String>>();
        conn=new myConn();
        intent=new Intent(getCtx(),MusicService.class);
        getCtx().bindService(intent,conn,BIND_AUTO_CREATE);


        if (cursor.getCount() == 0) {
            Toast.makeText(getCtx(), 0 + "", Toast.LENGTH_LONG).show();

        } else {
            cursor.moveToFirst();
            map = new HashMap<>();
            map.put("RName", cursor.getString(1));
            map.put("RLength", cursor.getString(2));
            map.put("RAddress", cursor.getString(3));
            map.put("RDate", cursor.getString(4));
            list.add(map);
        }
        while (cursor.moveToNext()) {
            map = new HashMap<>();
            map.put("RName", cursor.getString(1));
            map.put("RLength", cursor.getString(2));
            map.put("RAddress", cursor.getString(3));
            map.put("RDate", cursor.getString(4));
            list.add(map);
        }
        cursor.close();
        db.close();
        listView = (ListView) view.findViewById(R.id.listview);
        myBaseAdapter=new MyBaseAdapter();
        listView.setAdapter(myBaseAdapter);
        delete_tv();
        play_Media();
        return view;
    }

    private void play_Media() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Adapter adpter=parent.getAdapter();
                String get_date=adpter.getItem(position).toString().trim();
                int getName=get_date.indexOf("RName");
                int getLength=get_date.indexOf("RLength");
                String date1=get_date.substring(getName+6,getLength-2);
                mDialogManager.showPlayDialog(mDialogManager.getAddress(date1));
            }
        });
    }
    @Override
    public void onDestroy() {
        getCtx().unbindService(conn);
        super.onDestroy();
        Log.d("unbind","finish the service");
    }

    class myConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder=(MusicService.MyBinder) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private void delete_tv() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Adapter adpter=parent.getAdapter();
                String get_date=adpter.getItem(position).toString().trim();
                int getName=get_date.indexOf("RName");
                int getLength=get_date.indexOf("RLength");
                String date1=get_date.substring(getName+6,getLength-2);
                mDialogManager.showDeleteDialog(date1,mDialogManager.getAddress(date1));
                Mhandler = new Handler() {
                    public void handleMessage(android.os.Message message) {
                        switch (message.what) {
                            case 1:
                                list.remove(position);
                                myBaseAdapter.notifyDataSetChanged();
                                break;

                        }
                    };
                };
                return true;
            }
        });

    }

    static class MyBaseAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Map<String,String> showMap=list.get(position);
            Holder holder;
            if(convertView==null){
                convertView=LayoutInflater.from(getCtx()).inflate(R.layout.list_item,parent,false);
                holder=new Holder();
                holder.itemImage=(ImageView)convertView.findViewById(R.id.item_image);
                holder.titleName=(TextView) convertView.findViewById(R.id.item_name);
                holder.contentLength=(TextView) convertView.findViewById(R.id.item_duration);
                holder.contentDate=(TextView) convertView.findViewById(R.id.item_date);
                convertView.setTag(holder);
            }else {
                holder=(Holder)convertView.getTag();
            }
            holder.itemImage.setBackgroundResource(R.drawable.recorder);
            holder.titleName.setText(showMap.get("RName"));
            holder.contentLength.setText(showMap.get("RLength"));
            holder.contentDate.setText(showMap.get("RDate"));
            return convertView;
        }
        class Holder{
            ImageView itemImage;
            TextView titleName;
            TextView contentLength;
            TextView contentDate;
        }
    }
}


