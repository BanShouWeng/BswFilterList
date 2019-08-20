package com.bsw.bswfilterlist

import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.StringDef
import com.bsw.bswfilterlist.KBswFilterList.StaticParams.TO_THE_END
import java.util.*

class KBswListQuery<T : Any> constructor(list: KBswFilterList<T>) {
    private val list: KBswFilterList<T> = list
    private var filterAdapter: ListFilterAdapter<T>? = null
    private var reflectUtils: KBswReflectUtils<T> = list.reflectUtils

    constructor (list: KBswFilterList<T>, filterAdapter: ListFilterAdapter<T>?) : this(list) {
        this.filterAdapter = filterAdapter
    }

    /**
     * kotlin的静态方法
     */
    companion object {
        @StringDef(DESC, ASC)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class SortType

        /**
         * 查询结果排序方式：DESC倒序、ASC正序
         */
        const val DESC: String = " desc"
        const val ASC: String = " asc"

        @StringDef(AND, OR)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class QueryType

        /**
         * 查询结果排序方式：DESC倒序、ASC正序
         */
        const val AND: String = " and "
        const val OR: String = " or "

        @IntDef(PARAM_TYPE_CONTAINS, PARAM_TYPE_EQUALS)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class ParamTypeMatch

        /**
         * 包含搜索关键字
         */
        const val PARAM_TYPE_CONTAINS = 0x01
        /**
         * 相同
         */
        const val PARAM_TYPE_EQUALS = 0x02

        @IntDef(PARAM_TYPE_RANGE_IN, PARAM_TYPE_RANGE_OUT)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class ParamTypeRange

        /**
         * 范围内
         */
        const val PARAM_TYPE_RANGE_IN = 0x04
        /**
         * 范围外
         */
        const val PARAM_TYPE_RANGE_OUT = 0x08
    }

    /**
     * 搜索类型，默认与
     */
    private var queryType = AND
    /**
     * 排序类型，默认正序
     */
    private var sortType = ASC

    /**
     * 筛选条件列表
     */
    private var queryList: MutableList<KBswQueryParams>? = null

    /**
     * 用于排序的Key
     */
    private var sortKey = ""

    /**
     * 添加参数：Short
     *
     * @param key   key
     * @param value value
     * @return 查询类
     */
    fun putParams(key: String, value: Any): KBswListQuery<T> {
        if (null == queryList) {
            queryList = ArrayList()
        }
        queryList!!.add(KBswQueryParams(PARAM_TYPE_CONTAINS, key, value))
        return this
    }

    /**
     * 添加参数：Short
     *
     * @param matchType matchType
     * @param key       key
     * @param value     value
     * @return 查询类
     */
    fun putParams(@ParamTypeMatch matchType: Int, key: String, value: Any): KBswListQuery<T> {
        if (null == queryList) {
            queryList = ArrayList()
        }
        queryList!!.add(KBswQueryParams(matchType, key, value))
        return this
    }

    /**
     * 添加参数：Short
     *
     * @param rangeType rangeType
     * @param key       key
     * @param min       范围最小值
     * @param max       范围最大值
     * @return 查询类
     */
    fun putParams(@ParamTypeRange rangeType: Int, key: String, min: Any, max: Any): KBswListQuery<T> {
        if (null == queryList) {
            queryList = ArrayList()
        }
        queryList!!.add(KBswQueryParams(rangeType, key, min, max))
        return this
    }

    /**
     * 对获取的结果进行排序
     *
     * @param key      排序标识
     * @param sortType 排序方式，默认正序
     * @return 当前搜索类
     */
    fun sort(key: String, @SortType sortType: String): KBswListQuery<T> {
        this.sortKey = key
        this.sortType = sortType
        return this
    }

    /**
     * 设置搜索类型
     *
     * @param queryType 搜索的类型，默认AND，所有搜索条件都满足，可设置OR，满足一条即可
     * @return 当前搜索类
     */
    fun setQueryType(@QueryType queryType: String): KBswListQuery<T> {
        this.queryType = queryType
        return this
    }

