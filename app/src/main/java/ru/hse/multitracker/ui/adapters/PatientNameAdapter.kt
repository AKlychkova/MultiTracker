package ru.hse.multitracker.ui.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.hse.multitracker.data.repositories.PatientFullName
import ru.hse.multitracker.databinding.ItemPatientNameBinding

class PatientNameAdapter(private var data: List<PatientFullName>,
                         private val onPatientClick: (PatientFullName) -> Unit) :
    RecyclerView.Adapter<PatientNameAdapter.PatientNameViewHolder>() {

    fun setData(patientNames:List<PatientFullName> ) {
        val result:DiffUtil.DiffResult  = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return data.size
            }

            override fun getNewListSize(): Int {
                return patientNames.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition].id == patientNames[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val newName: PatientFullName = patientNames[newItemPosition]
                val oldName: PatientFullName = data[oldItemPosition]
                return newName.id == oldName.id &&
                        TextUtils.equals(oldName.name,newName.name) &&
                        TextUtils.equals(oldName.surname,newName.surname) &&
                        TextUtils.equals(oldName.patronymic,newName.patronymic)
            }
        })
        data = patientNames
        result.dispatchUpdatesTo(this)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientNameViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientNameBinding.inflate(layoutInflater, parent, false)
        return PatientNameViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PatientNameViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            onPatientClick(data[position])
        }
    }

    inner class PatientNameViewHolder(private val binding: ItemPatientNameBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(patientName: PatientFullName) {
            binding.nameTextview.text =
                "${patientName.surname} ${patientName.name} ${patientName.patronymic ?: ""}"
        }
    }
}