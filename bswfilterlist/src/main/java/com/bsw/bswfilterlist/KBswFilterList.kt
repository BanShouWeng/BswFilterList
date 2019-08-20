package com.bsw.bswfilterlist

import androidx.annotation.IntRange
import java.util.*
import kotlin.collections.ArrayList

class KBswFilterList<T : Any> : ArrayList<T> {

    companion object StaticParams {
        const val TO_THE_END = -1
    }

    /**
     * 无参构造方法
     */
    constructor()

    /**
     * 传入集合的构造方法
     *
     * @param list 被传入集合
     */
    constructor(list: List<T>) {
        this.addAll(list)
    }

    /**
     * 传入数组的构造方法
     *
     * @param list 被传入的数组
     */
    constructor(list: Array<T>) {
        this.addAll(ArrayList(Arrays.asList(*list)))
    }

    /**
     * 搜索
     */
    fun query(): KBswListQuery<T> {
        return KBswListQuery(this);
    }

    /**
     * 反射工具
     */
    val reflectUtils: KBswReflectUtils<T> = KBswReflectUtils()

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    fun query(filterAdapter: KBswListQuery.ListFilterAdapter<T>): KBswListQuery<T> {
        return KBswListQuery(this, filterAdapter)
    }

    /**
     * 更新，需配置参数的更新方式
     *
     * @return 更新的类
     */
    fun update(): KBswListUpdate<T> {
        return KBswListUpdate(this)
    }

    /**
     * 更新，通过适配器用户自行调整
     *
     * @param adapter 更新适配器
     * @return 更新后的结果
     */
    fun update(adapter: KBswListUpdate.ListUpdateAdapter<T>): KBswFilterList<T> {
        return KBswListUpdate(this).run(adapter)
    }

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    fun update(t: T, newT: T): KBswFilterList<T> {
        update(t, newT, true)
        return this
    }

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    fun update(bt: T, newT: T, isNullAdd: Boolean): KBswFilterList<T> {
        val bswDbListUpdate = KBswListUpdate(this)
        bswDbListUpdate.run(bt, newT, isNullAdd)
        return this
    }

    /**
     * 插入数据
     *
     * @param t 待插入数据
     * @return 返回当前列表
     */
    fun insert(t: T): KBswFilterList<T> {
        add(t)
        return this
    }

    /**
     * 插入数据
     *
     * @param t 待插入数据
     * @return 返回当前列表
     */
    fun insert(position: Int, t: T): KBswFilterList<T> {
        add(position, t)
        return this
    }

    /**
     * 截取集合（一般用于分页）
     *
     * @param from  从第几个开始截取
     * @param count 截取数量
     * @return 截取结果
     */
    override fun subList(@IntRange(from = 0) from: Int, @IntRange(from = TO_THE_END.toLong()) count: Int): KBswFilterList<T> {
        if (0 == from && -1 == count)
            return this
        else {
            val listSize = size
            if (listSize < from) {          // 若列表数量小于起始搜索条目角标，则返回空列表
                return KBswFilterList<T>()
            } else {                        // 若里列表数量大于起始搜索条目角标，则返回符合条件列表
                val end =
                    if (from + count < listSize) from + count else listSize  // 若目标获取的最后角标大于总数量，则以列表总数量作为判定条件，避免数组越界
                val subList = KBswFilterList<T>()
                for (i in from until end) {
                    subList.add(get(i))
                }
                return subList
            }
        }
    }
}
