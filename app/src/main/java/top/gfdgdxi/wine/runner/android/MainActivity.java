package top.gfdgdxi.wine.runner.android;

import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import top.gfdgdxi.wine.runner.android.databinding.ActivityMainBinding;
import top.gfdgdxi.wine.runner.android.PRoot;

public class MainActivity extends AppCompatActivity {
    protected void hideBottomUIMenu() {
        // 设置应用为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  //设置屏幕为横屏, 设置后会锁定方向
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 设置隐藏标题栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        WindowManager.LayoutParams lp = MainActivity.this.getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        MainActivity.this.getWindow().setAttributes(lp);

    }
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        hideBottomUIMenu();
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String packageName = "top.gfdgdxi.wine.runner.android";
            String command = "pm grant " + packageName + " android.permission.RUN_INSTRUMENTATION" ;
            try {
                Process process = Runtime.getRuntime().exec(command); // 等待命令执行完成
                process.waitFor();
            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /*WindowManager.LayoutParams lp = MainActivity.this.getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        MainActivity.this.getWindow().setAttributes(lp);*/


        // 设置版本号
        /*TextView versionShow = findViewById(R.id.versionShow);
        try {
            versionShow.setText("版本号：" + MainActivity.this.getPackageManager().getPackageInfo(MainActivity.this.getPackageName(),0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            versionShow.setText("版本号：内部版本");
            e.printStackTrace();
        }*/
        // 设置 WebView 属性
        WebView webView1 = findViewById(R.id.systemGUI);
        WebSettings webSettings = webView1.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);  // 解决问题 Cannot read property getItem of null
        // 设置不允许选中文本
        webView1.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });



        // 判断系统是否释放
        // 如果没有
        PRoot proot = new PRoot();
        if(!proot.IsSystemInstalled(MainActivity.this)) {
            webView1.loadUrl("file:///android_asset/UnpackEnvironment/index.html");
            webView1.evaluateJavascript("javascript:UpdateInfo('1.1.0')", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
            UnpackSystem unpackSystem = new UnpackSystem();
            unpackSystem.start();
        }
        else {
            proot.UnpackEnvironment(MainActivity.this);
            proot.SetVNCPasswd(MainActivity.this, "123456");
            RunSystem();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        hideBottomUIMenu();
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        hideBottomUIMenu();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public boolean telnetPort(String ip, int port)
    {
        Socket socket = new Socket();
        boolean res = false;
        try {
            socket.connect(new InetSocketAddress(ip, port), 100);
            res = socket.isConnected();
        } catch (IOException e) {
            res = false;
        }
        return res;
    }



    public void RunSystem()
    {
        // 设置加载页
        WebView webView1 = findViewById(R.id.systemGUI);
        webView1.post(() -> {
            webView1.loadUrl("file:///android_asset/LoadHTML/index.html");
            webView1.evaluateJavascript("javascript:UpdateInfo('1.1.0')", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        });
        // 加载系统
        LoadSystem loadSystem = new LoadSystem();
        loadSystem.start();
        CheckPort checkPort = new CheckPort();
        checkPort.start();
    }

    class CheckPort extends Thread {
        @Override
        public void run()
        {
            WebView webView = findViewById(R.id.systemGUI);
            while (true) {
                if(telnetPort("127.0.0.1", 6080)) {
                    // 设置 NoVNC
                    webView.post(() -> {
                        webView.loadUrl("http://127.0.0.1:6080/vnc.html");
                    });
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

    class LoadSystem extends Thread {
        @Override
        public void run()
        {
            PRoot proot = new PRoot();
            // 获取屏幕分辨率
            WindowManager windowManager = getWindow().getWindowManager();
            Point point = new Point();
            DisplayMetrics metrics = MainActivity.this.getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            // 保证 width > height
            if(height > width) {
                int temp = width;
                width = height;
                height = temp;
            }
            proot.Loging(MainActivity.this, (int) (width), (int) (height));
        }
    }

    class PRootShowInfoToWebView extends PRoot {
        // 使用队列存储数据
        Queue<String> commandResult = new LinkedList<>();
        WebViewRefresh webViewThread;
        LogcatCout logcatThread;
        String nowResult;
        boolean stopThread = false;
        @Override
        public void Cout(String data)
        {
            // 不直接输出至 WebView 以提升性能
            // 存储至队列
            Log.d("RunCommand", data);
            nowResult = data;
            commandResult.offer(data);
        }

        public void StartThread()
        {
            stopThread = false;
            // 开启刷新线程
            webViewThread = new WebViewRefresh();
            webViewThread.start();
            logcatThread = new LogcatCout();
            logcatThread.start();
        }

        public void StopThread()
        {
            stopThread = true;
        }
        class LogcatCout extends Thread {
            @Override
            public void run()
            {
                while(true) {
                    // 刷新次数：20次/s
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // 判断队列内是否有数据
                    if (commandResult.isEmpty()) {
                        if(stopThread) {
                            break;
                        }
                        continue;
                    }
                    // 读取数据
                    /*String data = commandResult.poll();
                    Log.d("RunCommand", data);*/
                }
            }
        }
        // 另外开启一个线程以处理输出问题
        class WebViewRefresh extends Thread {
            @Override
            public void run()
            {
                while (true) {
                    // 刷新次数：20次/s
                    try {
                        Thread.sleep(90);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if(stopThread) {
                        break;
                    }
                    WebView webView1 = findViewById(R.id.systemGUI);
                    webView1.post(() -> {
                        webView1.evaluateJavascript("javascript:SetUnpackData('" + nowResult + "')", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {}});
                    });
                }
            }
        }
    }

    class UnpackSystem extends Thread {
        @Override
        public void run()
        {
            WebView webView1 = findViewById(R.id.systemGUI);
            PRootShowInfoToWebView systemConfig = new PRootShowInfoToWebView();
            systemConfig.StartThread();
            systemConfig.CleanTempFile(MainActivity.this);
            // 解压文件
            webView1.post(() -> {
                webView1.evaluateJavascript("javascript:SetUnpackData('解压核心文件')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {}});
            });
            if(!systemConfig.IsEnvironmentInstalled(MainActivity.this)) {
                // 不重复安装
                systemConfig.UnpackEnvironment(MainActivity.this);
            }
            webView1.post(() -> {
                webView1.evaluateJavascript("javascript:SetUnpackData('解压资源文件')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {}}
                );
            });
            if(!systemConfig.IsSystemInstalled(MainActivity.this)) {
                // 不重复安装
                systemConfig.UnpackSystem(MainActivity.this);
                systemConfig.SetVNCPasswd(MainActivity.this, "123456");
            }
            systemConfig.StopThread();
            RunSystem();
        }
    }
}