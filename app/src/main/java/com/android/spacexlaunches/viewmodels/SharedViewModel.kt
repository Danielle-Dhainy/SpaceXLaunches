package com.android.spacexlaunches.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.android.spacexlaunches.models.Launch
import com.android.spacexlaunches.models.Rocket
import com.android.spacexlaunches.repository.Repository
import com.android.spacexlaunches.utils.Constants.Companion.CONNECTION_ERROR
import com.android.spacexlaunches.utils.Constants.Companion.LAUNCH_TAG
import com.android.spacexlaunches.utils.Constants.Companion.ROCKET_TAG
import com.android.spacexlaunches.utils.Constants.Companion.UNKNOWN_ERROR
import com.android.spacexlaunches.utils.Resource
import kotlinx.coroutines.launch

class SharedViewModel(private val context: Context) : ViewModel() {

    private val _launches: MutableLiveData<Resource<Launch>> = MutableLiveData()
    val launches: LiveData<Resource<Launch>> get() = _launches

    private val _rocket: MutableLiveData<Resource<Rocket>> = MutableLiveData()
    val rocket: LiveData<Resource<Rocket>> get() = _rocket

    val repository = Repository()

    init {

        loadLaunches()
    }

    fun loadLaunches() {

        viewModelScope.launch {
            _launches.postValue(Resource.Loading())

            // must check internet
            if (isNetworkAvailable()) {
                try {
                    val response = repository.loadLaunches()

                    if (response.isSuccessful) {
                        Log.d(LAUNCH_TAG, "SUCCESS ")

                        response.body()?.let { resultResponse ->
                            _launches.postValue(Resource.Success(resultResponse))
                            Log.d(LAUNCH_TAG, "SUCCESS: ${resultResponse.size} ")
                        }

                    } else {
                        _launches.postValue(Resource.Error(response.message()))
                        Log.d(LAUNCH_TAG, "FAILURE: $response.message()")

                    }
                } catch (e: Exception) {
                    Log.d(LAUNCH_TAG, "EXCEPTION $e.message.toString()")
                    _launches.postValue(Resource.Error(UNKNOWN_ERROR))
                }
            } else {
                _launches.postValue(Resource.Error(CONNECTION_ERROR))
                Log.d(LAUNCH_TAG, "CONNECTION")

            }
        }
    }

    fun getRocket(id: String) {
        viewModelScope.launch {
            _rocket.postValue(Resource.Loading())

            // must check internet
            if (isNetworkAvailable()) {
                try {
                    val response = repository.getRocket(id)

                    if (response.isSuccessful) {
                        response.body()?.let { resultResponse ->
                            _rocket.postValue(Resource.Success(resultResponse))
                            Log.d(ROCKET_TAG, "SUCCESS")

                        }
                    } else {
                        _rocket.postValue(Resource.Error(response.message()))
                        Log.d(ROCKET_TAG, "FAILURE: $response.message()")

                    }
                } catch (e: Exception) {
                    Log.d(ROCKET_TAG, "EXCEPTION $e.message.toString()")
                    _rocket.postValue(Resource.Error(UNKNOWN_ERROR))
                }
            } else {
                _rocket.postValue(Resource.Error(CONNECTION_ERROR))
                Log.d(ROCKET_TAG, "CONNECTION")
            }
        }
    }

    // check internet before loading the info
    @Suppress("DEPRECATION")
    fun isNetworkAvailable() = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getNetworkCapabilities(activeNetwork)?.run {
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } ?: false
            } else {
                activeNetworkInfo?.isConnectedOrConnecting?:false
            }
        }


    override fun onCleared() {
        super.onCleared()
    }
}

class SharedViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}