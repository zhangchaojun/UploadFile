package com.example.httpupload.server;

import com.alibaba.fastjson.JSON;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;

import static io.netty.buffer.Unpooled.copiedBuffer;

/*
 * 自定义处理的handler
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /*
     * 处理请求
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
        System.out.println(fullHttpRequest);

        FullHttpResponse response = null;
        if (fullHttpRequest.method() == HttpMethod.GET) {
            System.out.println(getGetParamsFromChannel(fullHttpRequest));
            String data = "GET method over";
            ByteBuf buf = copiedBuffer(data, CharsetUtil.UTF_8);
            response = responseOK(HttpResponseStatus.OK, buf);

        } else if (fullHttpRequest.method() == HttpMethod.POST) {
            System.out.println(getPostParamsFromChannel(fullHttpRequest));
            String data = "POST method over";
            ByteBuf content = copiedBuffer(data, CharsetUtil.UTF_8);
            System.out.println("post 返回");
            response = responseOK(HttpResponseStatus.OK, content);

        } else {
            response = responseOK(HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
        }
        // 发送响应
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /*
     * 获取GET方式传递的参数
     */
    private Map<String, Object> getGetParamsFromChannel(FullHttpRequest fullHttpRequest) {

        Map<String, Object> params = new HashMap<String, Object>();

        if (fullHttpRequest.method() == HttpMethod.GET) {
            // 处理get请求
            QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
            Map<String, List<String>> paramList = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                params.put(entry.getKey(), entry.getValue().get(0));
            }
            return params;
        } else {
            return null;
        }

    }

    /*
     * 获取POST方式传递的参数
     */
    private Map<String, Object> getPostParamsFromChannel(FullHttpRequest fullHttpRequest) {

        Map<String, Object> params = new HashMap<String, Object>();

        if (fullHttpRequest.method() == HttpMethod.POST) {
            // 处理POST请求
            String strContentType = fullHttpRequest.headers().get("Content-Type").trim();
            if (strContentType.contains("text/plain")) {
                ByteBuf content = fullHttpRequest.content();
                int i = content.readableBytes();
                byte[] bytes = new byte[i];
                content.readBytes(bytes);
                String s = new String(bytes, CharsetUtil.UTF_8);
                System.out.println("平文本：" + s);
            } else if (strContentType.contains("x-www-form-urlencoded")) {
                params = getFormParams(fullHttpRequest);
            } else if (strContentType.contains("application/json")) {
                try {
                    params = getJSONParams(fullHttpRequest);
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            } else if (strContentType.contains("multipart/form-data")) {

                ByteBuf content = fullHttpRequest.content();
                int i = content.readableBytes();
                System.out.println("content 长度== " + i);
            } else if (strContentType.contains("application/octet-stream")) {
                long starttime = System.currentTimeMillis();
                System.out.println("开始保存" + starttime);
                ByteBuf byteBuf = fullHttpRequest.content();
                int readableBytes = byteBuf.readableBytes();
                byte[] bytes = new byte[readableBytes];
                byteBuf.readBytes(bytes);//读入定义的字节数组数组

                InputStream inputStream = new ByteArrayInputStream(bytes);
                File file = new File("d:/hello.exe");
                FileOutputStream fos = null;
                int total = 0;
                int len;
                byte[] buffer = new byte[1024];
                //读取流
                try {
                    fos = new FileOutputStream(file);
                    while ((len = inputStream.read(buffer)) != -1) {

                        fos.write(buffer, 0, len);
                        total += len;
                        System.out.println("已下载==" + total);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.flush();
                        fos.close();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                long endtime = System.currentTimeMillis();
                long l = endtime - starttime;
                System.out.println("存储耗时" + l);

            } else {
                return null;
            }
            return params;
        } else {
            return null;
        }
    }

    static {
        //这是存储上传文件和属性的位置的配置，如果不存在该文件夹会报错。
        DiskFileUpload.deleteOnExitTemporaryFile = true; //当文件出现重名的时候是否删除
        DiskFileUpload.baseDirectory = "D:" + File.separatorChar + "aa"; // 系统存储文件的位置
        DiskAttribute.deleteOnExitTemporaryFile = true; //如果属性出现重复选择删掉
        DiskAttribute.baseDirectory = "D:" + File.separatorChar + "aa"; // 属性文件存储目录。
    }

    /*
     * 解析from表单数据（Content-Type = x-www-form-urlencoded）
     */
    private Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>();

        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();

        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }

        return params;
    }

    /*
     * 解析json数据（Content-Type = application/json）
     */
    private Map<String, Object> getJSONParams(FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        Map<String, Object> params = new HashMap<String, Object>();

        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, "UTF-8");


        com.alibaba.fastjson.JSONObject jsonParams = JSON.parseObject(strContent);
        for (Object key : jsonParams.keySet()) {
            params.put(key.toString(), jsonParams.get(key));
        }
        return params;

    }

    private FullHttpResponse responseOK(HttpResponseStatus status, ByteBuf content) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set("Content-Type", "text/plain;charset=UTF-8");
            response.headers().set("Content_Length", response.content().readableBytes());
        }
        return response;
    }

}