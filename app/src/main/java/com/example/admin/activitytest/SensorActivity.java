package com.example.admin.activitytest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mySensor;
    private float[] jia_Values = new float[3];
    private float[] di_Values = new float[3];
    TextView text1,text2,text3;
    TextView textfx1;

    TextView textlength,textstep;
//GPS
    TextView GPS_show;
    TextView newlocation;
    private MapView mMapView = null;
    private BaiduMap baiduMap;
    public LocationClient mLocationClient;
    private boolean isFirstLocate=true;
    private double latitude,longitude;
    /** 地球半径 **/
    private static final double R_dq = 6371e3;
    /** 180° **/
    private static final DecimalFormat df = new DecimalFormat("0.000000");
    private double[] new_start=new double[2];

//画图GPS轨迹
    List<LatLng> points = new ArrayList<LatLng>();
    OverlayOptions mOverlayOptions;

    private  long timeOfNow = 0;//时间
    //方向
    private double new_angle;
    int counter=0;
    float[] temp = new float[3];
    //计算步数
    private int stepnum;
    private int stepnum_before;
    private sma sma1 = new sma();
    private step step1 = new step();
    private float x_, y_, z_;
    private double length;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());


        setContentView(R.layout.activity_sensor);
        mMapView = (MapView) findViewById(R.id.map_view);
        baiduMap=mMapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        text1=(TextView)findViewById(R.id.text1);
        textfx1=(TextView)findViewById(R.id.textfx);
        textstep=(TextView)findViewById(R.id.textstep);
        textlength=(TextView)findViewById(R.id.textlength);
        GPS_show = (TextView) findViewById(R.id.textGPS);
        newlocation=(TextView)findViewById(R.id.newlocation);
        //获得系统中的加速度传感器
        mySensor = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        save("TYPE_ACCELEROMETER"+"\r\n");
        temp[0]=0;temp[1]=0;temp[2]=0;
        save("\r\n"+"gg"+"\r\n"+"\r\n"+"\r\n");

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*                LatLng p1 = new LatLng(22.535979,113.94858);
                LatLng p2 = new LatLng(23.535979,116.94858);
                LatLng p3 = new LatLng(24.535979,115.94858);
                points.add(new LatLng(22.535979,113.94858));
                points.add(p2);
                points.add(p3);*/
//                points.add(new LatLng(new_start[1],new_start[0]));
                if (stepnum>2){
                    mOverlayOptions = new PolylineOptions()
                            .width(10)
                            .color(0xAAFF0000)
                            .points(points);
                    Overlay mPolyline = baiduMap.addOverlay(mOverlayOptions);
                }else{
                    Toast.makeText(SensorActivity.this,"stepnum<3",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save("ERROR"+"\r\n");
                save(stepnum+" "+length+" "+new_angle+" "+new_start[0]+" "+new_start[1]+"\r\n");

            }
        });
        Button button0 = (Button) findViewById(R.id.button0);
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                points.set(0,new LatLng(latitude,longitude));
                Toast.makeText(SensorActivity.this,latitude+" "+longitude,Toast.LENGTH_SHORT).show();
            }
        });
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            requestLocation();
        }


    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setOpenGps(true);
        option.setLocationNotify(true);
//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setScanSpan(1000);
        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认GCJ02
