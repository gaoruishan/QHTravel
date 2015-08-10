package com.cmcc.hyapps.andyou;


import android.widget.Button;

        import android.app.Activity;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainActivity extends Activity {

    private Button btn1 = null;
    private Button btn2 = null;
    private Button btn3 = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        PullToRefreshListView pullToRefreshListView=new PullToRefreshListView(this);
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(onClickListener);

        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(onClickListener);
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(onClickListener);
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Button btn = (Button)v;
            switch (btn.getId()) {
                case R.id.btn1:
                    Toast.makeText(MainActivity.this, "打电话", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:13800138000"));
                    startActivity(intent);
                    break;
                case R.id.btn2:
                    Toast.makeText(MainActivity.this, "发短信", Toast.LENGTH_LONG).show();
                    Intent intent2 = new Intent();
                    intent2.setAction(Intent.ACTION_SENDTO);
                    intent2.setData(Uri.parse("smsto:5554"));
                    intent2.putExtra("sms_body", "发短信");
                    startActivity(intent2);
                    break;
                case R.id.btn3:
//                    startActivity(new Intent(MainActivity.this,PullToRefreshActivity.class));
                    break;
                default:
                    break;
            }
        }
    };
}
