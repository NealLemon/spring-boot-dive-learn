package com.web.method.support;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * 自定义Properties 返回值解析器
 */
public class PropertiesHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    /**
     * 是否支持返回值类型
     * @param returnType
     * @return
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        //这里判断方法中的返回值类型 是否匹配Properties
        return Properties.class.equals(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        //这里这是为true,告诉Spring已经完成了所有的处理，不需要往下处理了
        mavContainer.setRequestHandled(true);
        //获取Spring定义的 ServletServerHttpRequest
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(webRequest.getNativeRequest(HttpServletRequest.class));
        //获取Spring定义的 ServletServerHttpResponse
        ServletServerHttpResponse response = new ServletServerHttpResponse(webRequest.getNativeResponse(HttpServletResponse.class));
        //获取响应头中的 Content-Type
        MediaType contentType = response.getHeaders().getContentType();
        //如果响应头为空
        if(contentType == null) {
            //将请求头中的Content-Type 设置成 响应头的Content-Type
            contentType = inputMessage.getHeaders().getContentType();
        }
        //设置响应头为请求头的Content-type
        response.getHeaders().setContentType(contentType);
        // 获取字符编码
        Charset charset = contentType.getCharset();
        // 当 charset 不存在时，使用 UTF-8
        charset = charset == null ? Charset.forName("UTF-8") : charset;

        //获取响应流
        OutputStream outputStream = response.getBody();
        // 字符输出流
        Writer writer = new OutputStreamWriter(outputStream, charset);
        // Properties 写入到字符输出流
        Properties properties = (Properties)returnValue;
        //响应回客户端
        properties.store(writer,"From Neal Self");
        writer.close();
    }
}
