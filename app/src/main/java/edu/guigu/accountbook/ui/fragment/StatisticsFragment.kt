package edu.guigu.accountbook.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import edu.guigu.accountbook.data.dao.CategorySummary
import edu.guigu.accountbook.data.dao.MonthlyTrend
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.databinding.FragmentStatisticsBinding
import edu.guigu.accountbook.ui.viewmodel.RecordViewModel
import edu.guigu.accountbook.util.DateUtils

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[RecordViewModel::class.java]
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeData() {
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = "¥${DateUtils.formatAmount(income)}"
            updateBalance()
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvExpense.text = "¥${DateUtils.formatAmount(expense)}"
            updateBalance()
        }

        viewModel.expenseCategorySummary.observe(viewLifecycleOwner) { summary ->
            setupPieChart(summary)
        }

        viewModel.monthlyTrend.observe(viewLifecycleOwner) { trend ->
            setupLineChart(trend)
        }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        binding.tvBalance.text = "¥${DateUtils.formatAmount(income - expense)}"
    }

    private fun setupPieChart(summary: List<CategorySummary>) {
        if (summary.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.centerText = "暂无数据"
            binding.pieChart.invalidate()
            return
        }

        val entries = summary.map { PieEntry(it.total.toFloat(), it.category) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = summary.map { item ->
                val color = Record.getCategoryColor(item.category)
                Color.rgb(Color.red(color), Color.green(color), Color.blue(color))
            }
            sliceSpace = 3f
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
        }

        binding.pieChart.apply {
            data = pieData
            centerText = "支出分类"
            setUsePercentValues(true)
            description.isEnabled = false
            setDrawEntryLabels(false)
            legend.textColor = Color.DKGRAY
            isDrawHoleEnabled = true
            holeRadius = 40f
            setCenterTextSize(16f)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupLineChart(trend: List<MonthlyTrend>) {
        if (trend.isEmpty()) {
            binding.lineChart.clear()
            binding.lineChart.setNoDataText("暂无趋势数据")
            binding.lineChart.invalidate()
            return
        }

        val incomeEntries = mutableListOf<Entry>()
        val expenseEntries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        trend.forEachIndexed { index, item ->
            incomeEntries.add(Entry(index.toFloat(), item.income.toFloat()))
            expenseEntries.add(Entry(index.toFloat(), item.expense.toFloat()))
            labels.add(item.month)
        }

        val incomeSet = LineDataSet(incomeEntries, "收入").apply {
            color = Color.parseColor("#2ECC71")
            setCircleColor(Color.parseColor("#2ECC71"))
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
            valueFormatter = CurrencyValueFormatter()
        }

        val expenseSet = LineDataSet(expenseEntries, "支出").apply {
            color = Color.parseColor("#E74C3C")
            setCircleColor(Color.parseColor("#E74C3C"))
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
            valueFormatter = CurrencyValueFormatter()
        }

        binding.lineChart.apply {
            data = LineData(incomeSet, expenseSet)
            description.isEnabled = false
            axisRight.isEnabled = false
            legend.textColor = Color.DKGRAY
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
            axisLeft.valueFormatter = CurrencyValueFormatter()
            animateX(800)
            invalidate()
        }
    }

    private class CurrencyValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "¥${DateUtils.formatAmount(value.toDouble())}"
        }
    }
}
