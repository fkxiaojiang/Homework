package edu.guigu.accountbook.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import edu.guigu.accountbook.data.model.Record

data class CategorySummary(
    val category: String,
    val total: Double
)

data class MonthlyTrend(
    val month: String,
    val income: Double,
    val expense: Double
)

@Dao
interface RecordDao {

    @Query("SELECT * FROM records ORDER BY date DESC")
    suspend fun getAllRecords(): List<Record>

    @Query("SELECT SUM(amount) FROM records WHERE type = ${Record.TYPE_INCOME}")
    suspend fun getTotalIncome(): Double?

    @Query("SELECT SUM(amount) FROM records WHERE type = ${Record.TYPE_EXPENSE}")
    suspend fun getTotalExpense(): Double?

    @Query("SELECT * FROM records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getRecordsByDateRange(startDate: Long, endDate: Long): List<Record>

    @Query(
        "SELECT * FROM records " +
            "WHERE category LIKE '%' || :keyword || '%' " +
            "OR note LIKE '%' || :keyword || '%' " +
            "ORDER BY date DESC"
    )
    suspend fun searchRecords(keyword: String): List<Record>

    @Query(
        "SELECT * FROM records " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "AND (category LIKE '%' || :keyword || '%' OR note LIKE '%' || :keyword || '%') " +
            "ORDER BY date DESC"
    )
    suspend fun searchRecordsByDateRange(
        startDate: Long,
        endDate: Long,
        keyword: String
    ): List<Record>

    @Query(
        "SELECT category, SUM(amount) AS total FROM records " +
            "WHERE type = :type GROUP BY category ORDER BY total DESC"
    )
    suspend fun getCategorySummary(type: Int): List<CategorySummary>

    @Query(
        """
        SELECT
            strftime('%Y-%m', date / 1000, 'unixepoch', 'localtime') AS month,
            SUM(CASE WHEN type = 1 THEN amount ELSE 0 END) AS income,
            SUM(CASE WHEN type = 0 THEN amount ELSE 0 END) AS expense
        FROM records
        GROUP BY month
        ORDER BY month ASC
        """
    )
    suspend fun getMonthlyTrend(): List<MonthlyTrend>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: Record): Long

    @Update
    suspend fun update(record: Record)

    @Delete
    suspend fun delete(record: Record)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
