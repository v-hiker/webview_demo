package com.example.broswer2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {


    //数据声明
    private long exitTime = 0;//双击返回结束的间隔时间
    int dark_mode_flag = 0;//是否为夜间模式
    static String is_home = "";//判断是否为主页
    static String home = "https://m.mijisou.com";//主页
    static String search_engine = "https://mijisou.com/?q=";//搜索引擎
    //public static MainActivity instance = null;//用以在其他类中调用本类函数MainActivity.instance.myfunction()
    String now_url = home;

    LinearLayout layout_bottombar;//底栏
    RelativeLayout layout_topbar;//顶栏
    //LinearLayout layout_topbar;
    RelativeLayout bottom_tool_bar;//底部展开栏
    RelativeLayout back_dim_layout;//webview阴影背景
    RelativeLayout back_dim_layout2;//全局阴影背景
    LinearLayout layout_web;//承载webview主体

    ImageButton imageButton_back;//底栏返回按钮
    ImageButton imageButton_forward;//底栏前进按钮
    //ImageButton imageButton_window;//底栏窗口按钮，暂未实现
    ImageButton imageButton_home;//底栏主页按钮
    ImageButton imageButton_setting;//底栏设置按钮
    ImageButton imageButton_search_engine;//顶栏搜索引擎切换按钮
    ImageButton imageButton_refresh;//顶栏刷新按钮

    EditText editText_input;//输入框
    ProgressBar progressBar;//加载进度条

    Button see_bookmark,add_bookmark,set_home,dark_mod;//底部工具栏各按钮
    ImageButton to_exit,to_hidetool;//退出及底部工具栏隐藏按钮

    SQLiteDatabase db_bm;//书签数据库

    WebView webView;//webview主体

    ListView listview_bookmark;//自定义listview格式用以显示书签


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instance = this;

        //透明状态栏，然后使状态栏各字体图标为黑色
        transparentStatusbar(this);
        lightStatusbar(this);

        //对应声明顺序，实例化各view
        layout_bottombar = (LinearLayout) findViewById(R.id.layout_bottombar);
        layout_topbar = (RelativeLayout) findViewById(R.id.layout_topbar);
        //layout_topbar = (LinearLayout) findViewById(R.id.layout_topbar);
        bottom_tool_bar = (RelativeLayout)findViewById(R.id.bottontoolbar);
        back_dim_layout = (RelativeLayout) findViewById(R.id.bac_dim_layout);
        back_dim_layout2 = (RelativeLayout) findViewById(R.id.bac_dim_layout2);
        layout_web = (LinearLayout) findViewById(R.id.layout_web);

        imageButton_back = (ImageButton) findViewById(R.id.iv_back);
        imageButton_forward = (ImageButton) findViewById(R.id.iv_forward);
        imageButton_home = (ImageButton) findViewById(R.id.iv_home);
        //ImageButton imageButton_window = (ImageButton)findViewById(R.id.iv_windows);
        imageButton_setting = (ImageButton) findViewById(R.id.iv_setting);
        imageButton_search_engine = (ImageButton) findViewById(R.id.iv_search);
        imageButton_refresh = (ImageButton) findViewById(R.id.iv_refresh);

        editText_input = (EditText) findViewById(R.id.et_input);
        progressBar = (ProgressBar) findViewById(R.id.pb_web);

        see_bookmark = (Button)findViewById(R.id.show_bookmark);
        add_bookmark = (Button)findViewById(R.id.add_bookmark);
        set_home = (Button)findViewById(R.id.set_home);
        dark_mod = (Button)findViewById(R.id.dark_mode);
        to_exit = (ImageButton)findViewById(R.id.tool_exit);
        to_hidetool = (ImageButton)findViewById(R.id.tool_down);

        init_db();//创建数据库，只有第一次会被创建
        //init_setting();





        webView = (WebView) findViewById(R.id.wv_main);

        listview_bookmark = (ListView)findViewById(R.id.listview_bookmark);

        //webview设置
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);//允许https链接引用http资源

        /*String user_agent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(user_agent);*///暂未实现的自定义浏览器标识功能



        //webview阴影背景点击消失
        back_dim_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back_dim_layout.setVisibility(View.GONE);
                if(bottom_tool_bar.getVisibility()==View.VISIBLE){
                    bottom_tool_bar.setVisibility(View.GONE);
                }//隐藏底部工具栏
                webView.requestFocus();

            }
        });

        //webview长按工具，暂未实现
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        //切换搜索引擎
        imageButton_search_engine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchEngineChange();
            }
        });

        //顶部刷新按钮的两种点击事件
        final ImageView.OnClickListener refresh_in_webview = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        };
        final ImageButton.OnClickListener refresh_in_input = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = editText_input.getText().toString();
                if (!temp.isEmpty()) {
                    back_dim_layout.setVisibility(View.GONE);
                    webView.loadUrl(tourl(temp));
                    webView.requestFocus();
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }

        };

        //TOP输入框聚焦失焦的变化
        editText_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    back_dim_layout.setVisibility(View.VISIBLE);
                    editText_input.setText(webView.getUrl());
                    editText_input.selectAll();
                    imageButton_refresh.setImageResource(R.drawable.navigation_forward);
                    imageButton_refresh.setOnClickListener(refresh_in_input);
                } else {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    editText_input.setText("");
                    imageButton_refresh.setImageResource(R.drawable.navigation_refresh);
                    imageButton_refresh.setOnClickListener(refresh_in_webview);
                    webView.requestFocus();
                }
            }
        });

        //TOP输入框软键盘功能
        editText_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    String temp = editText_input.getText().toString();
                    if (!temp.isEmpty()) {
                        back_dim_layout.setVisibility(View.GONE);
                        webView.loadUrl(tourl(temp));
                        webView.requestFocus();
                        InputMethodManager imm = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });

        //底部工具栏功能
        see_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query_bm();
            }
        });
        add_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addbookmark();
            }
        });
        set_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHomeSet(v);
            }
        });
        dark_mod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                darkMode();
                Toast.makeText(getApplicationContext(), "切换成功", Toast.LENGTH_SHORT).show();
            }
        });
        to_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        to_hidetool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottom_tool_bar.setVisibility(View.GONE);
                back_dim_layout.setVisibility(View.GONE);
            }
        });

        //WebClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                is_home = url;
                if (("https".equalsIgnoreCase(Uri.parse(url).getScheme())) ||
                        ("http".equalsIgnoreCase(Uri.parse(url).getScheme()))) {
                    return false;
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception ex) {
                        return true;
                    }
                }
            }


        });
        imageButton_refresh.setOnClickListener(refresh_in_webview);

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    Uri uri = Uri.parse(url); // url为你要链接的地址
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            //获取网站标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
                editText_input.setHint(title);
            }

            //获取加载进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else if (newProgress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setProgress(0);
                }
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                imageButton_search_engine.setImageBitmap(icon);
            }
        });

        //("https://m.bilibili.com/index.html");

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) { // 表示按返回键
                        // 时的操作
                        webView.goBack(); // 后退
                        // webview.goForward();//前进
                        return true; // 已处理
                    }
                }
                return false;
            }
        });

        //activity异常终止时，恢复数据
        if(savedInstanceState != null){
            dark_mode_flag = (int)savedInstanceState.getInt("dark_mode_flag");
            dark_mode_flag = (dark_mode_flag==1?0:1);
            now_url = (String)savedInstanceState.getString("now_url");
            home = (String)savedInstanceState.getString("home");
            search_engine = (String)savedInstanceState.getString("search_engine");
            System.out.println("DARK:"+dark_mode_flag);
            darkMode();
        }

        init_setting();

        if(dark_mode_flag==1){
            dark_mode_flag = 0;
            darkMode();
        }

        webView.loadUrl(now_url);

        //底栏功能实现
        imageButton_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //查看书签时的返回
                if(listview_bookmark.getVisibility() == View.VISIBLE){
                    listview_bookmark.setVisibility(View.GONE);
                }
                else if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });
        imageButton_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });
        imageButton_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listview_bookmark.getVisibility() == View.VISIBLE){
                    listview_bookmark.setVisibility(View.GONE);
                }
                webView.loadUrl(home);
            }
        });
        imageButton_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottom_tool_bar.setVisibility(View.VISIBLE);
                back_dim_layout.setVisibility(View.VISIBLE);
            }
        });

    }

    //透明状态栏的实现
    public void transparentStatusbar(Activity activity){
        if (Build.VERSION.SDK_INT >= 21){
            Window window = activity.getWindow();
            //设置透明状态栏,这样才能让 ContentView 向上
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(Color.TRANSPARENT);
            ViewGroup mContentView = (ViewGroup)activity.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View .
                // 使其不为系统 View 预留空间.
                ViewCompat.setFitsSystemWindows(mChildView, false);
            }
        }
    }

    //状态栏图标黑色的实现
    public void lightStatusbar(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = activity.getWindow().getDecorView();
            int op_light = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(op_light);
        }
    }

    //返回需确认的实现
    public void onBackPressed() {
        doubleBackQuit();
    }

    private void doubleBackQuit() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    //更改搜索引擎的弹出框
    public void showSearchEngineChange() {
        String[] engine_name = new String[]{"百度", "谷歌", "必应", "秘迹搜索"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("搜索引擎");
        String temp;
        builder.setItems(engine_name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        search_engine = "https://m.baidu.com/s?word=";
                        break;
                    case 1:
                        search_engine = "https://www.google.com/search?q=";
                        break;
                    case 2:
                        search_engine = "https://cn.bing.com/search?q=";
                        break;
                    case 3:
                        search_engine = "https://mijisou.com/?q=";
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        Display d = getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (d.getWidth() * 0.5);
        window.setAttributes(params);

    }

    //夜间模式实现，即使全局遮罩可见
    public void darkMode() {
        if (dark_mode_flag == 0) {
            back_dim_layout2.setVisibility(View.VISIBLE);
            /*layout_bottombar.setBackgroundColor(Color.parseColor("#FF000000"));
            imageButton_back.setColorFilter(Color.parseColor("#FFFFFF"));
            imageButton_forward.setColorFilter(Color.parseColor("#FFFFFF"));
            imageButton_home.setColorFilter(Color.parseColor("#FFFFFF"));
            imageButton_setting.setColorFilter(Color.parseColor("#FFFFFF"));*/
            Drawable topDrawable = getResources().getDrawable(R.drawable.nightmode);
            dark_mod.setCompoundDrawablesWithIntrinsicBounds(null,topDrawable,null,null);
            dark_mod.setText("白天模式");
            dark_mode_flag = 1;
            //Toast.makeText(getApplicationContext(), "已切换到夜间模式", Toast.LENGTH_SHORT).show();
        } else {
            back_dim_layout2.setVisibility(View.GONE);
            /*layout_bottombar.setBackgroundColor(Color.parseColor("#FFFFFF"));
            imageButton_back.setColorFilter(Color.parseColor("#FF000000"));
            imageButton_forward.setColorFilter(Color.parseColor("#FF000000"));
            imageButton_home.setColorFilter(Color.parseColor("#FF000000"));
            imageButton_setting.setColorFilter(Color.parseColor("#FF000000"));*/
            Drawable topDrawable = getResources().getDrawable(R.drawable.white_mode);
            dark_mod.setCompoundDrawablesWithIntrinsicBounds(null,topDrawable,null,null);
            dark_mod.setText("夜间模式");
            dark_mode_flag = 0;
            //Toast.makeText(getApplicationContext(), "已切换到白天模式", Toast.LENGTH_SHORT).show();
        }
    }

    //设置主页弹出框
    public void showHomeSet(final View v) {
        String[] home_name = new String[]{"百度","秘迹搜索", "自定义"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("自定义主页");
        builder.setItems(home_name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        home =  "https://www.baidu.com/?tn=simple";
                        break;
                    case 1:
                        home =  "https://mijisou.com/";
                        break;
                    case 2:
                        final EditText inputServer = new EditText(v.getContext());
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("自定义主页").setView(inputServer)
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        home = tourl(inputServer.getText().toString());
                                    }
                                });
                        builder.show();
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //添加书签，向数据库插入
    public void addbookmark() {
        inser_to_bm(webView.getTitle(),webView.getUrl());
        Toast.makeText(getApplicationContext(), "已添加书签", Toast.LENGTH_SHORT).show();
    }


    //将输入的文字与搜索引擎结合成webview可load的链接
    public static String tourl(String input) {
        if (!input.isEmpty()) {
            ToUrl toUrl = new ToUrl();
            input = toUrl.tourl(input, search_engine);
            return input;
        } else
            return null;

    }

    //数据库的初始化及书签表的插入查询
    public void init_db(){
        db_bm = new MyDbHelper(this,"bookmark.db",null,1).getWritableDatabase();
        String sql = "create table if not exists book_mk(_id integer primary key autoincrement,bm_head,bm_url unique)",
                sql2 = "create table if not exists setting(_id integer primary key autoincrement,setting_name unique,setting_data)";
        db_bm.execSQL(sql);
        db_bm.execSQL(sql2);
    }

    public long inser_to_bm(String bm_head,String bm_url){
        ContentValues cv = new ContentValues();
        cv.put("bm_head",bm_head);
        cv.put("bm_url",bm_url);
        long id = db_bm.insert("book_mk",null,cv);
        return id;
    }

    public void query_bm(){

        Cursor c = db_bm.query("book_mk",new String[]{"_id","bm_head","bm_url"},
                null,null,null,null,null);
        if(c==null){
            Toast.makeText(getApplicationContext(), "查询错误", Toast.LENGTH_SHORT).show();
            return;
        }

        listview_bookmark.setVisibility(View.VISIBLE);
        bottom_tool_bar.setVisibility(View.GONE);
        back_dim_layout.setVisibility(View.GONE);

        final SimpleCursorAdapter ca = new SimpleCursorAdapter(this,R.layout.listview_bookmark_item,c,
                new String[]{"bm_head","bm_url"},
                new int[]{R.id.listview_bookmark_name,R.id.listview_bookmark_url},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listview_bookmark.setAdapter(ca);

        listview_bookmark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //webView.loadUrl();
                Cursor item =  (Cursor) parent.getItemAtPosition(position);
                System.out.println("SAFDASD:"+item.getString(2));
                webView.loadUrl(String.valueOf(item.getString(2)));
                listview_bookmark.setVisibility(View.GONE);
            }
        });
        listview_bookmark.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, int position, long id) {
                final Cursor item =  (Cursor) parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(listview_bookmark.getContext());
                String[] long_click = new String[]{"编辑","删除", "设置为主页"};
                builder.setItems(long_click, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                LinearLayout view_edit_bk = new LinearLayout(getApplicationContext());
                                view_edit_bk.setOrientation(LinearLayout.VERTICAL);
                                final EditText et_title = new EditText(getApplicationContext()),
                                        et_url = new EditText(getApplicationContext());
                                et_title.setWidth(100);
                                et_url.setWidth(100);
                                et_title.setSingleLine(true);
                                et_url.setSingleLine(true);
                                view_edit_bk.addView(et_title);
                                view_edit_bk.addView(et_url);
                                et_title.setText(item.getString(1));
                                et_url.setText(item.getString(2));
                                et_title.setSelectAllOnFocus(true);
                                et_url.setSelectAllOnFocus(true);
                                AlertDialog.Builder builder = new AlertDialog.Builder(listview_bookmark.getContext());
                                builder.setTitle("编辑").setView(view_edit_bk)
                                        .setNegativeButton("Cancel", null)
                                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String title = et_title.getText().toString(),
                                                        url = et_url.getText().toString();
                                                System.out.println("this title: "+title+"\n"+url);
                                                if(title!=null && url!=null){
                                                    String sql_editname = "update book_mk set bm_head=\""+title+"\" , bm_url=\""+url+"\" where _id="+item.getString(0);
                                                    db_bm.execSQL(sql_editname);
                                                    Toast.makeText(getApplicationContext(),"编辑成功",Toast.LENGTH_SHORT).show();
                                                    query_bm();
                                                }

                                            }
                                        });
                                builder.show();
                                break;
                            case 1:
                                String sql_delete = "delete from book_mk where _id="+item.getString(0);
                                db_bm.execSQL(sql_delete);
                                Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                query_bm();
                                break;
                            case 2:
                                home = item.getString(2);
                                Toast.makeText(getApplicationContext(),"设置成功",Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                final AlertDialog dialog = builder.create();

                dialog.show();
                return true;
            }
        });

    }

    //插入更新语句
    public void insert_to_settingtable(String name,String data){
        ContentValues cv = new ContentValues();
        cv.put("setting_name",name);
        cv.put("setting_data",data);
        db_bm.insert("setting",null,cv);
    }


    public void replace_setting(String name,String data){
        String sql1 = "UPDATE setting SET setting_data=\""+data+"\" WHERE setting_name=\""+name+"\"";
        db_bm.execSQL(sql1);
    }

    public void init_setting(){
        Cursor c1 = db_bm.rawQuery("select * from setting where setting_name=?",new String[]{"dark_mode_flag"}),
                c2 = db_bm.rawQuery("select * from setting where setting_name=?",new String[]{"now_url"}),
                c3 = db_bm.rawQuery("select * from setting where setting_name=?",new String[]{"home"}),
                c4 = db_bm.rawQuery("select * from setting where setting_name=?",new String[]{"search_engine"});
        if(c1.moveToFirst()){
            dark_mode_flag = Integer.parseInt(c1.getString(2));
        }
        if(c2.moveToFirst()){
            now_url = c2.getString(2);
        }
        if(c3.moveToFirst()){
            home = c3.getString(2);
        }
        if(c4.moveToFirst()){
            search_engine = c4.getString(2);
        }
        System.out.println(dark_mode_flag+"\n"+now_url+"\n"+home+"\n"+search_engine);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //insert_to_setting("dark_mode_flag",String.valueOf(dark_mode_flag));


    }

    //保存数据防止activity意外终止
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("dark_mode_flag",dark_mode_flag);
        outState.putString("now_url",webView.getUrl());
        outState.putString("home",home);
        outState.putString("search_engine",search_engine);

    }

    //onstop保存数据
    @Override
    protected void onStop() {
        super.onStop();

        insert_to_settingtable("dark_mode_flag",String.valueOf(dark_mode_flag));
        insert_to_settingtable("now_url",webView.getUrl());
        insert_to_settingtable("home",home);
        insert_to_settingtable("search_engine",search_engine);

        replace_setting("dark_mode_flag",String.valueOf(dark_mode_flag));
        replace_setting("now_url",webView.getUrl());
        replace_setting("home",home);
        replace_setting("search_engine",search_engine);
    }
}
