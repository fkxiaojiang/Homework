package edu.guigu.accountbook.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.databinding.FragmentBillsBinding
import edu.guigu.accountbook.ui.adapter.RecordAdapter
import edu.guigu.accountbook.ui.dialog.AddEditRecordDialog
import edu.guigu.accountbook.ui.viewmodel.RecordViewModel
import edu.guigu.accountbook.util.DateUtils
import java.util.Calendar

class BillsFragment : Fragment() {

    private var _binding: FragmentBillsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecordViewModel
    private lateinit var adapter: RecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RecordViewModel::class.java]
        setupList()
        setupFilter()
        setupSearch()

        viewModel.allRecords.observe(viewLifecycleOwner) { records ->
            adapter.updateRecords(records)
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupList() {
        adapter = RecordAdapter(
            onItemClick = { record -> showEditDialog(record) },
            onItemLongClick = { record -> showDeleteConfirmDialog(record) }
        )
        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecords.adapter = adapter
    }

    private fun setupFilter() {
        binding.chipFilter.apply {
            isCheckable = true
            isCloseIconVisible = false
            setOnClickListener { showMonthPicker() }
            setOnCloseIconClickListener {
                viewModel.clearMonthFilter()
                text = "筛选月份"
                isChecked = false
                isCloseIconVisible = false
            }
            setOnLongClickListener {
                binding.searchView.visibility =
                    if (binding.searchView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                true
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchKeyword(newText?.trim().orEmpty())
                return true
            }
        })
    }

    private fun showMonthPicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, _ ->
                val start = Calendar.getInstance().apply {
                    set(year, month, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val end = Calendar.getInstance().apply {
                    set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                viewModel.setMonthFilter(start.timeInMillis, end.timeInMillis)
                binding.chipFilter.isChecked = true
                binding.chipFilter.isCloseIconVisible = true
                binding.chipFilter.text = "${year}年${month + 1}月"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            1
        ).apply {
            datePicker.findViewById<View>(
                resources.getIdentifier("day", "id", "android")
            )?.visibility = View.GONE
        }.show()
    }

    private fun showAddDialog() {
        AddEditRecordDialog(
            editRecord = null,
            onSave = { record ->
                viewModel.insert(record)
                Toast.makeText(requireContext(), "记录已添加", Toast.LENGTH_SHORT).show()
            }
        ).show(parentFragmentManager, "AddEditDialog")
    }

    private fun showEditDialog(record: Record) {
        AddEditRecordDialog(
            editRecord = record,
            onSave = { updatedRecord ->
                viewModel.update(updatedRecord)
                Toast.makeText(requireContext(), "记录已更新", Toast.LENGTH_SHORT).show()
            }
        ).show(parentFragmentManager, "AddEditDialog")
    }

    private fun showDeleteConfirmDialog(record: Record) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除确认")
            .setMessage(
                "确定要删除「${record.category}」这条记录吗？\n金额：¥${
                    DateUtils.formatAmount(record.amount)
                }"
            )
            .setPositiveButton("删除") { _, _ ->
                viewModel.delete(record)
                Toast.makeText(requireContext(), "记录已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
