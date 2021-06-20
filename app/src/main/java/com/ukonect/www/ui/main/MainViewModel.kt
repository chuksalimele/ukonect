package com.ukonect.www.ui.main

import android.app.AlertDialog
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.ukonect.www.Ukonect
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.AuthState
import com.ukonect.www.remote.model.Model
import com.ukonect.www.util.Constants
import com.ukonect.www.util.Utils
import com.google.gson.Gson
import com.ukonect.www.Ukonect.Companion.appUser
import com.ukonect.www.exception.UkonectException
import com.ukonect.www.remote.WampService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class MainViewModel : ViewModel() {

    val auth: MutableLiveData<AppUser> by lazy {
        MutableLiveData(AppUser()).also { checkAuth(it) }
    }

    val app_user get() = auth.value
    var context: Context? = null

    var wampService: WampService? = null
    var disposable: Disposable? = null
    val gson = Gson();
    public fun updateAppUser(appUser: AppUser){
        this.viewModelScope.launch {
            appUser.auth_state = app_user?.auth_state?:AuthState.AUTH_STAGE_NONE
            appUser.line1Number = app_user?.line1Number?: ""
            appUser.favourite_list = app_user?.favourite_list?: mutableSetOf()
            auth.postValue(appUser)
            saveUserInfoToDisk(appUser)
        }
    }

    /*
        Asynchronously check authentication of user
        Read the user auth saved to the system
     */
    private fun checkAuth(liveAppUser: MutableLiveData<AppUser>) {

        this.viewModelScope.launch {
            readUserInfoFromDisk()?.let {
                    liveAppUser.postValue(it)
            }

        }

    }

    fun singupStage(data: Map<String, String>, stage: AuthState){

        this.viewModelScope.launch{

            when (stage) {
                AuthState.AUTH_STAGE_NONE -> {
                    //update the LiveData
                    auth.postValue(auth.value?.modifyAuth(AuthState.AUTH_STAGE_PHONE_NUMBER_VERIFY))
                }                
                AuthState.AUTH_STAGE_PHONE_NUMBER_VERIFY -> {
                    val observable  = wampService?.getCaller()?.signUpVerifyPhoneNumber(data)

                    handleObservable(observable){
                        if(saveUserInfoToDisk(app_user)) {
                            //update the LiveData
                            auth.postValue(app_user?.modifyAuth(AuthState.AUTH_STAGE_CONFIRM_VERIFICATION_CODE))
                        }
                    };
                }
                AuthState.AUTH_STAGE_CONFIRM_VERIFICATION_CODE -> {
                    val observable  = wampService?.getCaller()?.signUpComfirmPhoneNumber(data)
                    handleObservable(observable){
                        auth.postValue(app_user?.modifyAuth(AuthState.AUTH_STAGE_FULL_NANE))
                    };
                }
                AuthState.AUTH_STAGE_FULL_NANE -> {
                    val observable  = wampService?.getCaller()?.signUpName(data)
                    handleObservable(observable){
                        auth.postValue(app_user?.modifyAuth(AuthState.AUTH_STAGE_PROFILE_PHOTO))
                    };
                }
                AuthState.AUTH_STAGE_PROFILE_PHOTO -> {

                    val observable = wampService?.getCaller()?.signUpProfilePhoto(data)
                    handleObservable(observable){
                        auth.postValue(app_user?.modifyAuth(AuthState.AUTH_STAGE_SUCCESS))
                    };
                }
            }
        }

    }

    private fun handleObservable(observable: Observable<AppUser>?, onSuccess: () -> Unit){
        disposable = observable?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { app_user ->
                    auth.value = app_user
                    onSuccess()
                },
                { error ->  Utils.handleException(context,  error)}
            )
    }


    private fun readUserInfoFromDisk() : AppUser?{
        var serializedAppUser: String? = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(Constants.KEY_APP_USER, null)
            ?: return null
        val app_user: AppUser = gson.fromJson(serializedAppUser, AppUser::class.java)

        appUser = app_user;
        return app_user
    }

    private fun saveUserInfoToDisk(app_user: AppUser?): Boolean{
        var serializedAppUser = gson.toJson(app_user)

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(Constants.KEY_APP_USER, serializedAppUser)
            .apply()

        appUser = app_user;
        return true
    }

}
