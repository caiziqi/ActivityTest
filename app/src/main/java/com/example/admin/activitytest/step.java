package com.example.admin.activitytest;

//实现计步算法，原理，检测波峰，当波峰超过阀值且满足一定的条件，当做一步。
public class step {
    public   int step=0;//步数
    private final int num = 5;//数组大小
    private float[] diffValue = new float[num];//存放波峰波谷差值
    private int diffCount = 0;//实际波峰波谷差值的数量
    private  boolean Up = false; //波形上升标志位
    private  int UpCount = 0;  //上升次数
    private  int UpCount_before = 0;    //上一点的持续上升的次数
    private  int DownCount_before=0;    //上一次的持续下降次数
    private  int DownCount=0;            //下降次数
    private  boolean State_befor = false; //上一点的状态，上升还是下降
    private   float peak = 0;    //波峰值
    private  float valley = 0;    //波谷值
    private  float H = 0;    //波谷波谷差值
    private   long peaktime_now = 0; //此次波峰的时间
    private  long peaktime_before = 0;//上次波峰的时间
    private  long timeOfNow = 0;    //当前的时间
    private   float sensor_old = 0;    //上次传感器的值

    //控制参数
    private    final float init_limit_value = (float) 2;    //初始阈值
    private   float Auto_limit_value = (float) 4;//动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    private  final  int PEAK_TIME_DIFF=400;//满足条件--两个波峰的时间差
    private  final  int UP_DOWN_COUNT=5;//满足条件--上升或者下降连续次数

    /*增加********************/
    private  long time_Interval = 0;    //两步的间隔时间
    private  long step_freq = 0;
    private double step_length=0;
    /*********************/
    //波峰检测
    /*    传入当前值和上一次的值，如果当前值大于上次值则波型为上升状态，修改状态值，否者为下降，
     * 修改状态值。如果当前为下降，之前为上升则为波峰，如果当前为上升之前为下降则为波谷。将波峰波谷值进行记录。
     */
    private boolean check_peak(float newValue, float oldValue)
    {
        State_befor = Up;//保存上次的状态
        if (newValue >= oldValue)//上升
        {
            Up = true;
            UpCount++;
            DownCount_before=DownCount;
            DownCount=0;

        }
        else//下降
        {
            UpCount_before = UpCount;
            UpCount = 0;

            DownCount++;
            Up = false;
        }

        if (!Up && State_befor && (UpCount_before >= UP_DOWN_COUNT || oldValue >= 25)) //当前下降之前上升为波峰
        {
            peak = oldValue;
            return true;
        } else if (!State_befor && Up&&DownCount_before>=UP_DOWN_COUNT)//当前上升之前下降为波谷
        {
            valley = oldValue;
            return false;
        } else
        {
            return false;
        }
    }

    /*测步
     * 如果是第一次测量，将当前值赋值给上一此的值，跳过不测量。
     * 否者，如果检测到波峰且满足两次波峰时间差大于某值，波峰波谷差值大于动态阀值则计做一次步。
     * 如果波峰波谷差值大于初始阀值则进行动态阀值的更新。
     */
    public void step_run(float values) {
        if(sensor_old != 0)
        {
            if (check_peak(values, sensor_old)) //检测到波峰
            {
                peaktime_before = peaktime_now;
                timeOfNow = System.currentTimeMillis();//系统时间毫秒数
                if (timeOfNow - peaktime_before >= PEAK_TIME_DIFF && (peak - valley >= Auto_limit_value))
                {
                    /*********/
                    //两步之间的间隔时间，以毫秒为单位
                    time_Interval=timeOfNow-peaktime_now;
                    /*********/
                    peaktime_now = timeOfNow;//将当前时间定为当前波峰时间
                    H=peak - valley;

                    step_length=0.37f-0.000155f*time_Interval+0.1638f*(float) Math.sqrt(H);
                    if(step_length>1.5|step_length<-1.5)//防止数值过大
                        step_length=0;
                    else
                        step++;
                }
                if (timeOfNow - peaktime_before >= PEAK_TIME_DIFF && (peak - valley >= init_limit_value))
                {
                    peaktime_now = timeOfNow;
                    Auto_limit_value = limit_value_update(peak - valley);//更新动态阈值
                }
            }
        }
        sensor_old = values;
    }

    /*动态阀值更新
     * 如果动态阀值数组没有满，则进行保存，返回原始动态阀值。
     * 否则，将数组值进行替换返回更新后的动态法制…阀值*/
    private float limit_value_update(float value) //传入波峰与波谷的差值
    {
        float temp = Auto_limit_value;
        if (diffCount < num)
        {
            diffValue[diffCount] = value;
            diffCount++;
        } else
        {
            temp = averageValue(diffValue, num);
            for (int i = 1; i < num; i++)
            {
                diffValue[i - 1] = diffValue[i];
            }
            diffValue[num - 1] = value;
        }
        return temp;

    }

    /*阀值梯度
     * 动态配置阀值的梯度，调节感应的灵敏度。
     */
    private float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / n;
/*        if (ave >= 8)
            ave = (float) 6.5;
        else if (ave >= 7 && ave < 8)
            ave = (float) 5.5;
        else if (ave >= 6 && ave < 7)
            ave = (float) 4.5;
        else {
            ave = (float) 3.5;
        }*/
        if (ave >= 8) {
            ave = (float) 4.3;
        } else if (ave >= 7 && ave < 8) {
            ave = (float) 3.3;
        } else if (ave >= 4 && ave < 7) {
            ave = (float) 2.3;
        } else if (ave >= 3 && ave < 4) {
            ave = (float) 2.0;
        } else {
            ave = (float) 1.7;
        }
        return ave;
    }
    //输入sma处理后的加速度
    public void step_init(float x_,float y_,float z_){
        float temp = (float) Math.sqrt(x_ * x_ + y_ * y_ + z_ * z_);
        step_run(temp);
    }
    //获取步数
    public int getstep() {
        return step;
    }

    /*增加**********************/
    //获取步长
    public double getlength() {
/*        step_freq=1000/time_Interval;
        if (step_freq<=1.35)
            step_length=0.4375;
        else if(step_freq<=2.45)
            step_length=0.45*step_freq-0.17;
        else step_length=0.9325;
*/

        return step_length;
    }
    /***********************/
}
