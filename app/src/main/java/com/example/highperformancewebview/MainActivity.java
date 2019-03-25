package com.example.highperformancewebview;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;


/**
 * WebView learn demo
 * @author jingang.li
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG=MainActivity.class.getSimpleName();
    private WebView mWebView;
    private Button mButton;
    private int mCurrentState=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 一、WebView对象初始化
        mWebView=findViewById(R.id.web_view);
        mButton=findViewById(R.id.button);
        // 二、WebView设置
        initWebViewSetting();
        // 三、添加WebView通知，请求等事件监听器
        setWebViewEventListener();
        // 四、添加WebView与js交互监听器
        setWebViewAndJsEventListener();
        // 五、添加Js调用Android接口
        addJavascriptInterface();
        // 六、加载url
        mWebView.loadUrl("file:///android_asset/www/test.html");
        // 七、添加Android调用JS方法
        addAndroidCallJsMethod();

    }

    /**
     * 添加Android调用js方法
     */
    private void addAndroidCallJsMethod() {

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentState){
                    case 0:
                        // 方法一 Android调用没有参数的JS方法
                        Log.v(TAG,"AndroidCallJsMethod one without parameter");
                        mWebView.loadUrl("javascript:showHello()");
                        mCurrentState=(mCurrentState+1)%3;
                        break;
                    case 1:
                        // 方法一 Android调用有参数的JS方法
                        Log.v(TAG,"AndroidCallJsMethod one with parameter");
                        mWebView.loadUrl("javascript:showString('这是Android调用JS的方法展示')");
                        mCurrentState=(mCurrentState+1)%3;
                        break;
                    case 2:
                        // 方法二
                        Log.v(TAG,"AndroidCallJsMethod two with return");
                        mWebView.evaluateJavascript("javascript:returnedJsShowString('a1a')",
                                new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                //value为 js 返回的值
                                Log.v(TAG,"onReceiveValue:"+value);

                            }
                        });
                        mCurrentState=(mCurrentState+1)%3;
                        break;
                        default:
                            // 这句话的目的是可以让mCurrentState 值在0、1、2、3之间切换
                            mCurrentState=(mCurrentState+1)%3;
                }

            }
        });


    }


    /**
     * 初始化设置WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSetting() {
        // WebView也是一个view,所以可以设置它的背景 View可以理解为 一幅画 =背景+前景。
        mWebView.setBackgroundColor(Color.RED);

        
        //一些设置值放在了一个统一的地方、形成一个新的类：WebSettings

        // 使WebView可以运行Js
        // 注意：如果设置了这个选项可能导致引入xss漏洞
        // 所以如没有必须，请不要设置为true、默认false
        mWebView.getSettings().setJavaScriptEnabled(true);
        //设置WebView是否支持缩放，默认就是true
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        //设置JS是否可以自己自动打开新的页面弹窗，默认false
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置用户代理字符串，用户代理字符串就是告诉服务端现在我用的啥样的浏览器，以及浏览器的
        // 运行环境，服务端会根据不同的浏览器返回不同的网页内容，Android 早期的浏览器内核都是
        // Linux; Android 4.4.2; Lenovo K910e Build/KOT49H) AppleWebKit/537.36
        Log.v(TAG,"mWebView.getUserAgentString="+mWebView.getSettings().getUserAgentString());
        mWebView.getSettings().setUserAgentString(null);

    }

    /**
     *
     */
    private void setWebViewEventListener() {
        // 添加WebViewClient ，从名字上看，Client:委托人
        // 它的作用是处理各种WebView请求、通知事件回调
        mWebView.setWebViewClient(new WebViewClient(){
            /**
             *  当页面开始加载时候调用，注意这个方法 只会在主帧调用时候被调用一次
             *  如果一个页面有好多个frame-就是frameSet，那么这个方法只在 main frame加载时候生效
             *  可以设定frameSet集合中 某一个frame为main Frame： 设置方式是属性设定为main
             *  比如设置导航栏设置为mainFrame，当点击
             *  导航栏时候 分frame是会变化的，但是onPageStart就不会执行
             * @param view 当前WebView对象
             * @param url 当前url
             * @param favicon 网站图标
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.v(TAG,"onPageStarted");
                // 这个方法，设置是否阻断网络图片下载，等下载完毕，再取消阻断，然后WebView会自动下载
                // 个人觉得在onPageStarted 开始设置就好，容易形成对称。
                mWebView.getSettings().setBlockNetworkImage(true);
            }

            /**
             * 当主页面加载完毕时候调用
             * 注意事项看 onPageStarted的注释。
             * @param view 当前WebView对象
             * @param url v
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.v(TAG,"onPageFinished");
                // 取消阻断网络图片的下载，当从true变为false,WebView会自动下载图片
                mWebView.getSettings().setBlockNetworkImage(false);
            }

            /**
             * Override，重载、替代 方法名意思是：替代url加载
             * 在当前url里面发起一个新请求的时候，有以下三种处理方式
             * 1 如果没有设定WebViewClient，系统Activity Manager会选择处理器来处理这个url
             * 一般选择系统默认的浏览器加载。
             * 2 如果设定了WebViewClient，那么就会执行这个方法，如果这个方法返回true，表示用户要自己
             * 处理了这个url的加载，其他人就不要管理
             * 3 如果方法返回false，那就代表用户没有处理，那么就需要当前WebView来处理，WebView处理
             * 方式就是加载该url
             * 我们看到该方法的super方法，就是返回false，那其实只要我们向WebView里面添加一个
             * WebClient对象，WebView就会默认处理。
             * @param view 设置了该WebClient的WebView对象
             * @param request 就是包含请求url的具体Request对象
             * @return true or false 意义见上面说明
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

    }

    /**
     * WebChromeClient是辅助WebView处理Javascript的对话框，
     * 网站图标，网站title，加载进度等偏外部事件
     * 它的本质是与WebClient是一样的 提供Android了解h5加载情况，处理情况的一些回调。
     */
    private void setWebViewAndJsEventListener() {
        mWebView.setWebChromeClient(new WebChromeClient(){
            /**
             * 页面加载的进度发生改变时回调，用来告知主程序当前页面的加载进度。
             * 加载进度的统计，如何进行呢？那是另外一个问题啦~哈哈
             * @param view 添加该监听器的WebView
             * @param newProgress 当前页面加载进度：0-100
             */
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Log.v(TAG,"view="+view.getOriginalUrl()+" ,newProgress="+newProgress);
            }
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }
            /**
             * 通知当前加载网页的Title，可以用来设置Activity 标题什么的
             * @param view 添加该监听器的WebView
             * @param title  网页的Title
             */
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                // 类似onReceivedTitle
                super.onReceivedIcon(view, icon);
            }
            /**
             * 该方法在当前页面进入全屏模式时回调,页面要想实现全屏，（比如播放视频），
             * 如果不实现这两个方法，该web上的内容便不能进入全屏模式。
             * @param view 添加该监听器的WebView
             * @param callback  invoke this callback to request the page to exit
             * full screen mode.
             */
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
            /**
             * 通知应用当前页面退出全屏模式
             */
            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
            }

            /**
             * 当视频没有播放时候，会放一个封面-海报
             * 当用户没有设置这个海报的时候，系统会添加一个默认的，这个方法就是返回
             * 这个默认的封面图。
             * 注意：当我们的Web页面包含视频时，我们可以在HTML里为它设置一个预览图，WebView会在
             * 绘制页面时根据它的宽高为它布局。而当我们处于弱网状态下时，我们没有比较快的获取该图片，
             * 那WebView绘制页面时的getWidth()方法就会报出空指针异常~ 于是app就crash了：这种情况待验证
             * @return 返回默认的海报
             */
            @Nullable
            @Override
            public Bitmap getDefaultVideoPoster() {
                return super.getDefaultVideoPoster();
            }
            /**
             * 处理Javascript中的Alert对话框、如果返回true WebChromeClient自己处理，js就不用管了
             * 如果是false，JS会继续处理
             * @param view WebView对象
             * @param url 请求dialog的url
             * @param message dialog中的消息
             * @param result A JsResult to confirm that the user hit enter.具体自己写代码验证下：
             * @return true or false
             */
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
    }

    private void addJavascriptInterface() {
       mWebView.addJavascriptInterface(new DemoClass(),"demoClass");
    }

    private class DemoClass {

        @JavascriptInterface
        public void showLog(){
            Toast.makeText(MainActivity.this,
                    "JS调用Android",Toast.LENGTH_LONG).show();
            Log.v(TAG,"showLog- running");
        }


    }
}
