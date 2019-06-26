package com.example.admin.activitytest;



//x,y,z为某时刻的加速度
//x_,y_,z_为输出的平滑的数据，将这些数据用于后期计算
public class sma {
    private float threshold=220;//加速度阈值，防止抖动，大于这个值不入队
    private queue queue_x;
    private queue queue_y;
    private queue queue_z;
    float max_x;
    float max_y;
    float max_z;
    int LENGTH=5;
    sma()
    {
        queue_x=new queue();
        queue_y=new queue();
        queue_z=new queue();
    }

    public boolean ifture()//判断是否大于length
    {
        if (queue_x.full()&&queue_y.full()&&queue_z.full())
        {
            return  true;
        }
        else
        {
            return  false;
        }
    }
    //队列未满时将加速度数据入队
    public  void  sma_init(float x,float y,float z)
    {
        if((float)(x * x + y * y + z * z)<threshold){
            max_x+=x;
            max_y+=y;
            max_z+=z;
            queue_z.offer(z);
            queue_x.offer(x);
            queue_y.offer(y);
        }

    }

    public void sma_run(float x ,float y,float z)//满足==length执行
    {
        max_x-= queue_x.poll();
        queue_x.offer(x);
        max_x+= x;

        max_y-=queue_y.poll();
        queue_y.offer(y);
        max_y+=y;

        max_z-=queue_z.poll();
        queue_z.offer(z);
        max_z+= z;

    }

    public float getx() {
        return max_x/LENGTH;
    }

    public float gety() {
        return max_y/LENGTH;
    }

    public float getz() {
        return max_z/LENGTH;
    }
}

/*********************************
 * 例
 *      if (sma1.ifture())
 {
 sma1.sma_run(x,y,z);
 float x_=sma1.getx();
 float y_=sma1.gety();
 float z_=sma1.getz();
 输出("x:   "+x_+"y:    "+y_+"z:    "+z_+"\n");
 }
 else
 {
 sma1.sma_init(x,y,z);
 }

 */

