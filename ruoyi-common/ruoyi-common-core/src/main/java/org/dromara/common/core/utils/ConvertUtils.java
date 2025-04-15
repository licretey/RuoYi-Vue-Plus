package org.dromara.common.core.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConvertUtils {

    private ConvertUtils() {
    }

    /**
     * 将List转为Map
     *
     * @param list         原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return
     */
    public static <K, V> Map<K, V> listToMap(List<V> list, Function<V, K> keyExtractor) {
        return listToMap(list, keyExtractor,
            // 无规则
            Objects::nonNull);
    }

    /**
     * 将List转为Map，可以指定过滤规则
     *
     * @param list         原数据
     * @param keyExtractor Key的抽取规则
     * @param predicate    过滤规则
     * @param <K>          Key
     * @param <V>          Value
     * @return
     */
    public static <K, V> Map<K, V> listToMap(List<V> list, Function<V, K> keyExtractor, Predicate<V> predicate) {
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }
        Map<K, V> map = new HashMap<>(list.size());
        for (V element : list) {
            K key = keyExtractor.apply(element);
            if (key == null || !predicate.test(element)) {
                continue;
            }
            map.put(key, element);
        }
        return map;
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return
     */
    public static <T, R> List<R> resultToList(List<T> originList, Function<T, R> mapper) {
        return resultToList(originList, mapper,
            // 无规则
            Objects::nonNull);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     * 可以指定过滤规则
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param predicate  过滤规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return
     */
    public static <T, R> List<R> resultToList(List<T> originList, Function<T, R> mapper, Predicate<T> predicate) {
        if (originList == null || originList.isEmpty()) {
            return new ArrayList<>();
        }
        List<R> newList = new ArrayList<>(originList.size());
        for (T originElement : originList) {
            R newElement = mapper.apply(originElement);
            if (newElement == null || !predicate.test(originElement)) {
                continue;
            }
            newList.add(newElement);
        }
        return newList;
    }
}
