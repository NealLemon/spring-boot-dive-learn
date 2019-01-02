package com.web.method.support;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        //这里判断方法中的入参类型 是否匹配Properties
        return Properties.class.equals(parameter.getParameterType());
    }

    /**
     * 处理并获取请求的值
     * @param parameter
     * @param mavContainer
     * @param webRequest
     * @param binderFactory
     * @return
     * @throws Exception
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        //处理请求传入的值，并返回Properties 类型。
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
        //获取原生的HttpServletRequest
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        //获取Spring定义的 ServletServerHttpRequest
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
        //获取请求头中的 Content-Type
        MediaType contentType = inputMessage.getHeaders().getContentType();
        // 获取字符编码
        Charset charset = contentType.getCharset();
        // 当 charset 不存在时，使用 UTF-8
        charset = charset == null ? Charset.forName("UTF-8") : charset;
        // 获取字节流
        InputStream inputStream = inputMessage.getBody();
        //Properties的load方法需要一个实现Reader接口的字节流
        InputStreamReader reader = new InputStreamReader(inputStream, charset);
        //声明一个properties对象
        Properties properties = new Properties();
        // 加载字符流成为 Properties 对象
        properties.load(reader);
        reader.close();
        return properties;
    }
}
