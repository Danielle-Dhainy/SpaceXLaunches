package com.android.spacexlaunches.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.spacexlaunches.R
import com.android.spacexlaunches.databinding.LaunchItemDesignBinding
import com.android.spacexlaunches.fragments.MainFragmentDirections
import com.android.spacexlaunches.models.LaunchItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class LaunchListAdapter : RecyclerView.Adapter<LaunchListAdapter.ItemViewHolder>() {

    class ItemViewHolder(private var binding: LaunchItemDesignBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(launchItem: LaunchItem) {
            binding.apply {

                number.text = launchItem.flight_number.toString()
                name.text = launchItem.name
                date.text = launchItem.date_utc
                progressBar.progress = launchItem.progress

                launchItem.links?.patch?.small.let {
                    Glide.with(itemView.context)
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.loading)

                        .into(image)
                }

                itemView.setOnClickListener {

                    val action =
                        MainFragmentDirections.actionMainFragmentToDetailsFragment(
                            rocketId = launchItem?.rocket!!,
                            progress = launchItem.progress,
                            date = launchItem.date_utc,
                            number = launchItem.flight_number
                        )
                    it.findNavController().navigate(action)

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LaunchItemDesignBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val launchItem = differ.currentList[position]
        holder.bind(launchItem)

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    // its asynchronous so its done on background , it want block our main thread

    private val differCallback = object : DiffUtil.ItemCallback<LaunchItem>() {
        override fun areItemsTheSame(oldItem: LaunchItem, newItem: LaunchItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LaunchItem, newItem: LaunchItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}
