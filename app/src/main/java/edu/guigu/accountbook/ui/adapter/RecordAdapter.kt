package edu.guigu.accountbook.ui.adapter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import edu.guigu.accountbook.R
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.databinding.ItemRecordBinding
import edu.guigu.accountbook.util.DateUtils

class RecordAdapter(
    private val onItemClick: (Record) -> Unit,
    private val onItemLongClick: (Record) -> Unit
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    private val records = mutableListOf<Record>()

    fun updateRecords(newRecords: List<Record>) {
        val oldRecords = records.toList()
        val diffResult = DiffUtil.calculateDiff(RecordDiffCallback(oldRecords, newRecords))
        records.clear()
        records.addAll(newRecords)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = records.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun onBindViewHolder(
        holder: RecordViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            holder.bind(records[position])
            return
        }

        payloads.forEach { payload ->
            (payload as? Bundle)?.let(holder::applyPayload)
        }
        holder.bindListeners(records[position])
    }

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemRecordBinding.bind(itemView)

        fun bind(record: Record) {
            updateCategory(record)
            updateDate(record)
            updateNote(record)
            updateAmount(record)
            bindListeners(record)
        }

        fun applyPayload(bundle: Bundle) {
            val record = records[bindingAdapterPosition]
            if (bundle.containsKey(KEY_TYPE) || bundle.containsKey(KEY_AMOUNT)) {
                updateAmount(record)
            }
            if (bundle.containsKey(KEY_TYPE) || bundle.containsKey(KEY_CATEGORY)) {
                updateCategory(record)
            }
            if (bundle.containsKey(KEY_NOTE)) {
                updateNote(record)
            }
            if (bundle.containsKey(KEY_DATE)) {
                updateDate(record)
            }
        }

        fun bindListeners(record: Record) {
            itemView.setOnClickListener { onItemClick(record) }
            itemView.setOnLongClickListener { onItemLongClick(record); true }
        }

        private fun updateDate(record: Record) {
            binding.tvDate.text = DateUtils.formatDate(record.date)
        }

        private fun updateAmount(record: Record) {
            val isIncome = record.type == Record.TYPE_INCOME
            val prefix = if (isIncome) "+" else "-"
            binding.tvAmount.text = "${prefix}¥${DateUtils.formatAmount(record.amount)}"
            binding.tvAmount.setTextColor(
                Color.parseColor(if (isIncome) "#2ECC71" else "#E74C3C")
            )
        }

        private fun updateCategory(record: Record) {
            val color = Record.getCategoryColor(record.category)
            binding.tvCategoryIcon.text = record.category.first().toString()
            (binding.tvCategoryIcon.background.mutate() as? android.graphics.drawable.GradientDrawable)
                ?.setColor(color)
            binding.tvCategoryName.text = record.category
        }

        private fun updateNote(record: Record) {
            if (!record.note.isNullOrBlank()) {
                binding.tvNote.text = "备注：${record.note}"
                binding.tvNote.visibility = View.VISIBLE
            } else {
                binding.tvNote.visibility = View.GONE
            }
        }
    }

    class RecordDiffCallback(
        private val oldList: List<Record>,
        private val newList: List<Record>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            val bundle = Bundle()

            if (old.amount != new.amount) bundle.putDouble(KEY_AMOUNT, new.amount)
            if (old.type != new.type) bundle.putInt(KEY_TYPE, new.type)
            if (old.category != new.category) bundle.putString(KEY_CATEGORY, new.category)
            if (old.note != new.note) bundle.putString(KEY_NOTE, new.note)
            if (old.date != new.date) bundle.putLong(KEY_DATE, new.date)

            return bundle.takeUnless { it.isEmpty }
        }
    }

    companion object {
        private const val KEY_AMOUNT = "amount"
        private const val KEY_TYPE = "type"
        private const val KEY_CATEGORY = "category"
        private const val KEY_NOTE = "note"
        private const val KEY_DATE = "date"
    }
}
