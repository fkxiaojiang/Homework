package edu.guigu.accountbook.data.repository

import edu.guigu.accountbook.data.dao.CategorySummary
import edu.guigu.accountbook.data.dao.MonthlyTrend
import edu.guigu.accountbook.data.dao.RecordDao
import edu.guigu.accountbook.data.model.Record

class RecordRepository(private val dao: RecordDao) {

    suspend fun getAllRecords(): List<Record> = dao.getAllRecords()

    suspend fun getTotalIncome(): Double? = dao.getTotalIncome()

    suspend fun getTotalExpense(): Double? = dao.getTotalExpense()

    suspend fun getRecordsByDateRange(startDate: Long, endDate: Long): List<Record> =
        dao.getRecordsByDateRange(startDate, endDate)

    suspend fun searchRecords(keyword: String): List<Record> = dao.searchRecords(keyword)

    suspend fun searchRecordsByDateRange(
        startDate: Long,
        endDate: Long,
        keyword: String
    ): List<Record> = dao.searchRecordsByDateRange(startDate, endDate, keyword)

    suspend fun getCategorySummary(type: Int): List<CategorySummary> = dao.getCategorySummary(type)

    suspend fun getMonthlyTrend(): List<MonthlyTrend> = dao.getMonthlyTrend()

    suspend fun insert(record: Record): Long = dao.insert(record)

    suspend fun update(record: Record) = dao.update(record)

    suspend fun delete(record: Record) = dao.delete(record)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
