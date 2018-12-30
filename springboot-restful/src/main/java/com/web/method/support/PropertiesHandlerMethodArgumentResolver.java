package com.web.method.support;

import org.springframework.core.MethodParameter;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * 自定义 Properties 参数解析器
 */
public class PropertiesHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {


    /**
     * 是否支持参数解析
     * @param parameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Properties.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Properties arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
        return arg;
    }

    /**
     * 参数读取
     * @param webRequest
     * @param parameter
     * @param nestedGenericParameterType
     * @return
     */
    private Properties readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter, Type nestedGenericParameterType) throws Exception {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);

        MediaType contentType = inputMessage.getHeaders().getContentType();
        // 获取字符编码
        Charset charset = contentType.getCharset();
        // 当 charset 不存在时，使用 UTF-8
        charset = charset == null ? Charset.forName("UTF-8") : charset;
        // 字节流
        InputStream inputStream = inputMessage.getBody();
        InputStreamReader reader = new InputStreamReader(inputStream, charset);
        Properties properties = new Properties();
        // 加载字符流成为 Properties 对象
        properties.load(reader);
        return properties;
    }
}
