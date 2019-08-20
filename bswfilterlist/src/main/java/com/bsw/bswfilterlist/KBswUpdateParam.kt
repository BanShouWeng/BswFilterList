package com.bsw.bswfilterlist

/**
 * 更新的参数
 */
internal class KBswUpdateParam(val key: String, val value: Any?, val newValue: Any) {
    companion object {
        const val NULL_VALUE = "NULL_VALUE !@#$%^&*()_+ NULL_VALUE"
    }

    fun judge(vReflect: Any?): Boolean {
        return if (NULL_VALUE == value) {
            true
        } else {
            if (null == vReflect) {
                null == value
            } else {
                vReflect == value
            }
        }
    }
}