//GCJ02：国测局坐标；
//BD09ll：百度经纬度坐标；
//BD09：百度墨卡托坐标；
//海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
//        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "请同意所有权限", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch(type){
            case Sensor.TYPE_ACCELEROMETER :{
                jia_Values = event.values.clone();
                text1.setText("加速度  "+event.values[0]+"  "+event.values[1]+"  "+event.values[2]);
//                timeOfNow = System.currentTimeMillis();
                String msg=event.values[0]+"  "+event.values[1]+"  "+event.values[2]+"\r\n";
                save(msg);
                if(isFirstLocate == false) {
                    if (sma1.ifture()) {
                        sma1.sma_run(jia_Values[0], jia_Values[1], jia_Values[2]);
                        x_ = sma1.getx();
                        y_ = sma1.gety();
                        z_ = sma1.getz();
                        step1.step_init(x_, y_, z_);
                    } else {
                        sma1.sma_init(jia_Values[0], jia_Values[1], jia_Values[2]);
                    }
                    stepnum_before = stepnum;
                    stepnum = step1.getstep();
                    if (stepnum_before < stepnum) {
                        length = step1.getlength();
                        textstep.setText("步数： " + stepnum + " ");
                        textlength.setText("步长： " + Math.abs(length) + " m" + " ");
                        // devicevalues[0]
                        new_start = calLocationByDistanceAndLocationAndDirection(new_angle, new_start[0], new_start[1], length);
                        //double angle, double startLong,double startLat, double distance
                        //points.add(new LatLng(new_start[0],new_start[1]));
                        newlocation.setText("新坐标： " + new_start[0] + " " + new_start[1] + " ");
                        points.add(new LatLng(new_start[1], new_start[0]));
//                        save(stepnum + " " + length + " " + new_angle + " " + new_start[0] + " " + new_start[1] + "\r\n");
                    }
                }

                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD:{
                di_Values = event.values.clone();
                break;
            }
            default:
                break;
        }calculateOrientation();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        /**
         * 第三个参数决定传感器信息更新速度
         * SensorManager.SENSOR_DELAY_NORMAL:一般
         * SENSOR_DELAY_FASTEST:最快
         * SENSOR_DELAY_GAME:比较快,适合游戏
         * SENSOR_DELAY_UI:慢
         */
        mySensor.registerListener(this, mySensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mySensor.registerListener(this, mySensor.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        /*        Sensor.TYPE_ORIENTATION：方向传感器。
        Sensor.TYPE_ACCELEROMETER：加速度传感器。
        Sensor.TYPE_LIGHT：光线传感器。
        Sensor.TYPE_MAGNETIC_FIELD：磁场传感器。
        Senor.TYPE_GRAVITY:重力传感器
        Sensor.TYPE_ROTATION_VECTOR 旋转矢量传感器
        */
         mMapView.onResume();

    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (points != null)
            points.clear();
        if (mySensor != null)
            mySensor.unregisterListener(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (points != null)
            points.clear();
        mMapView.onDestroy();
        MapView.setMapCustomEnable(false);
        mMapView = null;
        mLocationClient.stop();
        baiduMap.setMyLocationEnabled(false);
    }

/*        values[0]  表示Z轴的角度：方向角，我们平时判断的东西南北就是看这
个数据的也就是说数据范围是（-180～180）,也就是说，0表示正北，90表示正东，180/-180表示正南，-90表示正西。
        values[1]  表示X轴的角度：俯仰角   即由静止状态开始，前后翻转
        values[2]  表示Y轴的角度：翻转角  即由静止状态开始，左右翻转
*/
    private void calculateOrientation()
    {
        float[] devicevalues = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, jia_Values,  di_Values);
        SensorManager.getOrientation(R, devicevalues);
        devicevalues[0] = (float) Math.toDegrees(devicevalues[0]);//转换为角度
        devicevalues[1] = (float) Math.toDegrees(devicevalues[1]);//转换为角度
        devicevalues[2] = (float) Math.toDegrees(devicevalues[2]);//转换为角度
        if (counter >=15) {
            for (int i=0;i<3;i++){devicevalues[i]=temp[i]/counter;temp[i]=0;}
            new_angle=devicevalues[0];
            counter=0;
            if (devicevalues[0] >= -5 && devicevalues[0] < 5) {
                textfx1.setText("方向：正北  " + devicevalues[0]+ "  "+devicevalues[1]+ "  "+ devicevalues[2]);
            } else if (devicevalues[0] >= 5 && devicevalues[0] < 85) {
                textfx1.setText("方向：东北  " + devicevalues[0]+ "  "+devicevalues[1]+ "  "+ devicevalues[2]);
            } else if (devicevalues[0] >= 85 && devicevalues[0] <= 95) {
                textfx1.setText("方向：正东  " + devicevalues[0]+ "  "+devicevalues[1]+ "  "+ devicevalues[2]);
            } else if (devicevalues[0] >= 95 && devicevalues[0] < 175) {
                textfx1.setText("方向：东南  " + devicevalues[0]+ "  "+devicevalues[1]+ "  "+ devicevalues[2]);
            } else if ((devicevalues[0] >= 175 && devicevalues[0] <= 180) || (devicevalues[0]) >= -180 && devicevalues[0] < -175) {
                textfx1.setText("方向：正南  " + devicevalues[0]+ "  "+devicevalues[1]+ "  "+ devicevalues[2]);
            } else if (devicevalues[0] >= -175 && devicevalues[0] < -95) {
                textfx1.setText("方向：西南  " + devicevalues[0]+ "  "+devicevalues[1]+ "  "+ devicevalues[2]);
            } else if (devicevalues[0] >= -95 && devicevalues[0] < -85) {
                textfx1.setText("方向：正西  " + devicevalues[0]+ "  "+devicevalues[1]+ "  "+ devicevalues[2]);
            } else if (devicevalues[0] >= -85 && devicevalues[0] < -5) {
                textfx1.setText("方向：西北  " + devicevalues[0]+ " "+devicevalues[1]+ " "+ devicevalues[2]);
            }
        }
        else{
            counter++;
            temp[0]+=devicevalues[0];temp[1]+=devicevalues[1];temp[2]+=devicevalues[2];
        }
    }


    private void navigateTo(BDLocation location){
        if(isFirstLocate) {
            new_start[1]=location.getLatitude();
            new_start[0]=location.getLongitude();
            points.add(new LatLng(new_start[1],new_start[0]));
            newlocation.setText("新坐标： "+new_start[0]+" "+new_start[1]);
//            save("0"+" "+"0"+" "+"0"+" "+new_start[0]+" "+new_start[1]+"\r\n");
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(18f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f
            StringBuilder sb=new StringBuilder();
            String coorType;
            if (location.getLocType()==BDLocation.TypeGpsLocation){
                coorType="GPS";
                navigateTo(location);
            }else if(location.getLocType()==BDLocation.TypeNetWorkLocation){
                coorType="net";
                navigateTo(location);
            }else{
                coorType="null";
            }
            sb.append(longitude).append("  ").append(latitude).append("  ").append(coorType);
            GPS_show.setText(sb);
        }
    }

    public void save(String msg){
        if(msg == null) return;
        try {
            // 步骤2:创建一个FileOutputStream对象,MODE_PRIVATE覆盖，MODE_APPEND追加模式
            FileOutputStream fos = openFileOutput("message.txt",MODE_APPEND);
            // 步骤3：将获取过来的值放入文件
            fos.write(msg.getBytes());
            // 步骤4：关闭数据流
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double[] calLocationByDistanceAndLocationAndDirection(double angle, double startLong,double startLat, double distance){
        double[] result = new double[2];
        //将距离转换成经度的计算公式
//        angle=(angle+360)%360;
        distance=Math.abs(distance);
        double δ = distance/R_dq;
        // 转换为radian，否则结果会不正确
        angle = Math.toRadians(angle);
        startLong = Math.toRadians(startLong);
        startLat = Math.toRadians(startLat);
        double lat = Math.asin(Math.sin(startLat)*Math.cos(δ)+Math.cos(startLat)*Math.sin(δ)*Math.cos(angle));
        double lon = startLong + Math.atan2(Math.sin(angle)*Math.sin(δ)*Math.cos(startLat),Math.cos(δ)-Math.sin(startLat)*Math.sin(lat));
        // 转为正常的10进制经纬度
        lon = Math.toDegrees(lon);
        lat = Math.toDegrees(lat);
        result[0] =Double.valueOf(df.format(lon).toString());
        result[1] =Double.valueOf(df.format(lat).toString());
 //       result[0] = df.format(lon);
 //       result[1] = df.format(lat);
        return result;
    }
}


