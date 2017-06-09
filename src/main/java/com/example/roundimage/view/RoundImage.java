package com.example.roundimage.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Saintyun on 2017/6/6.
 */

public class RoundImage extends ImageView {
//    自定义的圆形头像控件
//    传进来的是图片
//    1.首先用本地图片形式
//    2.再考虑加载网络图片
//  考虑问题有：1.文件的分辨率缩放 2.如何显示 3.获取图片的分辨率 单位？
//    想法：用bitmap类做中转站
//    bitmapfactory
//    初始化相关 获取图片以及相关信息
//    图片id
    private String tag = "Round Image";
    private int imgid;
//  缩放后的长度
//    半径根据此width来的话，有点问题 会出先空白部分，先缩小再放大
    private int width;
//    缩放比例
    private float scale_times;
    //    缩放后的宽度
    private int height;
//    圆心x坐标
    private Bitmap result_bmp;
    private Bitmap prebmp;
    private Context mContext;
    private String uri;
    private byte[] picByte;


    public RoundImage(Context context, int id) {
        super(context);
        mContext = context;
//        imgid = id;
        buildPreBitmap(id);
        Log.i(tag,"自定义 constructor");
    }

    // 加载已知路径图片，如本机的某个文件夹下的图片
    public void setInternetImageUri(Uri uri){

    }

// 不使用轮子手动加载网络图片
    public void setInternetImageStringNoframe(String uri){
        this.uri = uri;
        new Thread(runnable).start();
    }

    Handler handle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if(picByte!=null){
                        prebmp = BitmapFactory.decodeByteArray(picByte,0,picByte.length);
                        Log.e(tag,imgid+" ==handler prebmp===");
                        RoundImage.super.setImageBitmap(prebmp);
                    }
                    break;
            }
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
//                连接成功
                if(conn.getResponseCode()==200){
                    InputStream input = conn.getInputStream();
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] bytes = new byte [1024];
                    int length = -1;
                    while((length = input.read(bytes))!= -1){
                        output.write(bytes,0,length);
                    }
                    picByte = output.toByteArray();
                    output.close();
                    input.close();

                    Message msg = new Message();
                    msg.what = 1;
                    handle.sendMessage(msg);
                    Log.e(tag,imgid+" ==runnable prebmp===");
                }else{
                    Toast.makeText(mContext,"连接错误！"+conn.getResponseMessage(),Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };



//   使用轮子，Imageloader加载网络图片
    public void setInternetImageString(String url){
//        采用默认的配置
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(mContext);
        if(!ImageLoader.getInstance().isInited()){
            ImageLoader.getInstance().init(config);
        }
        //显示图片的配置
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(url,this,options);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        buildPreBitmap(resId);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        prebmp = Bitmap.createBitmap(bm);
    }

    public void init(Bitmap prebmp){
//        应该是扩展到一个
        width = getWidth();
        height = getHeight();
        Log.d("RoundImage","prewidth:"+prebmp.getWidth()+" preheight:"+prebmp.getHeight());
//      这样会产生拉伸效果，
        if((width/prebmp.getWidth())>=(height/prebmp.getHeight())){
            scale_times = height/(float)prebmp.getHeight();
        }else{
            scale_times = width/(float)prebmp.getWidth();
        }
        Log.d("RoundImage","times:"+scale_times+";");
        Matrix matrix = new Matrix();
//        同等程度缩放
//        py,px:缩放的锚点
        matrix.postScale(scale_times,scale_times);
        result_bmp= Bitmap.createBitmap(prebmp,0,0,prebmp.getWidth(),prebmp.getHeight(),matrix,true);
        Log.d("RoundImage","nowwidth:"+result_bmp.getWidth()+" nowheight:"+result_bmp.getHeight());
    }

    private void buildPreBitmap(int id) {
        imgid = id;
        prebmp = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(),imgid));
    }

    public RoundImage(Context context) {
        super(context);
        Log.i(tag,"First constructor");
        mContext = context;
    }

//    默认调用的构造函数
    public RoundImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(tag,"Second constructor");
        mContext = context;
//        for(int i=0;i<attrs.getAttributeCount();i++){
//            Log.d(tag,attrs.getAttributeName(i)+" ; "+attrs.getAttributeValue(i));
//        }
//      @2130903043是地址  imgid是后面的值2130903043
        imgid = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", 0);
        buildPreBitmap(imgid);
    }

    public RoundImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i(tag,"third constructor");
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RoundImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Log.i(tag,"Fourth constructor");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(tag,imgid+" ==ondraw===");
        init(prebmp);
        Bitmap bmp = toRoundBitmap(result_bmp);
        canvas.drawBitmap(bmp,0,0,null);
    }

    public Bitmap toRoundBitmap(Bitmap bmp){
        int bmpwidth,bmpheight;
        float roundPx;
        float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;
//        width与height是xml属性里面的宽高
        bmpwidth = bmp.getWidth();
        bmpheight = bmp.getHeight();
        if (bmpwidth <= bmpheight) {
            roundPx = bmpwidth / 2;
            top = 0;
            bottom = bmpwidth;
            left = 0;
            right = bmpwidth;
            bmpheight = bmpwidth;
            dst_left = 0;
            dst_top = 0;
//            等于半径
            dst_right = bmpwidth;
            dst_bottom = bmpwidth;
        } else {
            roundPx = bmpheight / 2;
            float clip = (bmpwidth - bmpheight) / 2;
            left = clip;
            right = bmpwidth - clip;
            top = 0;
            bottom = bmpheight;
            bmpwidth = bmpheight;
//            这个可以调节的话，根据相对于画布大小来，也就是定义的宽高来，可以设置从中心来进行圆头像
            dst_left = 0;
            dst_top = 0;
//            等于半径
            dst_right = bmpheight;
            dst_bottom = bmpheight;
        }
        Bitmap output = Bitmap.createBitmap(bmpwidth,
                bmpheight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
        final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom);
        final RectF rectF = new RectF(dst);
//        抗锯齿 true
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bmp, src, dst, paint);
//        放大output
        Log.e(tag,width/output.getWidth()+" ; "+height/output.getHeight()+"==lalal==");
        Matrix matrix = new Matrix();
        matrix.postScale(width/(roundPx*2),height/(roundPx*2));
        Bitmap bitmap = Bitmap.createBitmap(output,0,0,output.getWidth(),output.getHeight(),matrix,true);
        return bitmap;
    }
}
