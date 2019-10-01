package com.example.httpupload.client2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpResponse response = (FullHttpResponse) msg;

        ByteBuf content = response.content();
        HttpHeaders headers = response.headers();

        System.out.println("content:" + System.getProperty("line.separator") + content.toString(CharsetUtil.UTF_8));
        System.out.println("headers:" + System.getProperty("line.separator") + headers.toString());

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel active");

        /**
         * 普通post请求
         */
//        String meg = "hello";
//        //配置HttpRequest的请求数据和一些配置信息
//        FullHttpRequest request = new DefaultFullHttpRequest(
//                HttpVersion.HTTP_1_1, HttpMethod.POST, "/test", Unpooled.wrappedBuffer(meg.getBytes(StandardCharsets.UTF_8)));
//
//        request.headers()
//                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
//                //开启长连接
//                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
//                //设置传递请求内容的长度
//                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
//
//        //发送数据
//        ctx.writeAndFlush(request);


        /**
         * 上传文件请求
         */
//        1、首先要先将上传的文件封装到HttpPostRequestEncoder中

        HttpRequest request1 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/test2");

        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        // This encoder will help to encode Request for a FORM as POST.
        HttpPostRequestEncoder bodyRequestEncoder = new HttpPostRequestEncoder(factory, request1, false);
        bodyRequestEncoder.addBodyFileUpload("file", new File("d:/1.txt"), "application/x-zip-compressed", false);


        //2、然后获取该封装的request中的List<InterfaceHttpData>
        List<InterfaceHttpData> bodylist = bodyRequestEncoder.getBodyListAttributes();

        //3、最后再重新创建一个HttpRequest,将上面封装获取到的List<InterFaceHttpData> 放入，然后通过channel直接发送该request就行了，该注意的是第二次创建的HttpPostRequestEncoder的第三个参数是true
        HttpRequest uploadRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/test2");
        uploadRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        HttpPostRequestEncoder bodyRequestEncoder2 = new HttpPostRequestEncoder(factory, uploadRequest, true);

        bodyRequestEncoder2.setBodyHttpDatas(bodylist);
        bodyRequestEncoder2.finalizeRequest();

        ctx.writeAndFlush(uploadRequest);

    }


}
