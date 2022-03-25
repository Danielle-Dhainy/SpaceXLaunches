package com.android.spacexlaunches.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.spacexlaunches.R
import com.android.spacexlaunches.adapters.LaunchListAdapter
import com.android.spacexlaunches.databinding.FragmentMainBinding
import com.android.spacexlaunches.models.LaunchItem
import com.android.spacexlaunches.utils.Constants.Companion.CONNECTION_ERROR
import com.android.spacexlaunches.utils.Constants.Companion.LAUNCH_TAG
import com.android.spacexlaunches.utils.Resource
import com.android.spacexlaunches.viewmodels.SharedViewModel
import com.android.spacexlaunches.viewmodels.SharedViewModelFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var view: View? = null
    private lateinit var launchListAdapter: LaunchListAdapter

    private val viewModel: SharedViewModel by activityViewModels() {
        SharedViewModelFactory(
            requireContext()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        if (view == null)
            view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeLaunches()
        setUpAdapter()
        binding.errorImage.setOnClickListener {
            viewModel.loadLaunches()
        }

    }

    // to observe the launches loaded from api and handle each action : loading, data and error
    fun subscribeLaunches() {
        viewModel.launches.observe(viewLifecycleOwner, Observer { response ->
            when (response) {

                is Resource.Success -> {
                    stopLoading()
                    response.data?.let { launchResponse ->
                        doOperation(launchResponse)
                        Log.d(LAUNCH_TAG, "SUCCESS ${launchResponse.size}")
                    }
                }

                is Resource.Error -> {
                    response.message?.let { message ->
                        Log.d(LAUNCH_TAG, "ERROR " + message)
                        setError(message)
                    }
                }

                is Resource.Loading -> {
                    setLoading()
                    Log.d(LAUNCH_TAG, "LOADING")
                }
            }
        })
    }

    fun doOperation(launches: ArrayList<LaunchItem>) {

        var launchesList = ArrayList<LaunchItem>()

        /*
        * get successful and upcoming launches for the last 3 years
        * calculate progress for each launch
        * the idea behind the progress is that how much each launch has made a progress over the previous one
        * so if the launch is launched successfully the progress is increased by one
        * progress = the sum of all previous successful launches including the current one over the total launches */


        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = Date()

            val currentDateTime = currentDate.time
            for (launchItem in launches) {
                launchItem.let {
                    if (launchItem.success!! || launchItem.upcoming) {

                        val launchDate: Date = format.parse(launchItem.date_utc)
                        val launchDateStr = launchDate.toString()

                        // customise the date format

                        var formattedlDate =
                            launchDateStr.substring(8, 10) + " " + launchDateStr.substring(
                                4,
                                7
                            ) + " " +
                                    launchDateStr.substring(29, launchDateStr.length) + ", " +
                                    launchDateStr.substring(11, 16) + " " +
                                    launchDateStr.substring(20, 26)

                        val diff: Long = currentDateTime - launchDate.getTime()
                        val years = (diff / (1000 * 60 * 60 * 24)) * 0.002738

                        if (years <= 3) {
                            launchesList.add(
                                launchItem.copy(
                                    date_utc = formattedlDate,
                                    progress = 20
                                )
                            )
                        }
                    }
                }
            }

            // calculate the progress
            var finalLaunches = ArrayList<LaunchItem>()

            var totalDone = 0
            var progress: Int = 0
            var totalLaunches = launchesList.size
            Log.d("LAUNCEHS_TOTAL", totalLaunches.toString())

            for (launchItem in launchesList) {
                if (launchItem.success) {
                    totalDone++
                }
                progress = (totalDone * 100 / totalLaunches).toInt()

                finalLaunches.add(
                    launchItem.copy(
                        progress = progress
                    )
                )
            }

            launchListAdapter.differ.submitList(finalLaunches)

        } catch (e: ParseException) {
            e.printStackTrace()
            Log.d("DIFFERENCE_YEARS", "EXCEPTION: ${e.message.toString()}")
        }

    }


    fun setLoading() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            errorImage.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }
    }


    fun stopLoading() {

        binding.apply {
            progressBar.visibility = View.GONE
            errorImage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // stop loading and setup the data
    fun setUpAdapter() {
        launchListAdapter = LaunchListAdapter()
        var launches = ArrayList<LaunchItem>()

        binding.apply {
            progressBar.visibility = View.GONE
            errorImage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            recyclerView.setLayoutManager(
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            )
            recyclerView.setItemAnimator(DefaultItemAnimator())
            recyclerView.setAdapter(launchListAdapter)
        }
        try {

            launchListAdapter.differ.submitList(launches)
        } catch (e: Exception) {
        }
    }

    // stop loading and display an error image
    fun setError(error_type: String) {
        binding.apply {
            progressBar.visibility = View.GONE
            errorImage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            if (error_type.equals(CONNECTION_ERROR))
                errorImage.setImageResource(R.drawable.no_connection)
            else
                errorImage.setImageResource(R.drawable.error)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}