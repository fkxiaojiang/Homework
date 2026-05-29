package edu.guigu.accountbook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import edu.guigu.accountbook.data.dao.CategorySummary
import edu.guigu.accountbook.data.dao.MonthlyTrend
import edu.guigu.accountbook.data.database.AppDatabase
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.data.repository.RecordRepository
import kotlinx.coroutines.launch

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordRepository

    private val _allRecords = MutableLiveData<List<Record>>(emptyList())
    val allRecords: LiveData<List<Record>> get() = _allRecords

    private val _totalIncome = MutableLiveData(0.0)
    val totalIncome: LiveData<Double> get() = _totalIncome

    private val _totalExpense = MutableLiveData(0.0)
    val totalExpense: LiveData<Double> get() = _totalExpense

    private val _expenseCategorySummary = MutableLiveData<List<CategorySummary>>(emptyList())
    val expenseCategorySummary: LiveData<List<CategorySummary>> get() = _expenseCategorySummary

    private val _monthlyTrend = MutableLiveData<List<MonthlyTrend>>(emptyList())
    val monthlyTrend: LiveData<List<MonthlyTrend>> get() = _monthlyTrend

    private var currentFilterStart: Long? = null
    private var currentFilterEnd: Long? = null
    private var currentSearchKeyword: String = ""

    init {
        val dao = AppDatabase.getInstance(application).recordDao()
        repository = RecordRepository(dao)
        refreshData()
    }

    fun setMonthFilter(startDate: Long, endDate: Long) {
        currentFilterStart = startDate
        currentFilterEnd = endDate
        refreshData()
    }

    fun clearMonthFilter() {
        currentFilterStart = null
        currentFilterEnd = null
        refreshData()
    }

    fun setSearchKeyword(keyword: String) {
        currentSearchKeyword = keyword
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            _allRecords.postValue(loadVisibleRecords())
            _totalIncome.postValue(repository.getTotalIncome() ?: 0.0)
            _totalExpense.postValue(repository.getTotalExpense() ?: 0.0)
            _expenseCategorySummary.postValue(repository.getCategorySummary(Record.TYPE_EXPENSE))
            _monthlyTrend.postValue(repository.getMonthlyTrend())
        }
    }

    private suspend fun loadVisibleRecords(): List<Record> {
        val keyword = currentSearchKeyword.trim()
        val start = currentFilterStart
        val end = currentFilterEnd

        return when {
            start != null && end != null && keyword.isNotBlank() ->
                repository.searchRecordsByDateRange(start, end, keyword)

            start != null && end != null ->
                repository.getRecordsByDateRange(start, end)

            keyword.isNotBlank() ->
                repository.searchRecords(keyword)

            else ->
                repository.getAllRecords()
        }
    }

    fun insert(record: Record) {
        viewModelScope.launch {
            repository.insert(record)
            refreshData()
        }
    }

    fun update(record: Record) {
        viewModelScope.launch {
            repository.update(record)
            refreshData()
        }
    }

    fun delete(record: Record) {
        viewModelScope.launch {
            repository.delete(record)
            refreshData()
        }
    }
}
