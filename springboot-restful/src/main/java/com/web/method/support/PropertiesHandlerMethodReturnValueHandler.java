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

public class PropertiesHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return Properties.class.equals(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(webRequest.getNativeRequest(HttpServletRequest.class));
        ServletServerHttpResponse response = new ServletServerHttpResponse(webRequest.getNativeResponse(HttpServletResponse.class));
        MediaType contentType = response.getHeaders().getContentType();

        if(contentType == null) {
            contentType = inputMessage.getHeaders().getContentType();
        }
        response.getHeaders().setContentType(contentType);
        // 获取字符编码
        Charset charset = contentType.getCharset();
        // 当 charset 不存在时，使用 UTF-8
        charset = charset == null ? Charset.forName("UTF-8") : charset;

        OutputStream outputStream = response.getBody();
        // 字符输出流
        Writer writer = new OutputStreamWriter(outputStream, charset);
        // Properties 写入到字符输出流
        Properties properties = (Properties)returnValue;
        properties.store(writer,"From PropertiesHttpMessageConverter");
    }
}
