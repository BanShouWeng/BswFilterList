package com.bsw.bswfilterlist

import java.util.ArrayList

class KBswListUpdate<T : Any> internal constructor(private val list: KBswFilterList<T>) {
    private val reflectUtils: KBswReflectUtils<T>
    /**
     * 筛选条件列表
     */
    private val updateList = ArrayList<KBswUpdateParam>()

    init {
        reflectUtils = list.reflectUtils
        reflectUtils.reflectList(list)
    }

    /**
     * 替换对应项
     *
     * @param bt        原数据
     * @param newT      用来替换的数据
     * @param isNullAdd 如果列表中无对应项，是否需要添加
     */
    @Synchronized
    fun run(bt: T, newT: T, isNullAdd: Boolean): KBswFilterList<T> {
        if (list.contains(bt)) {
            val index = list.indexOf(bt)
            list.removeAt(index)
            list.add(index, newT)
        } else {
            if (isNullAdd) {
                list.add(newT)
            }
        }
        return list
    }

    /**
     * 设置更新值
     *
     * @param key      用于查找被更新项的属性名
     * @param newValue 新的值
     */
    fun putParams(key: String, newValue: Any): KBswListUpdate<T> {
        putParams(key, KBswUpdateParam.NULL_VALUE, newValue)
        return this
    }

    /**
     * 设置更新值
     *
     * @param key
     * @param originalValue
     * @param newValue
     */
    fun putParams(key: String, originalValue: Any, newValue: Any): KBswListUpdate<T> {
        updateList.add(KBswUpdateParam(key, originalValue, newValue))
        return this
    }

    @Synchronized
    fun run(): KBswFilterList<T> {
        for (t in list) {
            var reflectResult: Map<String, KBswReflectParam>? = reflectUtils.getReflectResult(t)
            if (null == reflectResult) {
                reflectResult = reflectUtils.reflectT(t)
            }
            for (p in updateList) {
                val param = reflectResult!![p.key] ?: continue
                if (p.judge(param.value)) {
                    try {
                        param.field.set(t, p.newValue)
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        return list
    }

    @Synchronized
    internal fun run(adapter: ListUpdateAdapter<T>?): KBswFilterList<T> {
        val resultList = KBswFilterList<T>()
        for (i in list.indices) {
            if (null != adapter) {
                val aT = list[i]
                val bT = adapter.update(aT)
                resultList.add(bT ?: aT)
            }
        }
        return resultList
    }

    interface ListUpdateAdapter<T> {
        fun update(item: T): T?
    }
}
