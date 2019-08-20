package com.bsw.bswfilterlist

import java.lang.reflect.Field
import java.util.*

class KBswReflectUtils<T : Any> {

    /**
     * 解析后的属性集合
     */
    private var reflectFields: List<Field>? = null

    /**
     * List反射解析结果缓存，防止多次反射浪费资源
     */
    private val reflectResultMap = HashMap<T, Map<String, KBswReflectParam>>()
    /**
     * List反射解析后，主键以及对应的位置，用于根据主键替换Bean
     */
    private val reflectPrimaryKeyMap = HashMap<Any, Int>()
    /**
     * 当主键变动时，调整缓存列表
     */
    private val primaryKeyTMap = HashMap<Any, T>()

    /**
     * 反射解析泛型类
     *
     * @param clz 待解析的类
     */
    private fun reflectClass(clz: Class<*>) {
        reflectFields = ArrayList(Arrays.asList(*clz.declaredFields))
    }

    /**
     * 反射解析列表
     *
     * @param list 待反射解析的列表
     */
    fun reflectList(list: KBswFilterList<T>) {
        for (i in list.indices) {
            val t = list[i]
            val result = reflectT(t, i)
            if (null != result)
                reflectResultMap[t] = result
        }
    }

    /**
     * 解析泛型属性
     *
     * @param t 待解析泛型
     * @return 解析的属性名与属性值的map
     */
    fun reflectT(t: T): Map<String, KBswReflectParam>? {
        return reflectT(t, null)
    }

    /**
     * 解析泛型属性
     *
     * @param t     待解析泛型
     * @param index 被解析的类在列表中的坐标
     * @return 解析的属性名与属性值的map
     */
    @Synchronized
    private fun reflectT(t: T, index: Int?): Map<String, KBswReflectParam>? {
        if (null == reflectFields) {
            reflectClass(t.javaClass)
        }
        val reflectResult = HashMap<String, KBswReflectParam>()
        for (field in reflectFields!!) {
            field.isAccessible = true
            var key: String? = null
            try {
                if (TextUtils.isEmpty(key)) {
                    key = field.name
                }
                if (TextUtils.isEmpty(key)) {
                    continue
                }
                // 存储参数自身的属性名
                reflectResult[key!!] = KBswReflectParam(key, field.get(t), field)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                return null
            }

        }
        return reflectResult
    }

    /**
     * 获取T的解析结果
     *
     * @param t 泛型
     * @return 解析的结果
     */
    fun getReflectResult(t: T): Map<String, KBswReflectParam> {
        var paramMap = reflectResultMap[t]
        if (null == paramMap) {
            paramMap = reflectT(t)
            reflectResultMap[t] = paramMap!!
        }
        return paramMap
    }
}
