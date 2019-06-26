package com.example.admin.activitytest;

public class queue {

    int front=0;// 队列头，允许删除
    int rear=0;// 队列尾，允许插入
    int LENGTH=5;
    int full=0;//判断队列是否已满
    int t=0;
    float[] data  = new float[LENGTH];// 队列


    // 入队
    public void offer(float date) {
        if (rear<LENGTH)
        {
            data[rear++] = date;
        }
        else
        {
            rear=0;//等于LENGTH时清零
        }

        if (t++>=LENGTH)
            full=1;
    }
    // 出队
    public float poll() {

        if (front<LENGTH)
        {
            float value = data[front];// 保留队列的front端的元素的值

            front++;
            return value;

        }
        else
        {
            front=0;
            float value = data[front];
            return  value;
        }
    }

    public boolean full()
    {
        if (full==1)
            return  true;
        else
            return false;
    }
}


