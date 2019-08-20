package com.bsw.bswfilterlist

/**
 * 搜索的参数
 */
internal class KBswQueryParams {
    /**
     * 搜索类型
     * [BswDbListQuery.PARAM_TYPE_CONTAINS]是否包含[BswDbQueryParam.value]判断
     * [BswDbListQuery.PARAM_TYPE_EQUALS]是否与[BswDbQueryParam.value]相等判断
     * [BswDbListQuery.PARAM_TYPE_RANGE_IN]是否在最小值[BswDbQueryParam.min]与最大值[BswDbQueryParam.max]范围内，
     * [BswDbListQuery.PARAM_TYPE_RANGE_IN]是否在最小值[BswDbQueryParam.min]与最大值[BswDbQueryParam.max]范围外，
     */
    private val paramType: Int
    var key: String? = null
        private set
    private var value: Any? = null
    private var min: Any = 0
    private var max: Any = 0

    /**
     * 当前搜索类型是否可用，PARAM_TYPE_CONTAINS 为0001； PARAM_TYPE_EQUALS 为0010； PARAM_TYPE_RANGE_IN 为0100； PARAM_TYPE_RANGE_OUT 为1000
     * 因此paramType与1111按位取与，若不为0则表示在这个范围内
     *
     * @return paramType是否可用
     */
    private val isParamTypeNotAvailable: Boolean
        get() = paramType and 0x0f == 0

    /**
     * 单参匹配
     *
     * @param type  [BswDbQueryParam.paramType]匹配类型，包含或者相同
     * @param key   [BswDbQueryParam.key]Bean中对应字段名
     * @param value [BswDbQueryParam.value]Bean中对应字段参数
     */
    constructor(@KBswListQuery.Companion.ParamTypeMatch type: Int, key: String, value: Any?) {
        paramType = type
        this.key = key
        this.value = value
    }

    /**
     * 范围匹配
     *
     * @param type [BswDbQueryParam.paramType]匹配类型，范围内或者范围外
     * @param key  [BswDbQueryParam.key]Bean中对应字段名
     * @param min  [BswDbQueryParam.min]范围最小值  min大于max会崩溃
     * @param max  [BswDbQueryParam.max]范围最大值
     */
    constructor(@KBswListQuery.Companion.ParamTypeRange type: Int, key: String, min: Any?, max: Any?) {
        if (null == min || null == max) {
            throw IllegalArgumentException("Min num or max num can't be null")
        }
        paramType = type
        this.key = key
        this.min = min
        this.max = max
    }

    /**
     * 判断反射获取数值是否符合筛选条件
     *
     * @param vReflect 反射获取的待判断参数
     * @return 是否符合条件
     */
    fun judge(vReflect: Any?): Boolean {
        // 类型不可用则判断结果为不满足
        if (isParamTypeNotAvailable) {
            return false
        }
        when (paramType) {
            KBswListQuery.PARAM_TYPE_CONTAINS -> {
                val vContains = value
                if (null == vContains || null == vReflect) {
                    return null == vContains && null == vReflect
                } else if (vReflect is String && vContains is String) {
                    return vReflect.toString().contains(vContains.toString())
                }
                val vEquals = value
                return vReflect == vEquals
            }

            KBswListQuery.PARAM_TYPE_EQUALS -> {
                val vEquals = value
                return if (null == vReflect) {
                    null == vEquals
                } else {
                    vReflect == vEquals
                }
            }

            KBswListQuery.PARAM_TYPE_RANGE_IN -> {
                val isInRange = isInRange(vReflect, min, max)
                return isInRange ?: false
            }

            KBswListQuery.PARAM_TYPE_RANGE_OUT -> {
                val isNotOutRangee = isInRange(vReflect, min, max)
                return if (null == isNotOutRangee) {
                    false
                } else {
                    !isNotOutRangee
                }
            }

            else -> return false
        }
    }

    /**
     * 判断待判断数值（只有数值能比大小）vReflect是否在vMin ~ vMax范围内
     *
     * @param vReflect 待比较数值
     * @param vMin     最小值
     * @param vMax     最大值
     * @return 是否满足条件
     */
    private fun isInRange(vReflect: Any?, vMin: Any, vMax: Any): Boolean? {
        try {
            val reflectNum = java.lang.Double.valueOf(vReflect!!.toString())
            val minNum = java.lang.Double.valueOf(vMin.toString())
            val maxNum = java.lang.Double.valueOf(vMax.toString())

            if (minNum > maxNum) {
                throw IllegalArgumentException("The maximum must be greater than the minimum.")
            }

            return minNum <= reflectNum && reflectNum <= maxNum
        } catch (e: NumberFormatException) {
            return null
        } catch (e: NullPointerException) {
            return null
        }

    }
}
