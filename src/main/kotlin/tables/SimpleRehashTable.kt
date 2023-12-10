package tables

import org.example.hash
import kotlin.time.measureTime

class SimpleRehashTable(size: Int) {
    private val table = arrayOfNulls<String?>(size)

    /**
     * Добавление элемента в таблицу с помощью простого рехэширования
     * @param identifier идентификатор, который хотим добавить
     */
    fun insertElement(identifier: String) {
        if (table.none { it == null })
            return
        var attemptNumber = 0
        var hash = hash(identifier)

        while (true) {
            when (table[hash]) {
                identifier -> {
                    println("Такой элемент (${table[hash]}) уже существует, завершаем попытку добавления")
                    return
                }

                null -> {
                    // Ячейка пуста, заполняем её идентификатором
                    table[hash] = identifier
                    return
                }

                else -> {
                    // Ячейка не пуста, т.е. произошла коллизия
                    attemptNumber++
                    hash = simpleRehash(hash, attemptNumber)
                }
            }
        }
    }

    /**
     *  Поиск элемента в таблице с помощью простого рехэширования
     *  @param identifier идентификатор, который хотим найти
     *  @return кол-во попыток, затраченное на поиск элемента
     */
    fun findElement(identifier: String): Int? {
        var attemptNumber = 0
        var hash = hash(identifier)

        while (true) {
            when (table[hash]) {
                null -> {
                    // Ячейка пуста => элемент не найден, завершаем алгоритм
                    println("Элемент не найден, завершаем поиск")
                    return null
                }
                identifier -> {
                    // Ячейка не пуста и элемент совпадает => элемент найден, возвращаем кол-во попыток
                    return attemptNumber + 1
                }
                else -> {
                    // Ячейка не пуста, но элемент не совпадает с искомым
                    attemptNumber++
                    hash = simpleRehash(hash, attemptNumber)
                }
            }
        }
    }

    fun testSearch() {
        println("Поиск элемента CA0fatamOA9jl9NJWtJCVmKj9bh9JniT в таблице с простым рехэшированием: ")
        val timeToFindElement = measureTime { findElement("CA0fatamOA9jl9NJWtJCVmKj9bh9JniT") }.inWholeMicroseconds
        println("Время, затраченное на поиск: $timeToFindElement мкс")
    }

    private fun hash(identifier: String) = identifier.hash() % table.size
    private fun simpleRehash(oldHash: Int, i: Int) = (oldHash + i) % table.size
}