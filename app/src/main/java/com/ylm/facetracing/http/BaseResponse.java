package com.ylm.facetracing.http;
/*
 * Created by Yanlm on 2020/12/8.
 */

public class BaseResponse<T> {
    private int errno; // 返回的code
    private T data; // 具体的数据结果
    private String errmsg; // message 可用来返回接口的说明

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