    /**
     * 截取搜索结果集合（一般用于分页）
     *
     * @param from  从第几个开始截取
     * @param count 截取数量
     * @return 截取结果
     */
    @Synchronized
    fun getList(@IntRange(from = 0) from: Int, @IntRange(from = TO_THE_END.toLong()) count: Int): KBswFilterList<T>? {
        var ignoredCount = 0
        if (list.size == 0) {
            return null
        }

        // 反射一次解析所有的List，避免多次反射消耗资源
        reflectUtils.reflectList(list)

        var resultList: KBswFilterList<T> = KBswFilterList()

        if (null == queryList || queryList!!.size == 0) {       // 搜索条件为空则直接返回排序结果
            if (null == filterAdapter)
                resultList = list
            else {
                for (t in list) {
                    if (filterAdapter!!.filter(t)) {
                        resultList.add(t)
                    }
                }
            }
            return sortJudge(resultList).subList(from, count)
        } else {                                                // 搜索条件不为空则先筛选，再返回排序结果
            when (queryType) {
                AND -> for (t in list) {
                    if (andJudge(t)) {
                        if (ignoredCount < from) {
                            ignoredCount++
                        } else {
                            resultList.add(t)
                        }
                    }
                    if (count != TO_THE_END && count <= resultList.size) {
                        break
                    }
                }

                OR -> for (t in list) {
                    if (orJudge(t)) {
                        if (ignoredCount < from) {
                            ignoredCount++
                        } else {
                            resultList.add(t)
                        }
                        if (count != TO_THE_END && count <= resultList.size) {
                            break
                        }
                    }
                }
            }
            // 添加的时候已经做好了筛选
            return sortJudge(resultList)
        }
    }

    /**
     * 获取满足条件的所有
     *
     * @return 返回符合条件的所有
     */
    @Synchronized
    fun getAll(): KBswFilterList<T>? {
        return getList(0, -1)
    }


    /**
     * 获取满足条件的第一个
     *
     * @return 第一个
     */
    @Synchronized
    fun getFirst(): T? {
        if (list.size == 0) {
            return null
        }

        var sortedList: KBswFilterList<T> = KBswFilterList()
        if (TextUtils.isEmpty(sortKey)) {
            reflectUtils.reflectList(sortedList)
            sortedList = sortJudge(list)
        } else
            sortedList = list

        when (queryType) {
            AND -> for (t in sortedList) {
                if (andJudge(t))
                    return t
            }

            OR -> for (t in sortedList) {
                if (orJudge(t))
                    return t
            }
        }
        return null
    }

    /**
     * 与判断
     *
     * @param t 待判断类
     * @return 是否满足与条件
     */
    @Synchronized
    private fun andJudge(t: T): Boolean {
        val reflectResult = reflectUtils.getReflectResult(t) ?: return false
        for (p in queryList!!) {
            val param = reflectResult.get(p.key) ?: return false
            val vReflect = param.value
            val judge = p.judge(vReflect)
            // 若paramType没有问题，则根据当前判断是筛选条件的与判断，有一个不满足则返回，所以验证judge若为false，则不满足返回
            if (!judge) {
                return false
            }
        }
        // 用户自定义判断，由于与判断，因此判断结果取与
        return filterAdapter?.filter(t) ?: true
    }

    /**
     * 或判断
     *
     * @param t 待判断类
     * @return 是否满足或条件
     */
    @Synchronized
    private fun orJudge(t: T): Boolean {
        val reflectResult = reflectUtils.getReflectResult(t) ?: return false
        for (p in queryList!!) {
            val param = reflectResult.get(p.key) ?: continue
            val vReflect = param.value
            var judge = p.judge(vReflect)
            // 用户自定义判断，由于或判断，因此判断结果取或
            if (null != filterAdapter) {
                judge = judge || filterAdapter!!.filter(t)
            }
            // 若paramType没有问题，则根据当前判断是筛选条件的与判断，有一个不满足则返回，所以验证judge若为false，则不满足返回
            if (judge) {
                return true
            }
        }
        return false
    }

    /**
     * 排序判断
     *
     * @param list 排序的列表
     * @return 排序后的列表
     */
    @Synchronized
    private fun sortJudge(list: KBswFilterList<T>): KBswFilterList<T> {
        if (TextUtils.isEmpty(sortKey)) {
            return list
        } else {
            list.sortWith(kotlin.Comparator { o2, o1 ->
                val param1: Any = reflectUtils.getReflectResult(o1).get(sortKey)?.value as Any
                val param2: Any = reflectUtils.getReflectResult(o2).get(sortKey)?.value as Any

                if (param1 is Int && param2 is Int) {
                    if (param1 < param2) -1 else 1
                } else if (param1 is Long && param2 is Long) {
                    if (param1 < param2) -1 else 1
                } else if (param1 is Double && param2 is Double) {
                    if (param1 < param2) -1 else 1
                } else if (param1 is Float && param2 is Float) {
                    if (param1 < param2) -1 else 1
                } else if (param1 is Short && param2 is Short) {
                    if (param1 < param2) -1 else 1
                } else if (param1 is Byte && param2 is Byte) {
                    if (param1 < param2) -1 else 1
                } else if (param1 is Boolean && param2 is Boolean) {
                    if (param1) -1 else 1
                } else {
                    param1.toString().compareTo(o2.toString())
                }
            })
            if (sortType == DESC) {
                Collections.reverse(list)
            }
            return list
        }
    }

    interface ListFilterAdapter<T> {
        fun filter(t: T): Boolean
    }
}