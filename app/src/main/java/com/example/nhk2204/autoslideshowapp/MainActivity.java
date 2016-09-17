package com.example.nhk2204.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE=2204;
    //Uriを格納するArrayList
    private static ArrayList<Uri> URIList=new ArrayList<>();
    //現在表示している画像がURIListの何番目の画像化を記憶するint
    private static int URIListNum;

    //スライドショウ再生に必要なタイマーやハンドラ
    private Timer timer;
    private Handler handler=new Handler();
    private long count=0;
    private TextView timerText;

    //再生中か否かを確認するboolean
    private boolean OnOffSwitch=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //タイマーにテキストをセット
        timerText=(TextView)findViewById(R.id.timer);
        //timerText.setText("00:00:0");

        //一応条件を確認
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            //パーミッション許可状況の確認
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                //許可されている場合画像情報を取得
                getContentsInfo();
            }else{
                //許可されていない場合(且つandroid6.0以降のバージョンの場合)は許可ダイアログを求める。
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_CODE);
            }
        //android5.0系以下の場合
        }else {
            getContentsInfo();
        }

        if(URIList.size()!=0) {
            //進むボタン
            Button button1 = (Button) findViewById(R.id.button1);
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (URIListNum + 1 == URIList.size()) {
                        URIListNum = 0;
                    } else {
                        URIListNum = URIListNum + 1;
                    }

                    //画像を表示する。
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageURI(URIList.get(URIListNum));
                }
            });

            //戻るボタン
            Button button2 = (Button) findViewById(R.id.button2);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (URIListNum == 0) {
                        URIListNum = URIList.size() - 1;
                    } else {
                        URIListNum = URIListNum - 1;
                    }

                    //画像を表示する。
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageURI(URIList.get(URIListNum));
                }
            });

            //再生/停止ボタン
            Button button3 = (Button) findViewById(R.id.button3);
            button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (OnOffSwitch == false) {
                        OnOffSwitch = true;
                        startSlideshow();
                    } else {
                        OnOffSwitch = false;
                        stopSlideshow();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults){
        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE:
                //パーミッションが許可された場合。
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo(){
        //画像の情報を取得する。
        ContentResolver resolver=getContentResolver();
        Cursor cursor=resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if(cursor.moveToFirst()){
            do{
                //indexからIDを取得しそのIDから画像URIを取得する。
                int fileIndex=cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id=cursor.getLong(fileIndex);
                Uri imageUri= ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);

                //UriをURIList(ArrayList)にスタックする。
                URIList.add(imageUri);

                Log.d("SlideShowApp","URI : "+imageUri.toString());
            }while(cursor.moveToNext());
        }

        //ギャラリーに画像がなかった際の処理
        if(URIList.size()==0){
            timerText.setText("ギャラリーに画像がありません。");
        }else {
            //目次（？）の最初の画像を表示する。
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            URIListNum = 0;
            imageView.setImageURI(URIList.get(URIListNum));
        }

        cursor.close();
    }

    //再生メソッド
    private void startSlideshow(){
        if(null!=timer){
            timer.cancel();
            timer=null;
        }

        //Timerインスタンスを生成
        timer=new Timer();

        //カウンター
        count=0;
        //timerText.setText("00:00:0");

        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                //handlerを使って処理をキューイングする。
                handler.post(new Runnable(){
                    @Override
                    public void run() {
                        count++;
                        //long mm=count*100/1000/60;
                        //long ss=count*100/1000%60;
                        //long ms=(count*100-ss*1000-mm*1000*60)/100;
                        //timerText.setText(String.format("%1$02d:%2$02d:%3$01d",mm,ss,ms));

                        if(count%2==0){
                            if(URIListNum+1==URIList.size()){
                                URIListNum=0;
                            }else{
                                URIListNum=URIListNum+1;
                            }

                            //画像を表示する。
                            ImageView imageView=(ImageView)findViewById(R.id.imageView);
                            imageView.setImageURI(URIList.get(URIListNum));
                        }
                    }
                });
            }
        },0,1000);
    }

    //停止メソッド
    private void stopSlideshow(){
        if(null!=timer){
            timer.cancel();
            timer=null;
            //timerText.setText("00:00:0");
        }
    }
}
