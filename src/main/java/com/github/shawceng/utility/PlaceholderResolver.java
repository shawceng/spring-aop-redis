package com.github.shawceng.utility;


public class PlaceholderResolver {
    /**
     * 默认前缀占位符
     */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "{";

    /**
     * 默认后缀占位符
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    /**
     * 默认单例解析器
     */

    /**
     * 占位符前缀
     */
    private static String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

    /**
     * 占位符后缀
     */
    private static String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;


    /**
     * 根据对象中字段路径(即类似js访问对象属性值)替换模板中的占位符 <br/><br/>
     * 如 content = product:${id}:detail:${detail.id} <br/>
     *    obj = Product.builder().id(1).detail(Detail.builder().id(2).build()).build(); <br/>
     *    经过解析返回 product:1:detail:2 <br/>
     *
     * @param content  要解析的内容
     * @param obj   填充解析内容的对象(如果是基本类型，则所有占位符替换为相同的值)
     * @return
     */
    public static String resolveByObject(String content, final Object obj) {
        int start = content.indexOf(placeholderPrefix);
        if (start == -1) {
            return content;
        }
        StringBuilder result = new StringBuilder(content);
        int end = result.indexOf(placeholderSuffix, start);
        //获取占位符属性值，如${id}, 即获取id
        String placeholder = result.substring(start + placeholderPrefix.length(), end).trim();
        //替换整个占位符内容，即将${id}值替换为替换规则回调中的内容
//            String replaceContent = placeholder.trim().isEmpty() ? "" : rule.apply(placeholder);
        String replaceContent = ReflectionUtils.getValueByFieldPath(obj, placeholder).toString();
        result.replace(start, end + placeholderSuffix.length(), replaceContent);
        return result.toString();
    }

    public static String resolveByObjects(String content, Object[] objs) {
        for (Object obj : objs) {
            content = resolveByObject(content, obj);
        }
        return content;
    }
}