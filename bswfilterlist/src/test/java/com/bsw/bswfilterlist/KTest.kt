package com.bsw.bswfilterlist

import org.junit.Test

class KTest {
    @Test
    fun test() {
        val people: KBswFilterList<Person> = KBswFilterList()
        val person = Person("张四", 9, false)
        people.insert(Person("张三", 5, false))
        people.insert(person)
        people.insert(Person("张五", 52, true))
        people.insert(Person("李三", 26, true))
        people.insert(Person("李四", 18, false))

        println(
            people.query().sort("age", KBswListQuery.DESC)
                .putParams(KBswListQuery.PARAM_TYPE_CONTAINS, "name", "张")
                .getAll()
        )

        println(
            people.query().sort("age", KBswListQuery.DESC)
                .putParams(KBswListQuery.PARAM_TYPE_RANGE_IN, "age", 9, 30)
                .getAll()
        )

        people.update().putParams("name", "张三", "张三三")
            .putParams("sex", true)
            .run()

        println(
            people
        )

        println(
            person
        )

        people.update(person, Person("张四四", 15, false))

        println(people)
    }

    class Person(val name: String, val age: Int, val sex: Boolean) {
        override fun toString(): String {
            return "Person(name='$name', age=$age, sex=$sex)"
        }
    }
}