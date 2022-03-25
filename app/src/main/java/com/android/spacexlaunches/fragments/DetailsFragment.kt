package com.android.spacexlaunches.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.android.spacexlaunches.R
import com.android.spacexlaunches.databinding.FragmentDetailsBinding
import com.android.spacexlaunches.models.RocketItem
import com.android.spacexlaunches.utils.Constants.Companion.CONNECTION_ERROR
import com.android.spacexlaunches.utils.Constants.Companion.ROCKET_TAG
import com.android.spacexlaunches.utils.Resource
import com.android.spacexlaunches.viewmodels.SharedViewModel
import com.android.spacexlaunches.viewmodels.SharedViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: DetailsFragmentArgs by navArgs()
    private var rocket_id: String = ""
    private var progress: Int = 0
    private var launch_date: String = ""
    private var launch_number: Int = 0

    private var view: View? = null

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
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        if (view == null)
            view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rocket_id = navigationArgs.rocketId
        progress = navigationArgs.progress
        launch_date = navigationArgs.date
        launch_number = navigationArgs.number

        binding.apply {
            closeImage.setOnClickListener {
                activity?.onBackPressed()
            }

                swipeRefreshLayout.setOnRefreshListener {
                    infoRel.setVisibility(View.GONE)
                    errorRel.setVisibility(View.GONE)
                    viewModel.getRocket(rocket_id)
            }
        }

        subscribeRocket()
        viewModel.getRocket(rocket_id)
    }

    // to observe the chosen rocket from api and handle each action : loading, data and error
    fun subscribeRocket() {
        viewModel.rocket.observe(viewLifecycleOwner, Observer { response ->
            when (response) {

                is Resource.Success -> {
                    response.data?.let { launchResponse ->
                        setData(launchResponse.find { it.id == rocket_id })
                    }
                    Log.d(ROCKET_TAG, "SUCCESS")
                }

                is Resource.Error -> {
                    response.message?.let { message ->
                        Log.d(ROCKET_TAG, "ERROR " + message)
                        setErrorRel(message)
                    }
                }

                is Resource.Loading -> {
                    setLoading()
                    Log.d(ROCKET_TAG, "LOADING")
                }
            }
        })
    }

    /* stop refreshing and set data
    * the read more button opens the browser to a wikipedia article to read more about the launched rocket
    * the share text allows us to share the wikipedia link via other apps */

    fun setData(rocketItem: RocketItem?) {
        binding.apply {
            errorRel.setVisibility(View.GONE)
            swipeRefreshLayout.setRefreshing(false)
            infoRel.setVisibility(View.VISIBLE)
            swipeRefreshLayout.setEnabled(false)

            rocketItem?.flickr_images!![0].let {
                Glide.with(requireContext())
                    .load(it)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.loading)
                    .into(image)
            }
            progressBar.progress = progress

            name.text=rocketItem?.name
            date.text = launch_date
            number.text = launch_number.toString()
            description.text = rocketItem?.description

            readMoreBtn.setOnClickListener {

                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(rocketItem?.wikipedia))
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Log.e("ERROR_EXCEPTION", e.message.toString())
                }
            }

            share.setOnClickListener {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, rocketItem?.wikipedia)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share Link")
                startActivity(shareIntent)
            }
        }
    }

    fun setLoading() {
        binding.apply {
            swipeRefreshLayout.setRefreshing(true)
            infoRel.setVisibility(View.GONE)
            errorRel.setVisibility(View.GONE)
        }
    }

    // in case of error show an error image and text

    fun setErrorRel(error_type: String) {
        binding.apply {
            swipeRefreshLayout.setRefreshing(false)
            infoRel.setVisibility(View.GONE)
            errorRel.setVisibility(View.VISIBLE)
            errorImage.setImageResource(android.R.color.transparent)

            if (error_type.equals(CONNECTION_ERROR)) {
                errorText.setText(resources.getString(R.string.check_connection))
                errorImage.setImageResource(R.drawable.no_connection)
            } else {
                errorText.setText(resources.getString(R.string.error_msg))
                errorImage.setImageResource(R.drawable.error)
            }
            swipeRefreshLayout.setEnabled(true)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}